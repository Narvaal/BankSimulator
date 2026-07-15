import {useEffect, useState} from "react";
import {Link} from "react-router-dom";
import NavBar from "../navBar/NavBar";
import UserMenu from "../usermenu/UserMenu";
import Pagination from "../util/Pagination";
import {useAccount} from "../auth/Auth";
import {API_URL} from "../../config";

/* ===================== TYPES ===================== */

interface PublicProfile {
    accountId: number;
    name: string;
    picture: string | null;
    accountNumber: string;
}

interface PublicProfilePageView {
    items: PublicProfile[];
    page: number;
    pageSize: number;
    totalPages: number;
    totalItems: number;
}

/* ===================== API ===================== */

async function searchAccounts(q: string, page: number, pageSize: number): Promise<PublicProfilePageView> {
    const res = await fetch(
        `${API_URL}/accounts/search?q=${encodeURIComponent(q)}&page=${page}&pageSize=${pageSize}`
    );
    if (!res.ok) throw new Error("Search failed");
    return res.json();
}

/* ===================== PAGE ===================== */

export default function SearchPage() {
    const {data: account} = useAccount();

    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });

    const [query, setQuery] = useState("");
    const [debouncedQuery, setDebouncedQuery] = useState("");
    const [results, setResults] = useState<PublicProfilePageView | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(0);
    const pageSize = 20;

    useEffect(() => {
        localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));
    }, [collapsed]);

    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedQuery(query);
            setPage(0);
        }, 400);
        return () => clearTimeout(timer);
    }, [query]);

    useEffect(() => {
        if (debouncedQuery.trim().length < 2) {
            setResults(null);
            return;
        }

        let cancelled = false;

        async function load() {
            try {
                setLoading(true);
                setError(null);
                const data = await searchAccounts(debouncedQuery.trim(), page, pageSize);
                if (!cancelled) setResults(data);
            } catch (e) {
                if (!cancelled) setError(e instanceof Error ? e.message : String(e));
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => { cancelled = true; };
    }, [debouncedQuery, page]);

    return (
        <div className="min-h-screen bg-slate-100">
            <NavBar collapsed={collapsed} setCollapsed={setCollapsed}/>
            <UserMenu
                balance={account?.balance ?? 0}
                nextFreeAssetAt={account?.nextFreeAssetAt ?? new Date().toISOString()}
                name={account?.name ?? ""}
                imageUrl={account?.picture ?? ""}
            />

            <main
                className="pt-[60px] transition-all duration-300 p-6 min-h-screen flex flex-col"
                style={{marginLeft: collapsed ? 64 : 220}}
            >
                <div className="max-w-xl w-full">

                    <h1 className="text-xl font-bold text-slate-900 mb-4">Search Profiles</h1>

                    <input
                        type="text"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        placeholder="Type a name..."
                        className="w-full border border-slate-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-slate-400 bg-white mb-4"
                        autoFocus
                    />

                    {query.trim().length > 0 && query.trim().length < 2 && (
                        <p className="text-sm text-slate-400">Type at least 2 characters to search.</p>
                    )}

                    {loading && (
                        <p className="text-sm text-slate-400">Searching...</p>
                    )}

                    {error && (
                        <p className="text-sm text-red-500">{error}</p>
                    )}

                    {!loading && results && results.items.length === 0 && (
                        <p className="text-sm text-slate-400">No profiles found for "{debouncedQuery}".</p>
                    )}

                    {!loading && results && results.items.length > 0 && (
                        <div className="flex flex-col gap-2">
                            {results.items.map((profile) => {
                                const initials = profile.name
                                    .split(" ").map(w => w[0]).slice(0, 2).join("").toUpperCase();

                                return (
                                    <Link
                                        key={profile.accountId}
                                        to={`/profile/${profile.accountId}`}
                                        className="flex items-center gap-4 bg-white rounded-xl border border-slate-200 shadow-sm px-4 py-3 hover:shadow-md hover:scale-[1.01] transition no-underline"
                                    >
                                        {profile.picture ? (
                                            <img
                                                src={profile.picture}
                                                alt={profile.name}
                                                className="w-10 h-10 rounded-full object-cover shrink-0"
                                            />
                                        ) : (
                                            <div className="w-10 h-10 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 text-sm font-bold shrink-0">
                                                {initials}
                                            </div>
                                        )}
                                        <div>
                                            <p className="text-slate-900 font-semibold text-sm">{profile.name}</p>
                                            <p className="text-slate-400 text-xs">#{profile.accountNumber}</p>
                                        </div>
                                    </Link>
                                );
                            })}

                            <div className="mt-4">
                                <Pagination
                                    page={page}
                                    totalPages={results.totalPages}
                                    onPageChange={setPage}
                                />
                            </div>
                        </div>
                    )}

                </div>
            </main>
        </div>
    );
}
