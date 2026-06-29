import {useEffect, useRef, useState} from "react";
import {Link, useSearchParams} from "react-router-dom";
import NavBar from "../navBar/NavBar";
import UserMenu from "../usermenu/UserMenu.tsx";
import {useAccount} from "../auth/Auth";
import Pagination from "../util/Pagination.tsx";
import {API_URL} from "../../config";

/* ===================== TYPES ===================== */

interface TransferLogView {
    id: number;
    artifactText: string;
    artifactUnitId: number;
    salePrice: number | null;
    fromAccountId: number;
    toAccountId: number;
    createdAt: string;
}

interface TransferLogPageView {
    items: TransferLogView[];
    page: number;
    pageSize: number;
    totalPages: number;
    totalItems: number;
}

/* ===================== API ===================== */

async function getTransferLog(
    page: number,
    pageSize: number,
    artifactId: number | null
): Promise<TransferLogPageView> {
    const params = new URLSearchParams({page: String(page), pageSize: String(pageSize)});
    if (artifactId != null) params.set("artifactId", String(artifactId));
    const res = await fetch(`${API_URL}/artifact-transfers?${params}`);
    if (!res.ok) throw new Error("Failed to load transfer log");
    return res.json();
}

/* ===================== PAGE ===================== */

export default function Logs() {
    const {data: account} = useAccount();
    const [searchParams, setSearchParams] = useSearchParams();

    const [log, setLog] = useState<TransferLogPageView | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(0);
    const pageSize = 30;

    const [artifactId, setArtifactId] = useState<number | null>(null);
    const [artifactLabel, setArtifactLabel] = useState<string | null>(null);

    const filtersInitialized = useRef(false);

    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });

    useEffect(() => {
        localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));
    }, [collapsed]);

    useEffect(() => {
        if (filtersInitialized.current) return;
        filtersInitialized.current = true;
        const aid = searchParams.get("artifactId");
        const label = searchParams.get("artifactText");
        if (aid) {
            setArtifactId(Number(aid));
            setArtifactLabel(label);
        }
    }, [searchParams]);

    useEffect(() => {
        let cancelled = false;

        async function load() {
            try {
                setLoading(true);
                setError(null);
                const data = await getTransferLog(page, pageSize, artifactId);
                if (!cancelled) setLog(data);
            } catch {
                if (!cancelled) setError("Failed to load transfer log");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => { cancelled = true; };
    }, [page, artifactId]);

    function clearArtifactFilter() {
        setArtifactId(null);
        setArtifactLabel(null);
        setPage(0);
        setSearchParams({});
    }

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
                <div className="mb-6 flex items-center gap-3 flex-wrap">
                    <div>
                        <h1 className="text-xl font-bold text-zinc-900">Transfer Log</h1>
                        <p className="text-sm text-zinc-500 mt-0.5">
                            {artifactId != null
                                ? `Transfers of "${artifactLabel ?? `#${artifactId}`}"`
                                : "All artifact sales on the platform, from newest to oldest."}
                        </p>
                    </div>

                    {artifactId != null && (
                        <button
                            onClick={clearArtifactFilter}
                            className="ml-auto flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full bg-zinc-900 text-white hover:bg-zinc-700 transition-colors"
                        >
                            <span>{artifactLabel ?? `#${artifactId}`}</span>
                            <span className="ml-1">×</span>
                        </button>
                    )}
                </div>

                {loading && <p className="text-center text-slate-500">Loading...</p>}
                {error && <p className="text-center text-red-500">{error}</p>}

                {!loading && !error && log && (
                    <>
                        {log.items.length === 0 ? (
                            <p className="text-center text-slate-400 mt-16">No transfers yet.</p>
                        ) : (
                            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                                <table className="w-full text-sm">
                                    <thead>
                                        <tr className="border-b border-slate-100 text-left text-xs text-zinc-500 uppercase tracking-wide">
                                            <th className="px-5 py-3 font-medium">Artifact</th>
                                            <th className="px-5 py-3 font-medium">Unit #</th>
                                            <th className="px-5 py-3 font-medium">Price</th>
                                            <th className="px-5 py-3 font-medium">From</th>
                                            <th className="px-5 py-3 font-medium">To</th>
                                            <th className="px-5 py-3 font-medium">Date</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-slate-50">
                                        {log.items.map((entry) => (
                                            <tr
                                                key={entry.id}
                                                className="hover:bg-slate-50 transition-colors"
                                            >
                                                <td className="px-5 py-3.5 font-medium text-zinc-800">
                                                    <Link
                                                        to={`/artifact/${entry.artifactUnitId}`}
                                                        className="hover:text-zinc-500 transition-colors"
                                                    >
                                                        {entry.artifactText}
                                                    </Link>
                                                </td>
                                                <td className="px-5 py-3.5 text-zinc-500">
                                                    <Link
                                                        to={`/artifact/${entry.artifactUnitId}`}
                                                        className="hover:text-zinc-700 transition-colors"
                                                    >
                                                        #{entry.artifactUnitId}
                                                    </Link>
                                                </td>
                                                <td className="px-5 py-3.5 font-semibold text-emerald-600">
                                                    {entry.salePrice != null
                                                        ? `$${Number(entry.salePrice).toFixed(2)}`
                                                        : "—"}
                                                </td>
                                                <td className="px-5 py-3.5 text-zinc-500 font-mono text-xs">
                                                    #{entry.fromAccountId}
                                                </td>
                                                <td className="px-5 py-3.5 text-zinc-500 font-mono text-xs">
                                                    #{entry.toAccountId}
                                                </td>
                                                <td className="px-5 py-3.5 text-zinc-400 text-xs">
                                                    {new Date(entry.createdAt).toLocaleString()}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}

                        <div className="mt-4 text-xs text-zinc-400 text-right">
                            {log.totalItems} transfer{log.totalItems !== 1 ? "s" : ""} total
                        </div>

                        <Pagination
                            page={page}
                            totalPages={log.totalPages}
                            onPageChange={setPage}
                        />
                    </>
                )}
            </main>
        </div>
    );
}
