import {useEffect, useState} from "react";
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
    artifactText: string;
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

/* ===================== API ===================== */

async function getListings(page: number, pageSize: number) {
    const res = await fetch(`${API_URL}/artifact-listings?page=${page}&pageSize=${pageSize}`);
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

    const [message, setMessage] = useState<{
        type: "success" | "error";
        text: string;
    } | null>(null);

    const [authModalOpen, setAuthModalOpen] = useState(false);

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
                        ? await getListings(page, pageSize)
                        : await getUserListings(page, pageSize);

                if (!cancelled) setListings(data);

            } catch {
                if (!cancelled) setError("Failed to load listings");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();

        return () => {
            cancelled = true;
        };

    }, [account, page, mode]);

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

                <div className="mb-6 flex gap-4">
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
                                <span className="text-slate-800 font-semibold">{item.artifactText}</span>
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
                            <div className="text-slate-800 font-semibold">{selectedListing.artifactText}</div>
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
