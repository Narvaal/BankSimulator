/* ===================== IMPORTS ===================== */

import NavBar from "../navBar/NavBar";
import {useCallback, useEffect, useState} from "react";
import UserMenu from "../usermenu/UserMenu.tsx";
import {useAccount} from "../auth/Auth";
import {useQueryClient} from "@tanstack/react-query";
import {API_URL} from "../../config";
import {authHeader} from "../../auth";
import {Link} from "react-router-dom";
import AuthRequiredModal from "../auth/AuthRequiredModal.tsx";
import {ArtifactCardThumb, ArtifactCardFullscreen, type CardMetadata} from "../artifact/ArtifactCard.tsx";

/* ===================== TYPES ===================== */

interface Bundle {
    id: string
    identifier: string
    createdAt: string
}

interface Artifact {
    id: number
    metadata: { name: string; rarity?: string; [key: string]: unknown }
    totalSupply: number
    createdAt: string
}


/* ===================== API ===================== */

async function getBundles(page: number, size: number): Promise<Bundle[]> {
    const res = await fetch(`${API_URL}/artifacts/bundles?page=${page}&size=${size}`);
    if (!res.ok) throw new Error("Failed to load bundles");
    return res.json();
}

async function getBundleAssets(id: string): Promise<Artifact[]> {
    const res = await fetch(`${API_URL}/artifacts/bundles/${id}/items?page=0&size=20`);
    if (!res.ok) throw new Error("Failed to load assets of bundle");
    return res.json();
}

async function claimAsset(artifactId: number) {
    const res = await fetch(`${API_URL}/artifacts/claim`, {
        method: "POST",
        credentials: "include",
        headers: {"Content-Type": "application/json", ...authHeader()},
        body: JSON.stringify({artifactId})
    });
    if (!res.ok) throw new Error("Failed to claim artifact");
    return res.json();
}


/* ===================== PAGE ===================== */

function Reward() {

    const {data: account} = useAccount();
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
    const [bundleAssets, setBundleAssets] = useState<Record<string, Artifact[]>>({});
    const [openBundles, setOpenBundles] = useState<Record<string, boolean>>({});


    /* ===================== PAGINATION ===================== */

    const [page, setPage] = useState(0);
    const [loadingBundles, setLoadingBundles] = useState(false);
    const [hasMore, setHasMore] = useState(true);

    const PAGE_SIZE = 8;


    /* ===================== MODAL STATE ===================== */

    const [selectedAsset, setSelectedAsset] = useState<Artifact | null>(null);
    const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);
    const [loadingClaim, setLoadingClaim] = useState(false);
    const [authModalOpen, setAuthModalOpen] = useState(false);


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

        let ticking = false;

        function handleScroll() {

            if (ticking) return;

            ticking = true;

            requestAnimationFrame(() => {

                if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 300) {
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

        setOpenBundles(prev => ({...prev, [bundleId]: newState}));

        if (newState && !bundleAssets[bundleId]) {
            try {
                const assets = await getBundleAssets(bundleId);
                setBundleAssets(prev => ({...prev, [bundleId]: assets}));
            } catch {
                setBundleAssets(prev => ({...prev, [bundleId]: []}));
            }
        }

    }

    /* ===================== MODAL CONTROLS ===================== */

    function openAsset(artifact: Artifact) {
        setMessage(null);
        setSelectedAsset(artifact);
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

        if (!account) {
            setAuthModalOpen(true);
            return;
        }

        try {

            setLoadingClaim(true);

            await claimAsset(selectedAsset.id);

            queryClient.invalidateQueries({queryKey: ["account"]});

            setBundleAssets(prev => {
                const updated = {...prev};
                for (const bundleId in updated) {
                    updated[bundleId] = updated[bundleId].map(artifact => {
                        if (artifact.id === selectedAsset.id) {
                            return {...artifact, totalSupply: Math.max(0, artifact.totalSupply - 1)};
                        }
                        return artifact;
                    });
                }
                return updated;
            });

            setMessage({type: "success", text: "Artifact claimed successfully!"});

            setTimeout(() => closeModal(), 1500);

        } catch {

            setMessage({type: "error", text: "Failed to claim artifact."});

        } finally {
            setLoadingClaim(false);
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

            {/* ===================== MAIN CONTENT ===================== */}

            <main
                className="pt-[60px] transition-all duration-300 p-6 min-h-screen flex flex-col"
                style={{marginLeft: collapsed ? 64 : 220}}
            >

                {/* ===================== LOADING / ERROR ===================== */}

                {loading && (
                    <p className="text-center text-slate-500">Loading...</p>
                )}

                {error && (
                    <p className="text-center text-red-500">{error}</p>
                )}

                {/* ===================== INFO ===================== */}

                <div className="mb-6 bg-yellow-50 border border-yellow-300 rounded-xl p-4 shadow-sm">

                    <div className="flex items-start gap-3">

                        <div className="text-yellow-600 mt-0.5">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5}
                                 stroke="currentColor" className="w-5 h-5">
                                <path strokeLinecap="round" strokeLinejoin="round"
                                      d="M12 9v3.75m9-.75a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 3.75h.008v.008H12v-.008Z"/>
                            </svg>
                        </div>

                        <div>
                            <p className="text-sm text-yellow-800 font-medium mb-1">
                                Each artifact bundle is automatically generated at a predefined time.
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
                                <h2 className="text-xl font-semibold mb-1">🔒 Upcoming Bundle</h2>
                                <p className="text-sm text-slate-300">This bundle is locked and will be available soon</p>
                            </div>

                            <div className="text-right">
                                <p className="text-xs text-slate-400 mb-1">Releases in</p>
                                <div className="text-lg font-bold text-emerald-400">{timeLeft}</div>
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

                            <div key={bundle.id} className="bg-white rounded-xl shadow-md border border-slate-200">

                                {/* BUNDLE HEADER */}

                                <div
                                    onClick={() => toggleBundle(bundle.id)}
                                    className="p-6 cursor-pointer flex justify-between items-center hover:bg-slate-50 rounded-xl"
                                >
                                    <div>
                                        <h2 className="text-xl font-semibold">{bundle.identifier}</h2>
                                        <p className="text-sm text-gray-500">
                                            Created at: {new Date(bundle.createdAt).toLocaleDateString()}
                                        </p>
                                    </div>
                                    <span className="text-gray-400">{isOpen ? "▲" : "▼"}</span>
                                </div>

                                {/* ===================== ASSET GRID ===================== */}

                                {isOpen && (

                                    <div className="p-6 pt-0">

                                        {!bundleAssets[bundle.id] && (
                                            <p className="text-gray-500 text-sm">Loading assets...</p>
                                        )}

                                        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">

                                            {bundleAssets[bundle.id]?.map((artifact, index) => {

                                                const disabled = artifact.totalSupply === 0;
                                                const meta = artifact.metadata as CardMetadata;

                                                return (
                                                    <div key={`${bundle.id}-${artifact.id}-${index}`} className="relative">
                                                        <ArtifactCardThumb
                                                            metadata={meta}
                                                            disabled={disabled}
                                                            onClick={() => openAsset(artifact)}
                                                        />
                                                        <div className="mt-1 text-center">
                                                            <span className="text-[10px] text-zinc-400">
                                                                {disabled ? "Sold Out" : `${artifact.totalSupply} left`}
                                                            </span>
                                                        </div>
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
                    <p className="text-center mt-6 text-gray-500">Loading more bundles...</p>
                )}

            </main>


            {authModalOpen && <AuthRequiredModal onClose={() => setAuthModalOpen(false)}/>}

            {/* ===================== MODAL ===================== */}

            {selectedAsset && (
                <ArtifactCardFullscreen
                    metadata={selectedAsset.metadata as CardMetadata}
                    title="Claim Artifact"
                    onClose={closeModal}
                >
                    <div className="px-5 py-4 space-y-3">
                        <p className="text-xs text-zinc-400 text-center">
                            {selectedAsset.totalSupply} remaining · Created {new Date(selectedAsset.createdAt).toLocaleDateString()}
                        </p>

                        {message && (
                            <div className={`p-2.5 rounded-lg text-sm text-center font-medium
                                ${message.type === "success"
                                    ? "bg-green-100 text-green-700"
                                    : "bg-red-100 text-red-700"
                                }`}
                            >
                                {message.text}
                            </div>
                        )}
                        <div className="flex gap-3">
                            <button
                                onClick={closeModal}
                                className="flex-1 px-4 py-2 rounded-xl border border-slate-300 text-slate-600 hover:bg-slate-50 transition text-sm"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleClaim}
                                disabled={loadingClaim}
                                className="flex-1 px-4 py-2 rounded-xl bg-zinc-900 text-white font-medium hover:bg-zinc-700 transition disabled:opacity-50 text-sm"
                            >
                                {loadingClaim ? "Claiming..." : account ? "Confirm Claim" : "Sign in to claim"}
                            </button>
                        </div>
                    </div>
                </ArtifactCardFullscreen>
            )}

        </div>
    );
}

export default Reward;
