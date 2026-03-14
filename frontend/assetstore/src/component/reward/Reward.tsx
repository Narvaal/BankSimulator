/* ===================== IMPORTS ===================== */

import NavBar from "../navBar/NavBar";
import {useCallback, useEffect, useState} from "react";
import UserMenu from "../usermenu/UserMenu.tsx";
import {useAccount} from "../auth/Auth";


/* ===================== TYPES ===================== */

interface Bundle {
    id: string
    identifier: string
    createdAt: string
}

interface Asset {
    id: number
    text: string
    totalSupply: number
    createdAt: string
}


/* ===================== API ===================== */

async function getBundles(page: number, size: number): Promise<Bundle[]> {

    const res = await fetch(
        `http://localhost:8080/assets/bundles?page=${page}&size=${size}`
    );

    if (!res.ok) throw new Error("Failed to load bundles");

    return res.json();
}

async function getBundleAssets(id: string): Promise<Asset[]> {

    const res = await fetch(
        `http://localhost:8080/assets/bundles/${id}/items?page=0&size=20`
    );

    if (!res.ok) throw new Error("Failed to load assets of bundle");

    return res.json();
}

async function claimAsset(assetId: number) {

    const res = await fetch("http://localhost:8080/assets/claim", {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({assetId})
    });

    if (!res.ok) throw new Error("Failed to claim asset");

    return res.json();
}


/* ===================== PAGE ===================== */

function Reward() {

    const {data: account, isLoading: authLoading, error: authError} = useAccount();


    /* ===================== UI STATE ===================== */

    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });


    /* ===================== PAGE STATE ===================== */

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);


    /* ===================== DATA STATE ===================== */

    const [bundles, setBundles] = useState<Bundle[]>([]);
    const [bundleAssets, setBundleAssets] = useState<Record<string, Asset[]>>({});
    const [openBundles, setOpenBundles] = useState<Record<string, boolean>>({});


    /* ===================== PAGINATION ===================== */

    const [page, setPage] = useState(0);
    const [loadingBundles, setLoadingBundles] = useState(false);
    const [hasMore, setHasMore] = useState(true);

    const PAGE_SIZE = 8;


    /* ===================== MODAL STATE ===================== */

    const [selectedAsset, setSelectedAsset] = useState<Asset | null>(null);
    const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);
    const [loadingClaim, setLoadingClaim] = useState(false);


    /* ===================== LOAD BUNDLES ===================== */

    const loadBundles = useCallback(async () => {

        if (loadingBundles || !hasMore) return;
        
        localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));

        setLoading(false);
        setError(null);

        try {

            setLoadingBundles(true);

            const bundleData = await getBundles(page, PAGE_SIZE);

            if (bundleData.length === 0) {
                setHasMore(false);
                return;
            }

            setBundles(prev => {

                const existingIds = new Set(prev.map(b => b.id));
                const filtered = bundleData.filter(b => !existingIds.has(b.id));

                return [...prev, ...filtered];
            });

            if (page === 0 && bundleData.length > 0) {
                toggleBundle(bundleData[0].id, true);
            }

            setPage(prev => prev + 1);

        } finally {
            setLoadingBundles(false);
        }

    }, [page, loadingBundles, hasMore]);


    /* ===================== INITIAL LOAD ===================== */

    useEffect(() => {
        loadBundles();
    }, [collapsed]);


    /* ===================== INFINITE SCROLL ===================== */

    useEffect(() => {

        if (!account) return;

        let ticking = false;

        function handleScroll() {

            if (ticking) return;

            ticking = true;

            requestAnimationFrame(() => {

                if (
                    window.innerHeight + window.scrollY >=
                    document.body.offsetHeight - 300
                ) {
                    loadBundles();
                }

                ticking = false;

            });

        }

        window.addEventListener("scroll", handleScroll);

        return () => window.removeEventListener("scroll", handleScroll);

    }, [loadBundles]);


    /* ===================== TOGGLE BUNDLE ===================== */

    async function toggleBundle(bundleId: string, forceOpen = false) {

        const isOpen = openBundles[bundleId];
        const newState = forceOpen ? true : !isOpen;

        setOpenBundles(prev => ({
            ...prev,
            [bundleId]: newState
        }));

        if (newState && !bundleAssets[bundleId]) {

            try {

                const assets = await getBundleAssets(bundleId);

                setBundleAssets(prev => ({
                    ...prev,
                    [bundleId]: assets
                }));

            } catch {

                setBundleAssets(prev => ({
                    ...prev,
                    [bundleId]: []
                }));

            }

        }

    }


    /* ===================== MODAL CONTROLS ===================== */

    function openAsset(asset: Asset) {
        setMessage(null);
        setSelectedAsset(asset);
    }

    function closeModal() {
        setSelectedAsset(null);
        setMessage(null);
    }


    /* ===================== CLAIM ASSET ===================== */

    async function handleClaim() {

        if (!selectedAsset) return;

        try {

            setLoadingClaim(true);

            await claimAsset(selectedAsset.id);

            setMessage({
                type: "success",
                text: "Asset claimed successfully!"
            });

            setTimeout(() => {
                closeModal();
            }, 1500);

        } catch {

            setMessage({
                type: "error",
                text: "Failed to claim asset."
            });

        } finally {
            setLoadingClaim(false);
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

            <UserMenu
                balance={account.balance}
                nextFreeAssetAt={account.nextFreeAssetAt}
                name={account.name}
                imageUrl={account.picture}
            />

            {/* ===================== MAIN CONTENT ===================== */}

            <main
                className="pt-22 transition-all duration-300 p-6 min-h-screen flex flex-col"
                style={{marginLeft: collapsed ? 80 : 256}}
            >

                {/* ===================== LOADING / ERROR ===================== */}

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

                {/* ===================== BUNDLE LIST ===================== */}

                <div className="grid gap-6">

                    {bundles.map(bundle => {

                        const isOpen = openBundles[bundle.id];

                        return (

                            <div
                                key={bundle.id}
                                className="bg-white rounded-xl shadow-md border border-slate-200"
                            >

                                {/* BUNDLE HEADER */}

                                <div
                                    onClick={() => toggleBundle(bundle.id)}
                                    className="p-6 cursor-pointer flex justify-between items-center hover:bg-slate-50 rounded-xl"
                                >

                                    <div>

                                        <h2 className="text-xl font-semibold">
                                            {bundle.identifier}
                                        </h2>

                                        <p className="text-sm text-gray-500">
                                            Created at: {new Date(bundle.createdAt).toLocaleDateString()}
                                        </p>

                                    </div>

                                    <span className="text-gray-400">
                                        {isOpen ? "▲" : "▼"}
                                    </span>

                                </div>

                                {/* ===================== ASSET GRID ===================== */}

                                {isOpen && (

                                    <div className="p-6 pt-0">

                                        {!bundleAssets[bundle.id] && (
                                            <p className="text-gray-500 text-sm">
                                                Loading assets...
                                            </p>
                                        )}

                                        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">

                                            {bundleAssets[bundle.id]?.map((asset, index) => {

                                                const disabled = asset.totalSupply === 0;

                                                return (

                                                    <div
                                                        key={`${bundle.id}-${asset.id}-${index}`}
                                                        onClick={() => !disabled && openAsset(asset)}
                                                        className={`
                                                        border rounded-lg p-3 transition
                                                        ${disabled
                                                            ? "bg-gray-100 border-gray-200 text-gray-400 cursor-not-allowed"
                                                            : "cursor-pointer bg-slate-50 border-slate-200 hover:bg-slate-100"
                                                        }
                                                        `}
                                                    >

                                                        <p className="font-medium">
                                                            {asset.text}
                                                        </p>

                                                        <p className="text-xs">
                                                            {disabled ? "Sold Out" : `Supply: ${asset.totalSupply}`}
                                                        </p>

                                                    </div>

                                                );

                                            })}

                                        </div>

                                    </div>

                                )}

                            </div>

                        );

                    })}

                </div>

                {/* ===================== INFINITE SCROLL ===================== */}

                {loadingBundles && (
                    <p className="text-center mt-6 text-gray-500">
                        Loading more bundles...
                    </p>
                )}

            </main>


            {/* ===================== MODAL ===================== */}

            {selectedAsset && (

                <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50">

                    <div className="bg-white rounded-2xl p-7 w-96 shadow-2xl border border-slate-200">

                        <h2 className="text-2xl font-bold mb-5 text-slate-800">
                            Claim Asset
                        </h2>

                        {/* ASSET INFO */}

                        <div className="space-y-2 mb-6">

                            <p className="text-slate-700">
                                <span className="font-semibold">Name:</span> {selectedAsset.text}
                            </p>

                            <div className="grid grid-cols-2 gap-4">

                                <p className="text-sm text-slate-500">
                                    Supply: {selectedAsset.totalSupply}
                                </p>

                                <p className="text-sm text-slate-500">
                                    Created At: {new Date(selectedAsset.createdAt).toLocaleDateString()}
                                </p>

                            </div>

                        </div>

                        {/* MODAL BUTTONS */}

                        <div className="flex justify-end gap-3 mb-4">

                            <button
                                onClick={closeModal}
                                className="px-4 py-2 rounded-lg border border-slate-300 text-slate-600 hover:bg-slate-100 transition"
                            >
                                Cancel
                            </button>

                            <button
                                onClick={handleClaim}
                                disabled={loadingClaim}
                                className="px-4 py-2 rounded-lg bg-blue-600 text-white font-medium hover:bg-blue-700 transition disabled:opacity-50"
                            >
                                {loadingClaim ? "Claiming..." : "Confirm Claim"}
                            </button>

                        </div>

                        {/* MODAL MESSAGE */}

                        {message && (

                            <div
                                className={`mt-4 p-3 rounded-lg text-sm text-center font-medium
                                ${message.type === "success"
                                    ? "bg-green-100 text-green-700 border border-green-300"
                                    : "bg-red-100 text-red-700 border border-red-300"
                                }`}
                            >
                                {message.text}
                            </div>

                        )}

                    </div>

                </div>

            )}

        </div>
    );
}

export default Reward;