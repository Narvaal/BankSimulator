import { useEffect, useState } from "react";
import NavBar from "../navBar/NavBar";

/* ===================== TYPES ===================== */

interface AssetListing {
    id: number;
    sellerAccountId: number;
    price: number;
    status: string;
    assetUnityId: number;
    createdAt: string;
    updatedAt: string;
}

interface Asset {
    id: number;
    text: string;
    totalSupply: number;
    createdAt: string;
}

interface ListingView {
    listingId: number;
    text: string;
    price: number;
    createdAt: string;
}

/* ===================== API ===================== */

async function getListings(): Promise<AssetListing[]> {
    const res = await fetch("http://localhost:8080/asset-listings");
    if (!res.ok) throw new Error("Failed to load listings");
    return res.json();
}

async function getAsset(assetId: number): Promise<Asset> {
    const res = await fetch(`http://localhost:8080/assets/${assetId}`);
    if (!res.ok) throw new Error("Failed to load asset");
    return res.json();
}

async function buyAssetUnity(assetListingId: number): Promise<Asset> {
    const res = await fetch(`http://localhost:8080/asset-listings/${assetListingId}/purchase`,
        { method: "POST", credentials: "include" });
    if (!res.ok) throw new Error("Failed to buy asset");
    return res.json();
}

/* ===================== CARD ===================== */

function ListingCard({ listing }: { listing: ListingView }) {
    return (
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-lg hover:scale-[1.02] transition p-5 flex flex-col gap-3">

            <div className="text-center">
                <div className="text-slate-800 font-semibold text-lg">
                    {listing.text}
                </div>

                <div className="text-xs text-slate-400 mt-1">
                    {new Date(listing.createdAt).toLocaleDateString()}
                </div>
            </div>

            <div className="mt-2 flex items-center justify-between">
                <span className="text-emerald-600 font-bold text-lg">
                    ${listing.price.toFixed(2)}
                </span>

                <button className="px-3 py-1.5 rounded-md bg-slate-900 text-white text-sm hover:bg-slate-700"
                onClick={() => buyAssetUnity(listing.listingId)}
                >
                    Buy
                </button>
            </div>
        </div>
    );
}

/* ===================== PAGE ===================== */

function Marketplace() {
    const [collapsed, setCollapsed] = useState(false);
    const [listings, setListings] = useState<ListingView[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function load() {
            try {
                const listingsData = await getListings();

                const enriched = await Promise.all(
                    listingsData.map(async (l) => {
                        const asset = await getAsset(l.assetUnityId);

                        return {
                            listingId: l.id,
                            text: asset.text,
                            price: l.price,
                            createdAt: l.createdAt,
                        };
                    })
                );

                setListings(enriched);
            } catch {
                setError("Failed to load marketplace");
            } finally {
                setLoading(false);
            }
        }

        load();
    }, []);

    return (
        <div className="min-h-screen bg-slate-100">
            <NavBar collapsed={collapsed} setCollapsed={setCollapsed} />

            <main
                className="transition-all duration-300 p-6"
                style={{ marginLeft: collapsed ? 80 : 256 }}
            >
                <h1 className="text-2xl font-bold text-center mb-8">
                    Marketplace
                </h1>

                {loading && <p className="text-center text-slate-500">Loading...</p>}
                {error && <p className="text-center text-red-500">{error}</p>}

                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                    {listings.map((listing) => (
                        <ListingCard key={listing.listingId} listing={listing} />
                    ))}
                </div>
            </main>
        </div>
    );
}

export default Marketplace;
