import {useEffect, useState} from "react";
import {useNavigate, useParams, Link} from "react-router-dom";
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

interface ArtifactView {
    artifactUnitId: number;
    artifactId: number;
    artifactText: string;
    createdAt: string;
}

interface ArtifactPageView {
    items: ArtifactView[];
    page: number;
    pageSize: number;
    totalPages: number;
    totalItems: number;
}

/* ===================== API ===================== */

async function fetchProfile(accountId: string): Promise<PublicProfile> {
    const res = await fetch(`${API_URL}/accounts/${accountId}/profile`);
    if (res.status === 404) throw new Error("Profile not found");
    if (!res.ok) throw new Error("Failed to load profile");
    return res.json();
}

async function fetchInventory(accountId: number, page: number, pageSize: number): Promise<ArtifactPageView> {
    const res = await fetch(
        `${API_URL}/artifact-units?ownerId=${accountId}&page=${page}&pageSize=${pageSize}`
    );
    if (!res.ok) throw new Error("Failed to load inventory");
    return res.json();
}

/* ===================== PAGE ===================== */

export default function ProfilePage() {
    const {accountId} = useParams<{accountId: string}>();
    const navigate = useNavigate();
    const {data: account} = useAccount();

    const [collapsed, setCollapsed] = useState(() => {
        const saved = localStorage.getItem("sidebar-collapsed");
        return saved ? JSON.parse(saved) : false;
    });

    const [profile, setProfile] = useState<PublicProfile | null>(null);
    const [inventory, setInventory] = useState<ArtifactPageView | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(0);
    const pageSize = 30;

    useEffect(() => {
        localStorage.setItem("sidebar-collapsed", JSON.stringify(collapsed));
    }, [collapsed]);

    useEffect(() => {
        if (!accountId) return;
        let cancelled = false;

        async function load() {
            try {
                setLoading(true);
                setError(null);
                const prof = await fetchProfile(accountId!);
                if (cancelled) return;
                setProfile(prof);
                const inv = await fetchInventory(prof.accountId, page, pageSize);
                if (!cancelled) setInventory(inv);
            } catch (e: any) {
                if (!cancelled) setError(e.message);
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => { cancelled = true; };
    }, [accountId, page]);

    const initials = profile?.name
        ? profile.name.split(" ").map(w => w[0]).slice(0, 2).join("").toUpperCase()
        : "?";

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
                {loading && (
                    <p className="text-center text-slate-500 mt-20">Loading...</p>
                )}

                {error && (
                    <div className="text-center mt-20">
                        <p className="text-red-500 text-lg font-semibold mb-2">{error}</p>
                        <button
                            onClick={() => navigate(-1)}
                            className="text-sm text-slate-500 hover:text-slate-800 underline"
                        >
                            Go back
                        </button>
                    </div>
                )}

                {!loading && !error && profile && (

                    <div className="flex flex-col gap-6">

                        {/* Profile header */}
                        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 flex items-center gap-5">
                            {profile.picture ? (
                                <img
                                    src={profile.picture}
                                    alt={profile.name}
                                    className="w-16 h-16 rounded-full object-cover"
                                />
                            ) : (
                                <div className="w-16 h-16 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 text-xl font-bold">
                                    {initials}
                                </div>
                            )}
                            <div>
                                <h1 className="text-xl font-bold text-slate-900">{profile.name}</h1>
                                <p className="text-sm text-slate-500">Account #{profile.accountNumber}</p>
                            </div>
                        </div>

                        {/* Inventory */}
                        <div>
                            <h2 className="text-sm font-semibold text-slate-500 uppercase tracking-wide mb-3">
                                Inventory
                                {inventory && (
                                    <span className="ml-2 font-normal normal-case">
                                        ({inventory.totalItems} artifact{inventory.totalItems !== 1 ? "s" : ""})
                                    </span>
                                )}
                            </h2>

                            {inventory && inventory.items.length === 0 && (
                                <p className="text-slate-400 text-sm">No artifacts in this collection yet.</p>
                            )}

                            {inventory && inventory.items.length > 0 && (
                                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                                    {inventory.items.map((artifact) => (
                                        <Link
                                            key={artifact.artifactUnitId}
                                            to={`/artifact/${artifact.artifactUnitId}`}
                                            className="rounded-xl border border-slate-200 bg-white shadow-sm
                                                hover:shadow-md hover:scale-[1.02] transition
                                                p-5 flex flex-col items-center justify-center text-center min-h-27 no-underline"
                                        >
                                            <span className="text-slate-800 font-semibold">
                                                {artifact.artifactText}
                                            </span>
                                            <div className="mt-1 text-xs text-slate-500">
                                                Artifact #{artifact.artifactId} • Unity #{artifact.artifactUnitId}
                                            </div>
                                        </Link>
                                    ))}
                                </div>
                            )}
                        </div>

                        <div className="mt-auto pt-4">
                            <Pagination
                                page={page}
                                totalPages={inventory?.totalPages ?? 0}
                                onPageChange={setPage}
                            />
                        </div>

                    </div>
                )}
            </main>
        </div>
    );
}
