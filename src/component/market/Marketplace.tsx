import {useEffect, useState} from "react";
import NavBar from "../navBar/NavBar";
import PriceHistoryChart from "../market/PriceHistoryChart";
import {useAccount} from "../auth/Auth";
import Pagination from "../util/Pagination.tsx";
import UserMenu from "../usermenu/UserMenu.tsx";

/* ===================== TYPES ===================== */

interface AssetPriceHistory {
    assetId: number;
    assetUnityId: number;
    oldPrice: number;
    newPrice: number;
    createdAt: string;
}

interface ListingView {
    id: number;
    assetUnityId: number;
    assetId: number;
    assetText: string;
    price: number;
    createdAt: string;
}

interface assetListingPageView {
    items: ListingView[]
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
}

/* ===================== API ===================== */

async function getListings(page: number, pageSize: number) {
    const res = await fetch(`https://api.alessandro-bezerra.me/asset-listings?page=${page}&pageSize=${pageSize}`, {
        credentials: "include"
    });
    if (!res.ok) throw new Error();
    return res.json();
}

async function getUserListings(page: number, pageSize: number) {
    const res = await fetch(`https://api.alessandro-bezerra.me/asset-listings/me?page=${page}&pageSize=${pageSize}`, {
        credentials: "include"
    });
    if (!res.ok) throw new Error();
    return res.json();
}

async function cancelOffer(assetListingId: number) {
    const res = await fetch(`https://api.alessandro-bezerra.me/asset-listings/${assetListingId}/cancel`, {
        method: "POST",
        credentials: "include"
    });
    if (!res.ok) throw new Error();
    return res.json();
}

async function getAssetPriceHistory(assetUnityId: number) {
    const res = await fetch(
        `https://api.alessandro-bezerra.me/assets/${assetUnityId}/price-history`
    );
    if (!res.ok) throw new Error();
    return res.json();
}

async function buyAssetUnity(assetListingId: number) {
    const res = await fetch(
        `https://api.alessandro-bezerra.me/asset-listings/${assetListingId}/purchase`,
        {method: "POST", credentials: "include"}
    );
    if (!res.ok) throw new Error();
    return res.json();
}

/* ===================== PAGE ===================== */

function Marketplace() {

    const {data: account, isLoading: authLoading, error: authError} = useAccount();

    const [mode, setMode] = useState<"market" | "user">("market");

    const [listings, setListings] = useState<assetListingPageView | null>(null);
    const [selectedListing, setSelectedListing] = useState<ListingView | null>(null);

    const [priceHistory, setPriceHistory] = useState<AssetPriceHistory[]>([]);
    const [loadingHistory, setLoadingHistory] = useState(false);

    const [page, setPage] = useState(0);
    const pageSize = 24;

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    /* ===================== LOAD ===================== */

    useEffect(() => {

        if (!account) return;

        setLoading(true);

        async function load() {
            try {

                const data = mode === "market"
                    ? await getListings(page, pageSize)
                    : await getUserListings(page, pageSize);

                setListings(data);

            } catch {
                setError("Failed to load listings");
            } finally {
                setLoading(false);
            }
        }

        load();

    }, [account, page, mode]);

    /* ===================== OPEN MODAL ===================== */

    async function openListing(listing: ListingView) {
        setSelectedListing(listing);
        setLoadingHistory(true);

        try {
            const history = await getAssetPriceHistory(listing.assetUnityId);
            setPriceHistory(history);
        } finally {
            setLoadingHistory(false);
        }
    }

    /* ===================== BUY ===================== */

    async function handleBuy() {
        if (!selectedListing) return;

        try {
            await buyAssetUnity(selectedListing.id);

            setListings(prev => ({
                ...prev!,
                items: prev!.items.filter(i => i.id !== selectedListing.id)
            }));

            setSelectedListing(null);

        } catch {
            alert("Purchase failed");
        }
    }

    /* ===================== CANCEL ===================== */

    async function handleCancel() {
        if (!selectedListing) return;

        try {
            await cancelOffer(selectedListing.id);

            setListings(prev => ({
                ...prev!,
                items: prev!.items.filter(i => i.id !== selectedListing.id)
            }));

            setSelectedListing(null);

        } catch {
            alert("Cancel failed");
        }
    }

    if (authLoading) return <div className="p-10 text-center">Checking session...</div>;
    if (authError || !account) return <div className="p-10 text-center text-red-500">Not authenticated</div>;

    return (
        <div className="min-h-screen bg-slate-100">

            <NavBar collapsed={false} setCollapsed={() => {}}/>

            <UserMenu
                balance={account.balance}
                nextFreeAssetAt={account.nextFreeAssetAt}
                name={account.name}
                imageUrl={account.picture}
            />

            <main className="pt-22 p-6">

                {/* ===================== SWITCH ===================== */}

                <div className="flex justify-center mb-6">
                    <div className="bg-white rounded-lg p-1 shadow flex">

                        <button
                            onClick={() => { setMode("market"); setPage(0); }}
                            className={`px-4 py-2 rounded-md text-sm font-semibold transition ${
                                mode === "market"
                                    ? "bg-slate-900 text-white"
                                    : "text-slate-500"
                            }`}
                        >
                            Marketplace
                        </button>

                        <button
                            onClick={() => { setMode("user"); setPage(0); }}
                            className={`px-4 py-2 rounded-md text-sm font-semibold transition ${
                                mode === "user"
                                    ? "bg-slate-900 text-white"
                                    : "text-slate-500"
                            }`}
                        >
                            My Listings
                        </button>

                    </div>
                </div>

                {/* ===================== GRID ===================== */}

                {loading && <p className="text-center">Loading...</p>}

                {listings && (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                        {listings.items.map(item => (
                            <div key={item.id} onClick={() => openListing(item)}
                                 className="bg-white p-4 rounded-xl cursor-pointer hover:shadow">
                                <div>{item.assetText}</div>
                                <div className="text-emerald-500">${item.price}</div>
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

            {selectedListing && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center">

                    <div className="bg-white p-6 rounded-xl w-[400px]">

                        <h2 className="text-xl font-bold mb-4">
                            {mode === "market" ? "Buy Asset" : "Manage Listing"}
                        </h2>

                        <div className="mb-4">
                            {selectedListing.assetText}
                        </div>

                        {loadingHistory
                            ? <p>Loading...</p>
                            : <PriceHistoryChart priceHistory={priceHistory}/>
                        }

                        <div className="flex justify-end gap-3 mt-6">

                            <button onClick={() => setSelectedListing(null)}>
                                Close
                            </button>

                            {mode === "market" ? (
                                <button onClick={handleBuy} className="bg-emerald-600 text-white px-4 py-2 rounded">
                                    Buy
                                </button>
                            ) : (
                                <button onClick={handleCancel} className="bg-red-600 text-white px-4 py-2 rounded">
                                    Cancel Offer
                                </button>
                            )}

                        </div>

                    </div>

                </div>
            )}

        </div>
    );
}

export default Marketplace;