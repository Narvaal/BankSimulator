import {useEffect, useState} from "react";
import NavBar from "../navBar/NavBar";
import {useAccount} from "../auth/Auth";
import PriceHistoryChart from "../market/PriceHistoryChart.tsx";
import Pagination from "../util/Pagination.tsx";
import UserMenu from "../usermenu/UserMenu.tsx";

/* ===================== TYPES ===================== */

interface AssetView {
    assetUnityId: number;
    assetId: number;
    assetText: string;
    createdAt: string;
}

interface AssetPriceHistory {
    assetId: number;
    assetUnityId: number;
    oldPrice: number;
    newPrice: number;
    createdAt: string;
}

interface AssetPageView {
    items: AssetView[];
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
): Promise<AssetPageView> {

    const res = await fetch(
        `http://BankSimulator.us-east-2.elasticbeanstalk.com/asset-units?ownerId=${ownerId}&page=${page}&pageSize=${pageSize}`,
        {credentials: "include"}
    );

    if (!res.ok) throw new Error("Assets not found");
    return res.json();
}

async function getAssetPriceHistory(assetUnityId: number): Promise<AssetPriceHistory[]> {
    const res = await fetch(
        `http://BankSimulator.us-east-2.elasticbeanstalk.com/assets/${assetUnityId}/price-history`,
        {credentials: "include"}
    );

    if (!res.ok) throw new Error("Asset price history not found");

    return res.json();
}

async function postAssetOffer(assetUnityId: number, price: number) {
    const res = await fetch(
        `http://BankSimulator.us-east-2.elasticbeanstalk.com/asset-offers`,
        {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({assetUnityId, price})
        }
    );

    if (!res.ok) throw new Error("Asset offer not valid");
    return res.json();
}

/* ===================== PAGE ===================== */

export default function Home() {

    const {data: account, isLoading: authLoading, error: authError} = useAccount();

    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });

    const [assets, setAssets] = useState<AssetPageView | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [selectedAsset, setSelectedAsset] = useState<AssetView | null>(null);
    const [priceHistory, setPriceHistory] = useState<AssetPriceHistory[]>([]);
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

        localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));

        const accountId = account.id;
        let cancelled = false;

        async function load() {
            try {

                setLoading(true);
                setError(null);

                const units: AssetPageView = await getAssetUnits(accountId, page, pageSize);

                if (!cancelled) setAssets(units);

            } catch (e: any) {

                if (!cancelled) setError(e.message);

            } finally {

                if (!cancelled) setLoading(false);

            }
        }

        load();

        return () => {
            cancelled = true;
        };

    }, [account, page, collapsed]);

    /* ===================== OPEN ASSET ===================== */

    async function openAsset(asset: AssetView) {

        try {

            setSelectedAsset(asset);
            setLoadingHistory(true);
            setMessage(null);
            setPrice("");

            const history = await getAssetPriceHistory(asset.assetUnityId);

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

            await postAssetOffer(selectedAsset.assetUnityId, numericPrice);

            setAssets((prev) => {

                if (!prev) return prev;

                return {
                    ...prev,
                    items: prev.items.filter(
                        (a) => a.assetUnityId !== selectedAsset.assetUnityId
                    )
                };

            });

            setMessage({
                type: "success",
                text: "Asset listed successfully"
            });

            setPrice("");

            setTimeout(() => {
                setSelectedAsset(null);
                setMessage(null);
            }, 800);

        } catch (e: any) {

            setMessage({
                type: "error",
                text: e.message || "Failed to list asset"
            });

        }
    }

    /* ===================== AUTH STATES ===================== */

    if (authLoading) {
        return <div className="p-10 text-center">Checking session...</div>;
    }

    if (authError || !account) {
        return <div className="p-10 text-center text-red-500">Not authenticated</div>;
    }

    /* ===================== UI ===================== */

    return (
        <div className="min-h-screen bg-slate-100">

            <NavBar collapsed={collapsed} setCollapsed={setCollapsed}/>
            <UserMenu balance={account.balance} nextFreeAssetAt={account.nextFreeAssetAt} name={account.name}
                      imageUrl={account.picture}></UserMenu>

            <main
                className="pt-22 transition-all duration-300 p-6 min-h-screen flex flex-col"
                style={{marginLeft: collapsed ? 80 : 256}}
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

                            {assets.items.map((asset) => (

                                <div
                                    key={asset.assetUnityId}
                                    onClick={() => openAsset(asset)}
                                    className="rounded-xl border border-slate-200 bg-white shadow-sm
                        hover:shadow-md hover:scale-[1.02] transition
                        p-5 cursor-pointer flex flex-col items-center justify-center text-center min-h-27"
                                >

                        <span className="text-slate-800 font-semibold">
                            {asset.assetText}
                        </span>

                                    <div className="mt-1 text-xs text-slate-500">
                                        Asset #{asset.assetId} • Unity
                                        #{asset.assetUnityId} • {new Date(asset.createdAt).toLocaleDateString()}
                                    </div>

                                </div>

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

                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">

                    <div className="bg-white rounded-2xl w-105 p-6 shadow-xl">

                        <h2 className="text-xl font-bold mb-2">
                            Sell Asset
                        </h2>

                        <div className="text-left mb-4">
                            <div className="text-slate-800 font-semibold">
                                {selectedAsset.assetText}
                            </div>
                            <div className="text-xs text-slate-500">
                                Asset #{selectedAsset.assetId} • Unity
                                #{selectedAsset.assetUnityId} • {new Date(selectedAsset.createdAt).toLocaleDateString()}
                            </div>
                        </div>

                        {/* PRICE HISTORY */}

                        <div className="mb-5">

                            <h3 className="text-sm font-semibold mb-2">
                                Price History
                            </h3>

                            {loadingHistory && (
                                <p className="text-sm text-slate-500">
                                    Loading...
                                </p>
                            )}

                            <PriceHistoryChart priceHistory={priceHistory}></PriceHistoryChart>

                        </div>

                        {/* PRICE INPUT */}

                        <input
                            type="number"
                            step="0.01"
                            min="0.01"
                            placeholder="Price"
                            value={price}
                            onChange={(e) => setPrice(e.target.value)}
                            className="w-full border rounded-lg p-2 mb-4"
                        />

                        {/* BUTTONS */}

                        <div className="flex justify-end gap-3">

                            <button
                                onClick={() => setSelectedAsset(null)}
                                className="px-4 py-2 border rounded-lg text-black hover:bg-slate-900 hover:text-white"
                            >
                                Cancel
                            </button>

                            <button
                                onClick={handleSell}
                                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg"
                            >
                                List for sale
                            </button>

                        </div>

                        {/* MESSAGE */}

                        {message && (

                            <div
                                className={`mt-4 text-sm text-center p-2 rounded ${
                                    message.type === "success"
                                        ? "bg-green-100 text-green-700"
                                        : "bg-red-100 text-red-700"
                                }`}
                            >
                                {message.text}
                            </div>

                        )}

                    </div>

                </div>

            )}

        </div>
    );
}