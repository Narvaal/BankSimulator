import {useEffect, useRef, useState} from "react";
import {useQueryClient} from "@tanstack/react-query";
import {useSearchParams} from "react-router-dom";
import NavBar from "../navBar/NavBar";
import PriceHistoryChart from "../market/PriceHistoryChart";
import {useAccount} from "../auth/Auth";
import Pagination from "../util/Pagination.tsx";
import UserMenu from "../usermenu/UserMenu.tsx";
import {API_URL} from "../../config";
import {authHeader} from "../../auth";
import {Link} from "react-router-dom";
import AuthRequiredModal from "../auth/AuthRequiredModal.tsx";

/* ===================== TYPES ===================== */

interface ArtifactPriceHistory {
    artifactId: number;
    artifactUnitId: number;
    oldPrice: number;
    newPrice: number;
    createdAt: string;
}

interface ListingView {
    id: number;
    artifactUnitId: number;
    artifactId: number;
    artifactName: string;
    price: number;
    createdAt: string;
}

interface artifactListingPageView {
    items: ListingView[]
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
}

type SortOption = "newest" | "price_asc" | "price_desc";

/* ===================== API ===================== */

async function getListings(
    page: number,
    pageSize: number,
    artifactId: number | null,
    search: string,
    sort: SortOption,
    minPrice: string,
    maxPrice: string
): Promise<artifactListingPageView> {
    const params = new URLSearchParams({page: String(page), pageSize: String(pageSize)});
    if (artifactId != null) params.set("artifactId", String(artifactId));
    if (search.trim().length >= 2) params.set("q", search.trim());
    if (sort !== "newest") params.set("sort", sort);
    if (minPrice !== "") params.set("minPrice", minPrice);
    if (maxPrice !== "") params.set("maxPrice", maxPrice);

    const res = await fetch(`${API_URL}/artifact-listings?${params}`, {
        credentials: "include",
        headers: authHeader()
    });
    if (!res.ok) throw new Error();
    return res.json();
}

async function getUserListings(page: number, pageSize: number) {
    const res = await fetch(`${API_URL}/artifact-listings/me?page=${page}&pageSize=${pageSize}`, {
        credentials: "include",
        headers: authHeader()
    });
    if (!res.ok) throw new Error();
    return res.json();
}

async function cancelOffer(artifactListingId: number) {
    const res = await fetch(`${API_URL}/artifact-offers/cancel`, {
        method: "POST",
        credentials: "include",
        headers: {"Content-Type": "application/json", ...authHeader()},
        body: JSON.stringify({artifactListingId})
    });
    if (!res.ok) throw new Error();
}

async function getArtifactPriceHistory(artifactUnitId: number) {
    const res = await fetch(`${API_URL}/artifacts/${artifactUnitId}/price-history`);
    if (!res.ok) throw new Error();
    return res.json();
}

async function buyArtifactUnit(artifactListingId: number) {
    const res = await fetch(
        `${API_URL}/artifact-listings/${artifactListingId}/purchase`,
        {method: "POST", credentials: "include", headers: authHeader()}
    );
    if (!res.ok) throw new Error();
    return res.json();
}

/* ===================== PAGE ===================== */

function Marketplace() {

    const {data: account} = useAccount();
    const queryClient = useQueryClient();
    const [searchParams] = useSearchParams();

    const [mode, setMode] = useState<"market" | "user">("market");

    const [listings, setListings] = useState<artifactListingPageView | null>(null);
    const [selectedListing, setSelectedListing] = useState<ListingView | null>(null);

    const [priceHistory, setPriceHistory] = useState<ArtifactPriceHistory[]>([]);
    const [loadingHistory, setLoadingHistory] = useState(false);

    const [page, setPage] = useState(0);
    const pageSize = 24;

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });

    const [message, setMessage] = useState<{type: "success" | "error"; text: string} | null>(null);
    const [authModalOpen, setAuthModalOpen] = useState(false);

    /* ===================== FILTERS ===================== */

    const [artifactId, setArtifactId] = useState<number | null>(null);
    const [artifactLabel, setArtifactLabel] = useState<string | null>(null);
    const [search, setSearch] = useState("");
    const [debouncedSearch, setDebouncedSearch] = useState("");
    const [sort, setSort] = useState<SortOption>("newest");
    const [minPrice, setMinPrice] = useState("");
    const [maxPrice, setMaxPrice] = useState("");

    const filtersInitialized = useRef(false);

    useEffect(() => {
        if (filtersInitialized.current) return;
        filtersInitialized.current = true;
        const aid = searchParams.get("artifactId");
        const label = searchParams.get("artifactName");
        if (aid) {
            setArtifactId(Number(aid));
            setArtifactLabel(label);
        }
    }, [searchParams]);

    useEffect(() => {
        const timer = setTimeout(() => setDebouncedSearch(search), 400);
        return () => clearTimeout(timer);
    }, [search]);

    function resetPage() { setPage(0); }

    /* ===================== LOAD ===================== */

    useEffect(() => {
        if (mode === "user" && !account) return;

        let cancelled = false;

        async function load() {
            try {
                setLoading(true);
                setError(null);

                const data =
                    mode === "market"
                        ? await getListings(page, pageSize, artifactId, debouncedSearch, sort, minPrice, maxPrice)
                        : await getUserListings(page, pageSize);

                if (!cancelled) setListings(data);

            } catch {
                if (!cancelled) setError("Failed to load listings");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();

        return () => { cancelled = true; };

    }, [account, page, mode, artifactId, debouncedSearch, sort, minPrice, maxPrice]);

    useEffect(() => {
        localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));
    }, [collapsed]);

    function handleSwitch(newMode: "market" | "user") {
        if (newMode === mode) return;
        setMode(newMode);
        setPage(0);
        setListings(null);
    }

    /* ===================== OPEN MODAL ===================== */

    async function openListing(listing: ListingView) {
        setSelectedListing(listing);
        setPriceHistory([]);
        setLoadingHistory(true);
        setMessage(null);

        try {
            const history = await getArtifactPriceHistory(listing.artifactUnitId);
            setPriceHistory(history);
        } finally {
            setLoadingHistory(false);
        }
    }

    /* ===================== BUY ===================== */

    async function handleBuy() {
        if (!selectedListing) return;

        if (!account) {
            setAuthModalOpen(true);
            return;
        }

        if (account.balance < selectedListing.price) {
            setMessage({type: "error", text: "Insufficient balance"});
            return;
        }

        try {
            await buyArtifactUnit(selectedListing.id);
            setMessage({type: "success", text: "Purchase successful"});
            queryClient.invalidateQueries({queryKey: ["account"]});

            setListings(prev => ({
                ...prev!,
                items: prev!.items.filter(i => i.id !== selectedListing.id)
            }));

            setTimeout(() => setSelectedListing(null), 800);

        } catch {
            setMessage({type: "error", text: "Purchase failed"});
        }
    }

    /* ===================== UI ===================== */

    const sortButton = (label: string, value: SortOption) => (
        <button
            onClick={() => { setSort(value); resetPage(); }}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition ${
                sort === value
                    ? "bg-zinc-900 text-white"
                    : "bg-white border border-slate-200 text-zinc-600 hover:bg-slate-50"
            }`}
        >
            {label}
        </button>
    );

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

                {/* ===================== SWITCH ===================== */}

                <div className="mb-4 flex gap-4">
                    <div className="relative flex bg-slate-200 rounded-full p-1 w-[260px]">

                        <div
                            className={`absolute top-1 bottom-1 rounded-full bg-[#0f172a] transition-all duration-300 ${
                                mode === "market" ? "left-1 w-[calc(50%-4px)]" : "left-1/2 w-[calc(50%-4px)]"
                            }`}
                        />

                        <button
                            onClick={() => handleSwitch("market")}
                            className={`relative z-10 flex-1 py-2 text-sm font-semibold transition ${
                                mode === "market" ? "text-white" : "text-slate-500"
                            }`}
                        >
                            Marketplace
                        </button>

                        <button
                            onClick={() => account ? handleSwitch("user") : setAuthModalOpen(true)}
                            className={`relative z-10 flex-1 py-2 text-sm font-semibold transition ${
                                mode === "user" ? "text-white" : "text-slate-500"
                            }`}
                        >
                            My Listings
                        </button>

                    </div>
                </div>

                {/* ===================== FILTERS (market mode only) ===================== */}

                {mode === "market" && (
                    <div className="mb-5 flex flex-wrap items-center gap-3">

                        {/* Artifact chip */}
                        {artifactId != null && (
                            <div className="flex items-center gap-1.5 bg-zinc-900 text-white text-xs font-medium px-3 py-1.5 rounded-full">
                                <span>{artifactLabel ?? `#${artifactId}`}</span>
                                <button
                                    onClick={() => { setArtifactId(null); setArtifactLabel(null); resetPage(); }}
                                    className="ml-1 hover:text-zinc-300 transition-colors"
                                    aria-label="Clear artifact filter"
                                >
                                    ×
                                </button>
                            </div>
                        )}

                        {/* Search */}
                        <input
                            type="text"
                            value={search}
                            onChange={e => { setSearch(e.target.value); resetPage(); }}
                            placeholder="Search by name..."
                            className="border border-slate-200 rounded-lg px-3 py-1.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-slate-300 w-48"
                        />

                        {/* Sort */}
                        <div className="flex gap-1.5">
                            {sortButton("Newest", "newest")}
                            {sortButton("Price ↑", "price_asc")}
                            {sortButton("Price ↓", "price_desc")}
                        </div>

                        {/* Price range */}
                        <div className="flex items-center gap-1.5">
                            <input
                                type="number"
                                value={minPrice}
                                onChange={e => { setMinPrice(e.target.value); resetPage(); }}
                                placeholder="Min $"
                                min="0"
                                className="border border-slate-200 rounded-lg px-3 py-1.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-slate-300 w-24"
                            />
                            <span className="text-slate-400 text-sm">—</span>
                            <input
                                type="number"
                                value={maxPrice}
                                onChange={e => { setMaxPrice(e.target.value); resetPage(); }}
                                placeholder="Max $"
                                min="0"
                                className="border border-slate-200 rounded-lg px-3 py-1.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-slate-300 w-24"
                            />
                        </div>

                        {/* Clear all */}
                        {(search || sort !== "newest" || minPrice || maxPrice || artifactId != null) && (
                            <button
                                onClick={() => {
                                    setArtifactId(null); setArtifactLabel(null);
                                    setSearch(""); setSort("newest");
                                    setMinPrice(""); setMaxPrice("");
                                    resetPage();
                                }}
                                className="text-xs text-zinc-400 hover:text-zinc-700 transition-colors underline"
                            >
                                Clear filters
                            </button>
                        )}

                    </div>
                )}

                {/* ===================== GRID ===================== */}

                {loading && <p className="text-center">Loading...</p>}
                {error && <p className="text-center text-red-500">{error}</p>}

                {!loading && !error && listings && (

                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">

                        {listings.items.map((item) => (

                            <div
                                key={item.id}
                                onClick={() => openListing(item)}
                                className="rounded-xl border border-slate-200 bg-white shadow-sm
                                hover:shadow-md hover:scale-[1.02] transition
                                p-5 cursor-pointer flex flex-col items-center justify-center text-center min-h-27"
                            >
                                <span className="text-slate-800 font-semibold">{item.artifactName}</span>
                                <div className="mt-1 text-xs text-slate-500">
                                    Artifact #{item.artifactId} • Unity #{item.artifactUnitId} •{" "}
                                    {new Date(item.createdAt).toLocaleDateString()}
                                </div>
                                <span className="mt-2 text-emerald-500 font-bold text-sm">
                                    ${item.price.toFixed(2)}
                                </span>
                            </div>

                        ))}

                    </div>

                )}

                <Pagination
                    page={page}
                    totalPages={listings?.totalPages ?? 0}
                    onPageChange={setPage}
                />

            </main>

            {/* ===================== MODAL ===================== */}

            {authModalOpen && <AuthRequiredModal onClose={() => setAuthModalOpen(false)}/>}

            {selectedListing && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">

                    <div className="bg-white rounded-2xl w-[440px] p-6 shadow-xl">

                        <h2 className="text-xl font-bold mb-2">
                            {mode === "market" ? "Buy Artifact" : "Manage Listing"}
                        </h2>

                        <div className="text-left mb-4">
                            <div className="text-slate-800 font-semibold">{selectedListing.artifactName}</div>
                            <div className="text-xs text-slate-500 mb-1">
                                Artifact #{selectedListing.artifactId} • Unity #{selectedListing.artifactUnitId} •{" "}
                                {new Date(selectedListing.createdAt).toLocaleDateString()}
                            </div>
                            <span className="text-emerald-500 font-bold text-lg">
                                ${selectedListing.price.toFixed(2)}
                            </span>
                        </div>

                        <div className="mb-5">
                            <h3 className="text-sm font-semibold mb-2">Price History</h3>
                            {loadingHistory ? (
                                <p className="text-sm text-slate-500">Loading history...</p>
                            ) : (
                                <PriceHistoryChart priceHistory={priceHistory}/>
                            )}
                        </div>

                        {mode === "market" && account && (
                            <div className="mb-8">
                                <div className="text-sm font-semibold mb-2">Purchase summary</div>
                                <div className="flex justify-between text-slate-500 text-sm">
                                    <span>Balance</span>
                                    <span>${account.balance.toFixed(2)}</span>
                                </div>
                                <div className="flex justify-between text-slate-500 text-sm">
                                    <span>Item price</span>
                                    <span>- ${selectedListing.price.toFixed(2)}</span>
                                </div>
                                <div className="border-t border-slate-300 mt-2 pt-2 flex justify-between text-sm text-slate-800">
                                    <span>Remaining</span>
                                    <span>${(account.balance - selectedListing.price).toFixed(2)}</span>
                                </div>
                            </div>
                        )}

                        <div className="flex justify-end gap-3">

                            <button
                                onClick={() => setSelectedListing(null)}
                                className="px-4 py-2 border rounded-lg text-black hover:bg-slate-900 hover:text-white transition"
                            >
                                Close
                            </button>

                            {mode === "market" ? (
                                <button
                                    onClick={handleBuy}
                                    disabled={!!account && account.balance < selectedListing.price}
                                    className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-500 disabled:opacity-50 transition"
                                >
                                    {account ? "Confirm Purchase" : "Sign in to buy"}
                                </button>
                            ) : (
                                <button
                                    onClick={async () => {
                                        try {
                                            await cancelOffer(selectedListing.id);
                                            setListings(prev => {
                                                if (!prev) return prev;
                                                return {...prev, items: prev.items.filter(l => l.id !== selectedListing.id)};
                                            });
                                            setMessage({type: "success", text: "Listing canceled"});
                                            setTimeout(() => setSelectedListing(null), 800);
                                        } catch {
                                            setMessage({type: "error", text: "Failed to cancel listing"});
                                            setTimeout(() => setSelectedListing(null), 800);
                                        }
                                    }}
                                    className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-500 transition"
                                >
                                    Cancel Offer
                                </button>
                            )}

                        </div>

                        {message && (
                            <div className={`mt-4 text-sm px-3 py-2 rounded-md ${
                                message.type === "error" ? "bg-red-100 text-red-600" : "bg-emerald-100 text-emerald-600"
                            }`}>
                                {message.text}
                            </div>
                        )}

                    </div>
                </div>
            )}
        </div>
    );
}

export default Marketplace;
