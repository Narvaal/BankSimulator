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

async function getListings(page: number, pageSize: number): Promise<assetListingPageView> {
    const res = await fetch(`https://BankSimulator.us-east-2.elasticbeanstalk.com/asset-listings?page=${page}&pageSize=${pageSize}`, {
        method: "GET",
        credentials: "include"
    });

    if (!res.ok) throw new Error("Failed to load listings");

    return res.json();
}

async function getAssetPriceHistory(assetUnityId: number): Promise<AssetPriceHistory[]> {
    const res = await fetch(
        `https://BankSimulator.us-east-2.elasticbeanstalk.com/assets/${assetUnityId}/price-history`
    );

    if (!res.ok) throw new Error("Failed to load price history");

    return res.json();
}

async function buyAssetUnity(assetListingId: number) {
    const res = await fetch(
        `https://BankSimulator.us-east-2.elasticbeanstalk.com/asset-listings/${assetListingId}/purchase`,
        {method: "POST", credentials: "include"}
    );

    if (!res.ok) throw new Error("Failed to buy asset");
    return res.json();
}

/* ===================== CARD ===================== */

function ListingCard({
                         listing,
                         onOpen
                     }: {
    listing: ListingView;
    onOpen: (listing: ListingView) => void;
}) {

    return (
        <div
            onClick={() => onOpen(listing)}
            className="bg-white rounded-xl border border-slate-200 shadow-sm
            hover:shadow-lg hover:scale-[1.02] transition
            p-5 flex flex-col gap-3 cursor-pointer"
        >

            <div className="text-center">

                <span className="text-slate-800 font-semibold">
                    {listing.assetText}
                </span>

                <div className="mt-1 text-xs text-slate-500">
                    Asset #{listing.assetId} • Unity
                    #{listing.assetUnityId} • {new Date(listing.createdAt).toLocaleDateString()}
                </div>

            </div>

            <span className="text-emerald-500 font-bold text-lg text-center">
                    ${listing.price.toFixed(2)}
            </span>

        </div>
    );
}

/* ===================== PAGE ===================== */

function Marketplace() {

    const {data: account, isLoading: authLoading, error: authError} = useAccount();
    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });

    const [listings, setListings] = useState<assetListingPageView | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [selectedListing, setSelectedListing] = useState<ListingView | null>(null);
    const [priceHistory, setPriceHistory] = useState<AssetPriceHistory[]>([]);
    const [loadingHistory, setLoadingHistory] = useState(false);

    const [page, setPage] = useState(0);
    const pageSize = 24;

    /* ===================== LOAD MARKET ===================== */

    useEffect(() => {

        if (!account) return;

        localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));

        async function load() {

            try {

                const listingsData = await getListings(page, pageSize);
                setListings(listingsData);

            } catch {

                setError("Failed to load marketplace");

            } finally {

                setLoading(false);

            }
        }

        load();

    }, [account, page, collapsed]);

    /* ===================== OPEN MODAL ===================== */

    async function openListing(listing: ListingView) {

        setSelectedListing(listing);
        setPriceHistory([]);
        setLoadingHistory(true);

        try {

            const history = await getAssetPriceHistory(listing.assetUnityId);
            setPriceHistory(history);

        } catch {

            setPriceHistory([]);

        } finally {

            setLoadingHistory(false);

        }
    }

    /* ===================== BUY ===================== */

    async function handleBuy() {

        if (!selectedListing) return;

        try {

            await buyAssetUnity(selectedListing.id);

            setListings((prev) => {

                if (!prev) return prev;

                return {
                    ...prev,
                    items: prev.items.filter(
                        (l) => l.id !== selectedListing.id
                    )
                };

            });

            setSelectedListing(null);

        } catch {

            alert("Purchase failed");

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

                    {!loading && !error && listings && (

                        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">

                            {listings.items.map((item) => (

                                <ListingCard
                                    key={item.id}
                                    listing={item}
                                    onOpen={openListing}
                                />

                            ))}

                        </div>

                    )}

                </div>

                <div className="mt-auto pt-10">
                    <Pagination
                        page={page}
                        totalPages={listings?.totalPages ?? 0}
                        onPageChange={setPage}
                    />
                </div>

            </main>


            {/* ===================== MODAL ===================== */}

            {selectedListing && (

                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">

                    <div className="bg-white rounded-2xl w-110 p-6 shadow-xl">

                        <h2 className="text-xl font-bold mb-2">
                            Buy Asset
                        </h2>

                        <div className="text-left mb-4">
                            <div className="text-slate-800 font-semibold">
                                {selectedListing.assetText}
                            </div>

                            <div className="text-xs text-slate-500 mb-1">
                                Asset #{selectedListing.assetId} • Unity
                                #{selectedListing.assetUnityId} • {new Date(selectedListing.createdAt).toLocaleDateString()}
                            </div>

                            <span className="text-emerald-500 font-bold text-lg">
                                    ${selectedListing.price.toFixed(2)}
                            </span>
                        </div>

                        {/* PRICE HISTORY */}

                        <div className="mb-5">

                            <h3 className="text-sm font-semibold mb-2">
                                Price History
                            </h3>

                            {loadingHistory ? (

                                <p className="text-sm text-slate-500">
                                    Loading history...
                                </p>

                            ) : (

                                <PriceHistoryChart priceHistory={priceHistory}/>

                            )}

                        </div>

                        {/* Balance resume */}

                        <div className="mb-8">
                            <div className="text-sm font-semibold mb-2">
                                Purchase summary
                            </div>

                            <div className="flex justify-between text-slate-500 text-sm">
                                <span>Balance</span>
                                <span>${account.balance.toFixed(2)}</span>
                            </div>

                            <div className="flex justify-between text-slate-500 text-sm">
                                <span>Item price</span>
                                <span>- ${selectedListing.price.toFixed(2)}</span>
                            </div>

                            <div className="border-t border-slate-500  flex justify-between text-sm text-slate-800">
                                <span>Remaining</span>
                                <span>
                                    ${(account.balance - selectedListing.price).toFixed(2)}
                                </span>
                            </div>

                        </div>

                        {/* BUTTONS */}

                        <div className="flex justify-end gap-3">

                            <button
                                onClick={() => setSelectedListing(null)}
                                className="px-4 py-2 border rounded-lg text-black hover:bg-slate-900 hover:text-white"
                            >
                                Cancel
                            </button>

                            <button
                                onClick={handleBuy}
                                disabled={account.balance < selectedListing.price}
                                className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-500 disabled:opacity-50"
                            >
                                Confirm Purchase
                            </button>

                        </div>

                    </div>

                </div>

            )}

        </div>
    );
}

export default Marketplace;