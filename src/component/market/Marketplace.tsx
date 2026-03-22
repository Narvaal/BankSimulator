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

    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });

    /* ===================== LOAD ===================== */

     useEffect(() => {

         if (!account) return;

         localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));

         async function load() {

             setLoading(true);
             setError(null);

             try {

                 const data =
                     mode === "market"
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

     }, [account, page, collapsed, mode]);

    function handleSwitch(newMode: "market" | "user") {
        setMode(newMode);
        setPage(0);
        setListings(null);
    }

    /* ===================== OPEN MODAL ===================== */

    async function openListing(listing: ListingView) {
        setSelectedListing(listing);
        setPriceHistory([]);
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

    if (authLoading) return <div className="p-10 text-center">Checking session...</div>;
    if (authError || !account) return <div className="p-10 text-center text-red-500">Not authenticated</div>;
    if (error) return <div className="p-10 text-center text-red-500">{error}</div>;

    return (
        <div className="min-h-screen bg-slate-100">

            <NavBar collapsed={collapsed} setCollapsed={setCollapsed}/>

            <UserMenu
                balance={account.balance}
                nextFreeAssetAt={account.nextFreeAssetAt}
                name={account.name}
                imageUrl={account.picture}
            />

            <main
                className="pt-22 p-6 transition-all duration-300"
                style={{ marginLeft: collapsed ? 80 : 256 }}
            >

                {/* ===================== SWITCH ===================== */}

                <div className="flex gap-3 mb-6">

                    <button
                        onClick={() => handleSwitch("market")}
                        className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
                            mode === "market"
                                ? "bg-[#0f172a] text-white"
                                : "bg-white text-slate-500 hover:bg-slate-200"
                        }`}
                    >
                        Marketplace
                    </button>

                    <button
                        onClick={() => handleSwitch("user")}
                        className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
                            mode === "user"
                                ? "bg-[#0f172a] text-white"
                                : "bg-white text-slate-500 hover:bg-slate-200"
                        }`}
                    >
                        My Listings
                    </button>

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
              <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">

                <div className="bg-white rounded-2xl w-[440px] p-6 shadow-xl">

                  <h2 className="text-xl font-bold mb-2">
                    {mode === "market" ? "Buy Asset" : "Manage Listing"}
                  </h2>

                  <div className="text-left mb-4">

                    <div className="text-slate-800 font-semibold">
                      {selectedListing.assetText}
                    </div>

                    <div className="text-xs text-slate-500 mb-1">
                      Asset #{selectedListing.assetId} • Unity #{selectedListing.assetUnityId} •{" "}
                      {new Date(selectedListing.createdAt).toLocaleDateString()}
                    </div>

                    <span className="text-emerald-500 font-bold text-lg">
                      ${selectedListing.price.toFixed(2)}
                    </span>

                  </div>

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

                  {mode === "market" && (
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

                      <div className="border-t border-slate-300 mt-2 pt-2 flex justify-between text-sm text-slate-800">
                        <span>Remaining</span>
                        <span>
                          ${(account.balance - selectedListing.price).toFixed(2)}
                        </span>
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
                        disabled={account.balance < selectedListing.price}
                        className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-500 disabled:opacity-50 transition"
                      >
                        Confirm Purchase
                      </button>
                    ) : (
                      <button
                        onClick={async () => {
                          try {
                            await cancelOffer(selectedListing.id);

                            setListings(prev => {
                              if (!prev) return prev;
                              return {
                                ...prev,
                                items: prev.items.filter(l => l.id !== selectedListing.id)
                              };
                            });

                            setSelectedListing(null);

                          } catch {
                            alert("Failed to cancel listing");
                          }
                        }}
                        className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-500 transition"
                      >
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