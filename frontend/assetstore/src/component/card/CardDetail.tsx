import {useEffect, useState} from "react";
import {Link, useParams} from "react-router-dom";
import NavBar from "../navBar/NavBar";
import UserMenu from "../usermenu/UserMenu.tsx";
import PriceHistoryChart from "../market/PriceHistoryChart.tsx";
import {useAccount} from "../auth/Auth";
import {API_URL} from "../../config";

/* ===================== TYPES ===================== */

interface ArtifactDetail {
    id: number;
    text: string;
    totalSupply: number;
    createdAt: string;
}

interface ArtifactPriceHistory {
    artifactId: number;
    artifactUnitId: number;
    oldPrice: number;
    newPrice: number;
    createdAt: string;
}

/* ===================== API ===================== */

async function getArtifact(id: string): Promise<ArtifactDetail> {
    const res = await fetch(`${API_URL}/artifacts/${id}`);
    if (res.status === 404) throw new Error("not_found");
    if (!res.ok) throw new Error("fetch_error");
    return res.json();
}

async function getPriceHistory(id: string): Promise<ArtifactPriceHistory[]> {
    const res = await fetch(`${API_URL}/artifacts/${id}/price-history`);
    if (!res.ok) return [];
    return res.json();
}

/* ===================== PAGE ===================== */

export default function CardDetail() {
    const {id} = useParams<{id: string}>();
    const {data: account} = useAccount();

    const [artifact, setArtifact] = useState<ArtifactDetail | null>(null);
    const [priceHistory, setPriceHistory] = useState<ArtifactPriceHistory[]>([]);
    const [loading, setLoading] = useState(true);
    const [notFound, setNotFound] = useState(false);

    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });

    useEffect(() => {
        localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));
    }, [collapsed]);

    useEffect(() => {
        if (!id) return;
        let cancelled = false;

        async function load() {
            try {
                setLoading(true);
                setNotFound(false);
                const [art, history] = await Promise.all([getArtifact(id!), getPriceHistory(id!)]);
                if (!cancelled) {
                    setArtifact(art);
                    setPriceHistory(history);
                }
            } catch (e: unknown) {
                if (!cancelled) {
                    setNotFound(e instanceof Error && e.message === "not_found");
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => { cancelled = true; };
    }, [id]);

    const lastPrice = priceHistory.length > 0
        ? Math.max(...priceHistory.map(h => h.newPrice))
        : null;

    return (
        <div className="min-h-screen bg-slate-100">

            <NavBar collapsed={collapsed} setCollapsed={setCollapsed}/>

            {account ? (
                <UserMenu
                    balance={account.balance}
                    nextFreeAssetAt={account.nextFreeAssetAt}
                    name={account.name}
                    imageUrl={account.picture}
                />
            ) : (
                <header className="fixed top-0 left-0 w-full z-50 border-b border-zinc-100 bg-white">
                    <div className="w-full px-5 h-14 flex items-center gap-4">
                        <div className="flex items-center gap-2.5">
                            <img src="/icons/RareLines.png" alt="Rare Lines" className="h-7 w-auto object-contain"/>
                            <span className="font-semibold text-zinc-900 tracking-tight text-[14px]">Rare Lines</span>
                        </div>
                        <div className="flex-1"/>
                        <Link
                            to="/login"
                            className="px-3 py-1.5 rounded-md bg-zinc-900 text-white text-[13px] font-medium hover:bg-zinc-700 transition-colors"
                        >
                            Sign in
                        </Link>
                    </div>
                </header>
            )}

            <main
                className="pt-[60px] p-6 transition-all duration-300"
                style={{marginLeft: collapsed ? 64 : 220}}
            >
                {/* breadcrumb */}
                <nav className="mb-5 text-xs text-zinc-400 flex items-center gap-1.5">
                    <Link to="/market" className="hover:text-zinc-600 transition-colors">Marketplace</Link>
                    <span>/</span>
                    <span className="text-zinc-600">Card #{id}</span>
                </nav>

                {loading && (
                    <p className="text-center text-slate-400 mt-20">Loading...</p>
                )}

                {!loading && notFound && (
                    <div className="text-center mt-20">
                        <p className="text-zinc-500 text-lg font-medium">Card not found</p>
                        <Link to="/market" className="mt-4 inline-block text-sm text-zinc-400 hover:text-zinc-700 transition-colors">
                            ← Back to Marketplace
                        </Link>
                    </div>
                )}

                {!loading && !notFound && artifact && (
                    <div className="max-w-2xl mx-auto">

                        {/* card header */}
                        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 mb-4">
                            <div className="flex items-start justify-between gap-4">
                                <div>
                                    <h1 className="text-2xl font-bold text-zinc-900">{artifact.text}</h1>
                                    <div className="mt-1 flex flex-wrap gap-3 text-xs text-zinc-400">
                                        <span>Card #{artifact.id}</span>
                                        <span>·</span>
                                        <span>Supply: {artifact.totalSupply}</span>
                                        <span>·</span>
                                        <span>Added {new Date(artifact.createdAt).toLocaleDateString()}</span>
                                    </div>
                                </div>

                                {lastPrice !== null && (
                                    <div className="shrink-0 text-right">
                                        <div className="text-xs text-zinc-400 mb-0.5">Last sale</div>
                                        <div className="text-xl font-bold text-emerald-600">
                                            ${Number(lastPrice).toFixed(2)}
                                        </div>
                                    </div>
                                )}
                            </div>

                            <div className="mt-5">
                                <h2 className="text-sm font-semibold text-zinc-700 mb-1">Price History</h2>
                                <PriceHistoryChart priceHistory={priceHistory}/>
                            </div>
                        </div>

                        {/* actions */}
                        <div className="flex gap-3">
                            <Link
                                to="/market"
                                className="flex-1 text-center px-4 py-2.5 rounded-xl bg-zinc-900 text-white text-sm font-medium hover:bg-zinc-700 transition-colors"
                            >
                                View on Marketplace
                            </Link>
                            <Link
                                to="/logs"
                                className="flex-1 text-center px-4 py-2.5 rounded-xl border border-slate-200 bg-white text-zinc-700 text-sm font-medium hover:bg-slate-50 transition-colors"
                            >
                                Transfer Log
                            </Link>
                        </div>

                    </div>
                )}
            </main>
        </div>
    );
}
