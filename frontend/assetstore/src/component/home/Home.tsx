import {useEffect, useState} from "react";
import NavBar from "../navBar/NavBar";
import {useAccount} from "../auth/Auth";
import PriceHistoryChart from "../market/PriceHistoryChart.tsx";
import Pagination from "../util/Pagination.tsx";
import UserMenu from "../usermenu/UserMenu.tsx";
import { API_URL } from "../../config";
import { authHeader } from "../../auth";
import {useNavigate} from "react-router-dom";
import AuthRequiredModal from "../auth/AuthRequiredModal.tsx";
import {ArtifactCardThumb, ArtifactCardFullscreen, type CardMetadata} from "../artifact/ArtifactCard.tsx";

/* ===================== TYPES ===================== */

interface ArtifactView {
    artifactUnitId: number;
    artifactId: number;
    artifactName: string;
    metadata: CardMetadata;
    createdAt: string;
}

interface ArtifactPriceHistory {
    artifactId: number;
    artifactUnitId: number;
    oldPrice: number;
    newPrice: number;
    createdAt: string;
}

interface ArtifactPageView {
    items: ArtifactView[];
    page: number,
    pageSize: number,
    totalPages: number,
    totalItems: number
}

/* ===================== API ===================== */

async function getAssetUnits(
    ownerId: number,
    page: number,
    pageSize: number
): Promise<ArtifactPageView> {

    const res = await fetch(
        `${API_URL}/artifact-units?ownerId=${ownerId}&page=${page}&pageSize=${pageSize}`,
        {credentials: "include", headers: authHeader()}
    );

    if (!res.ok) throw new Error("Assets not found");
    return res.json();
}

async function getArtifactPriceHistory(artifactUnitId: number): Promise<ArtifactPriceHistory[]> {
    const res = await fetch(
        `${API_URL}/artifacts/${artifactUnitId}/price-history`,
        {credentials: "include"}
    );

    if (!res.ok) throw new Error("Artifact price history not found");

    return res.json();
}

async function postAssetOffer(artifactUnitId: number, price: number) {
    const res = await fetch(
        `${API_URL}/artifact-offers`,
        {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                ...authHeader()
            },
            body: JSON.stringify({artifactUnitId, price})
        }
    );

    if (!res.ok) throw new Error("Artifact offer not valid");
    return res.json();
}

/* ===================== PAGE ===================== */

export default function Home() {

    const {data: account, isLoading: authLoading, error: authError} = useAccount();
    const navigate = useNavigate();
    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });

    const [assets, setAssets] = useState<ArtifactPageView | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [selectedAsset, setSelectedAsset] = useState<ArtifactView | null>(null);
    const [priceHistory, setPriceHistory] = useState<ArtifactPriceHistory[]>([]);
    const [price, setPrice] = useState("");
    const [loadingHistory, setLoadingHistory] = useState(false);

    const [page, setPage] = useState(0);
    const pageSize = 30;

    const [message, setMessage] = useState<{
        type: "success" | "error";
        text: string;
    } | null>(null);

    /* ===================== LOAD INVENTORY ===================== */

    useEffect(() => {
        if (!account) return;

        const accountId = account.id;
        let cancelled = false;

        async function load() {
            try {
                setLoading(true);
                setError(null);

                const units: ArtifactPageView = await getAssetUnits(accountId, page, pageSize);

                if (!cancelled) setAssets(units);

            } catch (e) {
                if (!cancelled) setError(e instanceof Error ? e.message : String(e));
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();

        return () => {
            cancelled = true;
        };

    }, [account, page]);

    useEffect(() => {
        localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));
    }, [collapsed]);

    /* ===================== OPEN ASSET ===================== */

    async function openAsset(artifact: ArtifactView) {

        try {

            setSelectedAsset(artifact);
            setLoadingHistory(true);
            setMessage(null);
            setPrice("");

            const history = await getArtifactPriceHistory(artifact.artifactUnitId);

            setPriceHistory(history);

        } catch {

            setPriceHistory([]);

        } finally {

            setLoadingHistory(false);

        }
    }

    /* ===================== SELL ===================== */

    async function handleSell() {

        if (!selectedAsset) return;

        const numericPrice = Number(price);

        if (!price || isNaN(numericPrice) || numericPrice <= 0) {
            setMessage({
                type: "error",
                text: "Invalid price"
            });
            return;
        }

        if (price.includes(".") && price.split(".")[1].length > 2) {
            setMessage({
                type: "error",
                text: "Price cannot have more than 2 decimal places"
            });
            return;
        }

        try {

            await postAssetOffer(selectedAsset.artifactUnitId, numericPrice);

            setAssets((prev) => {

                if (!prev) return prev;

                return {
                    ...prev,
                    items: prev.items.filter(
                        (a) => a.artifactUnitId !== selectedAsset.artifactUnitId
                    )
                };

            });

            setMessage({
                type: "success",
                text: "Artifact listed successfully"
            });

            setPrice("");

            setTimeout(() => {
                setSelectedAsset(null);
                setMessage(null);
            }, 800);

        } catch (e) {

            setMessage({
                type: "error",
                text: e instanceof Error && e.message ? e.message : "Failed to list artifact"
            });

        }
    }

    /* ===================== AUTH STATES ===================== */

    if (authLoading) {
        return <div className="p-10 text-center">Checking session...</div>;
    }

    if (authError || !account) {
        return (
            <AuthRequiredModal onClose={() => navigate(-1)}/>
        );
    }

    /* ===================== UI ===================== */

    return (
        <div className="min-h-screen bg-slate-100">

            <NavBar collapsed={collapsed} setCollapsed={setCollapsed}/>
            <UserMenu balance={account.balance} nextFreeAssetAt={account.nextFreeAssetAt} name={account.name}
                      imageUrl={account.picture}></UserMenu>

            <main
                className="pt-[60px] transition-all duration-300 p-6 min-h-screen flex flex-col"
                style={{marginLeft: collapsed ? 64 : 220}}
            >

                {loading && (
                    <p className="text-center text-slate-500">
                        Loading...
                    </p>
                )}

                {error && (
                    <p className="text-center text-red-500">
                        {error}
                    </p>
                )}

                <div className="flex-1">

                    {!loading && !error && assets && (

                        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                            {assets.items.map((artifact) => (
                                artifact.metadata && Object.keys(artifact.metadata).length > 0 ? (
                                    <ArtifactCardThumb
                                        key={artifact.artifactUnitId}
                                        metadata={artifact.metadata}
                                        onClick={() => openAsset(artifact)}
                                    />
                                ) : (
                                    <div
                                        key={artifact.artifactUnitId}
                                        onClick={() => openAsset(artifact)}
                                        className="rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow-md hover:scale-[1.02] transition p-5 cursor-pointer flex flex-col items-center justify-center text-center min-h-27"
                                    >
                                        <span className="text-slate-800 font-semibold">{artifact.artifactName}</span>
                                        <div className="mt-1 text-xs text-slate-500">
                                            #{artifact.artifactId} · unit #{artifact.artifactUnitId}
                                        </div>
                                    </div>
                                )
                            ))}
                        </div>

                    )}

                </div>

                <div className="mt-auto pt-10">
                    <Pagination
                        page={page}
                        totalPages={assets?.totalPages ?? 0}
                        onPageChange={setPage}
                    />
                </div>

            </main>

            {/* ===================== MODAL ===================== */}

            {selectedAsset && (
                <ArtifactCardFullscreen
                    metadata={selectedAsset.metadata ?? {}}
                    title="Sell Artifact"
                    onClose={() => setSelectedAsset(null)}
                >
                    <div className="p-5 space-y-4">

                        {/* PRICE HISTORY */}
                        <div>
                            <h3 className="text-sm font-semibold mb-2">Price History</h3>
                            {loadingHistory && <p className="text-sm text-slate-500">Loading...</p>}
                            <PriceHistoryChart priceHistory={priceHistory} />
                        </div>

                        <input
                            type="number"
                            step="0.01"
                            min="0.01"
                            placeholder="Price"
                            value={price}
                            onChange={(e) => setPrice(e.target.value)}
                            className="w-full border rounded-lg p-2 text-sm"
                        />

                        <div className="flex gap-3">
                            <button
                                onClick={() => setSelectedAsset(null)}
                                className="flex-1 px-4 py-2 border rounded-lg text-sm text-zinc-700 hover:bg-slate-50 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleSell}
                                className="flex-1 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-sm font-medium transition-colors"
                            >
                                List for sale
                            </button>
                        </div>

                        {message && (
                            <p className={`text-xs text-center ${message.type === "success" ? "text-emerald-600" : "text-red-600"}`}>
                                {message.text}
                            </p>
                        )}
                    </div>
                </ArtifactCardFullscreen>
            )}

        </div>
    );
}