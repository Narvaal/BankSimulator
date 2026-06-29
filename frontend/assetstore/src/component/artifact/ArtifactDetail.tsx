import {useEffect, useState} from "react";
import {Link, useParams} from "react-router-dom";
import NavBar from "../navBar/NavBar";
import UserMenu from "../usermenu/UserMenu.tsx";
import PriceHistoryChart from "../market/PriceHistoryChart.tsx";
import {useAccount} from "../auth/Auth";
import {API_URL} from "../../config";

/* ===================== TYPES ===================== */

interface ArtifactPriceHistory {
    artifactId: number;
    artifactUnitId: number;
    oldPrice: number;
    newPrice: number;
    createdAt: string;
}

interface ArtifactUnitTransferView {
    id: number;
    fromAccountId: number;
    toAccountId: number;
    salePrice: number | null;
    createdAt: string;
}

interface ArtifactUnitDetail {
    unitId: number;
    artifactId: number;
    artifactText: string;
    ownerAccountId: number;
    status: "AVAILABLE" | "IN_MARKET" | "RESERVED" | "TRANSFERRING";
    createdAt: string;
    priceHistory: ArtifactPriceHistory[];
    transfers: ArtifactUnitTransferView[];
}

/* ===================== API ===================== */

async function getUnitDetail(id: string): Promise<ArtifactUnitDetail> {
    const res = await fetch(`${API_URL}/artifact-units/${id}`);
    if (res.status === 404) throw new Error("not_found");
    if (!res.ok) throw new Error("fetch_error");
    return res.json();
}

/* ===================== STATUS BADGE ===================== */

function StatusBadge({status}: {status: ArtifactUnitDetail["status"]}) {
    const styles: Record<string, string> = {
        AVAILABLE:    "bg-emerald-100 text-emerald-700",
        IN_MARKET:    "bg-blue-100 text-blue-700",
        RESERVED:     "bg-amber-100 text-amber-700",
        TRANSFERRING: "bg-purple-100 text-purple-700",
    };
    const labels: Record<string, string> = {
        AVAILABLE:    "Available",
        IN_MARKET:    "Listed",
        RESERVED:     "Reserved",
        TRANSFERRING: "Transferring",
    };
    return (
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${styles[status] ?? "bg-slate-100 text-slate-600"}`}>
            {labels[status] ?? status}
        </span>
    );
}

/* ===================== PAGE ===================== */

export default function ArtifactDetail() {
    const {id} = useParams<{id: string}>();
    const {data: account} = useAccount();

    const [unit, setUnit] = useState<ArtifactUnitDetail | null>(null);
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
                const data = await getUnitDetail(id!);
                if (!cancelled) setUnit(data);
            } catch (e: unknown) {
                if (!cancelled) setNotFound(e instanceof Error && e.message === "not_found");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => { cancelled = true; };
    }, [id]);

    const lastPrice = unit?.priceHistory.length
        ? unit.priceHistory[unit.priceHistory.length - 1].newPrice
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
                <nav className="mb-5 text-xs text-zinc-400 flex items-center gap-1.5">
                    <Link to="/market" className="hover:text-zinc-600 transition-colors">Marketplace</Link>
                    <span>/</span>
                    <span className="text-zinc-600">Artifact #{id}</span>
                </nav>

                {loading && (
                    <p className="text-center text-slate-400 mt-20">Loading...</p>
                )}

                {!loading && notFound && (
                    <div className="text-center mt-20">
                        <p className="text-zinc-500 text-lg font-medium">Artifact not found</p>
                        <Link to="/market" className="mt-4 inline-block text-sm text-zinc-400 hover:text-zinc-700 transition-colors">
                            ← Back to Marketplace
                        </Link>
                    </div>
                )}

                {!loading && !notFound && unit && (
                    <div className="max-w-2xl mx-auto space-y-4">

                        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
                            <div className="flex items-start justify-between gap-4">
                                <div>
                                    <h1 className="text-2xl font-bold text-zinc-900">{unit.artifactText}</h1>
                                    <div className="mt-1.5 flex flex-wrap items-center gap-2.5 text-xs text-zinc-400">
                                        <span>Unit #{unit.unitId}</span>
                                        <span>·</span>
                                        <span>Artifact #{unit.artifactId}</span>
                                        <span>·</span>
                                        <span>Owner #{unit.ownerAccountId}</span>
                                        <span>·</span>
                                        <span>{new Date(unit.createdAt).toLocaleDateString()}</span>
                                    </div>
                                    <div className="mt-2">
                                        <StatusBadge status={unit.status}/>
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
                        </div>

                        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
                            <h2 className="text-sm font-semibold text-zinc-700 mb-1">Price History</h2>
                            <PriceHistoryChart priceHistory={unit.priceHistory}/>
                        </div>

                        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
                            <h2 className="text-sm font-semibold text-zinc-700 mb-3">Ownership Chain</h2>

                            {unit.transfers.length === 0 ? (
                                <p className="text-sm text-zinc-400">No transfers yet — this artifact has never been sold.</p>
                            ) : (
                                <ol className="space-y-3">
                                    {unit.transfers.map((t, i) => (
                                        <li key={t.id} className="flex items-center gap-3 text-sm">
                                            <span className="shrink-0 w-5 h-5 rounded-full bg-slate-100 text-zinc-500 text-xs flex items-center justify-center font-medium">
                                                {i + 1}
                                            </span>
                                            <span className="font-mono text-zinc-500 text-xs">#{t.fromAccountId}</span>
                                            <span className="text-zinc-300">→</span>
                                            <span className="font-mono text-zinc-500 text-xs">#{t.toAccountId}</span>
                                            {t.salePrice != null && (
                                                <>
                                                    <span className="text-zinc-300">·</span>
                                                    <span className="font-semibold text-emerald-600">
                                                        ${Number(t.salePrice).toFixed(2)}
                                                    </span>
                                                </>
                                            )}
                                            <span className="ml-auto text-zinc-400 text-xs">
                                                {new Date(t.createdAt).toLocaleDateString()}
                                            </span>
                                        </li>
                                    ))}
                                </ol>
                            )}
                        </div>

                        <div className="flex gap-3">
                            <Link
                                to={`/market?artifactId=${unit.artifactId}&artifactText=${encodeURIComponent(unit.artifactText)}`}
                                className="flex-1 text-center px-4 py-2.5 rounded-xl bg-zinc-900 text-white text-sm font-medium hover:bg-zinc-700 transition-colors"
                            >
                                View in Market
                            </Link>
                            <Link
                                to={`/logs?artifactId=${unit.artifactId}&artifactText=${encodeURIComponent(unit.artifactText)}`}
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
