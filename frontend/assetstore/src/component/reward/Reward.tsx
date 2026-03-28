/* ===================== IMPORTS ===================== */

import NavBar from "../navBar/NavBar";
import {useCallback, useEffect, useState} from "react";
import UserMenu from "../usermenu/UserMenu.tsx";
import {useAccount} from "../auth/Auth";
import { useQueryClient } from "@tanstack/react-query";

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
        `https://api.alessandro-bezerra.me/assets/bundles?page=${page}&size=${size}`
    );

    if (!res.ok) throw new Error("Failed to load bundles");

    return res.json();
}

async function getBundleAssets(id: string): Promise<Asset[]> {

    const res = await fetch(
        `https://api.alessandro-bezerra.me/assets/bundles/${id}/items?page=0&size=20`
    );

    if (!res.ok) throw new Error("Failed to load assets of bundle");

    return res.json();
}

async function claimAsset(assetId: number) {

    const res = await fetch("https://api.alessandro-bezerra.me/assets/claim", {
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
    const queryClient = useQueryClient();

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


    /* ===================== MODAL TIME ===================== */

    const [nextDate, setNextDate] = useState(getNextBundleDateUTC());
    const [timeLeft, setTimeLeft] = useState("");


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

    useEffect(() => {
        function updateTimer() {
            const now = new Date().getTime();
            const distance = nextDate.getTime() - now;

            if (distance <= 0) {
                setTimeLeft("Available now");
                return;
            }

            const hours = Math.floor((distance / (1000 * 60 * 60)) % 24);
            const minutes = Math.floor((distance / (1000 * 60)) % 60);
            const seconds = Math.floor((distance / 1000) % 60);

            setTimeLeft(`${hours}h ${minutes}m ${seconds}s`);
        }

        updateTimer();

        const interval = setInterval(updateTimer, 1000);

        return () => clearInterval(interval);
    }, [nextDate]);

    useEffect(() => {
        const interval = setInterval(() => {
            setNextDate(getNextBundleDateUTC());
        }, 60000);

        return () => clearInterval(interval);
    }, []);

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

    function getNextBundleDateUTC() {
        const now = new Date();

        const next = new Date();
        next.setUTCHours(8, 0, 0, 0);

        if (now.getTime() >= next.getTime()) {
            next.setUTCDate(next.getUTCDate() + 1);
        }

        return next;
    }

    /* ===================== CLAIM ASSET ===================== */

    async function handleClaim() {

        if (!selectedAsset) return;

        try {

            setLoadingClaim(true);

            await claimAsset(selectedAsset.id);

            queryClient.invalidateQueries({ queryKey: ["account"] });

            setBundleAssets(prev => {
                const updated = { ...prev };

                for (const bundleId in updated) {
                    updated[bundleId] = updated[bundleId].map(asset => {
                        if (asset.id === selectedAsset.id) {
                            return {
                                ...asset,
                                totalSupply: Math.max(0, asset.totalSupply - 1)
                            };
                        }
                        return asset;
                    });
                }

                return updated;
            });

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

                {/* ===================== INFO ===================== */}

                <div className="mb-6 bg-yellow-50 border border-yellow-300 rounded-xl p-4 shadow-sm">

                    <div className="flex items-start gap-3">

                        <div className="text-yellow-600 mt-0.5">
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                fill="none"
                                viewBox="0 0 24 24"
                                strokeWidth={1.5}
                                stroke="currentColor"
                                className="w-5 h-5"
                            >
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M12 9v3.75m9-.75a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 3.75h.008v.008H12v-.008Z"
                                />
                            </svg>
                        </div>

                        <div>

                            <p className="text-sm text-yellow-800 font-medium mb-1">
                                Each asset bundle is automatically generated at a predefined time.
                                Once available, assets can be claimed until the supply runs out.
                            </p>

                            <p className="text-xs text-yellow-700">
                                Disclaimer: Generated phrases may contain inconsistencies or inaccuracies.
                                We are not responsible for any unintended meanings.
                            </p>

                        </div>

                    </div>

                </div>

                {/* ===================== UPCOMING BUNDLE ===================== */}

                <div className="mb-6">

                    <div className="bg-gradient-to-r from-slate-900 to-slate-700 text-white rounded-xl p-6 shadow-lg border border-slate-800">

                        <div className="flex justify-between items-center">

                            <div>

                                <h2 className="text-xl font-semibold mb-1">
                                    🔒 Upcoming Bundle
                                </h2>

                                <p className="text-sm text-slate-300">
                                    This bundle is locked and will be available soon
                                </p>

                            </div>

                            <div className="text-right">

                                <p className="text-xs text-slate-400 mb-1">
                                    Releases in
                                </p>

                                <div className="text-lg font-bold text-emerald-400">
                                    {timeLeft}
                                </div>

                            </div>

                        </div>

                        <div className="mt-4 text-xs text-slate-400">
                            Release date: {nextDate.toLocaleString()}
                        </div>

                    </div>

                </div>

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