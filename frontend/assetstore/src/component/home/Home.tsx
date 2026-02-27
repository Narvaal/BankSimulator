import { useState, useEffect } from "react";
import NavBar from "../navBar/NavBar";
import { useAccount } from "../auth/Auth";

/* ===================== TYPES ===================== */

interface AssetUnit {
    id: number;
    assetId: number;
    ownerAccountId: number;
    createdAt: string;
}

interface Asset {
    id: number;
    text: string;
}

interface AssetView {
    unityId: number;
    text: string;
    createdAt: string;
}

/* ===================== API ===================== */

async function getAssetUnits(ownerId: number): Promise<AssetUnit[]> {
    const res = await fetch(`http://localhost:8080/asset-units?ownerId=${ownerId}`, {
        credentials: "include",
    });

    if (!res.ok) throw new Error("Assets not found");
    return res.json();
}

async function getAsset(assetId: number): Promise<Asset> {
    const res = await fetch(`http://localhost:8080/assets/${assetId}`, {
        credentials: "include",
    });

    if (!res.ok) throw new Error("Asset not found");
    return res.json();
}

/* ===================== PAGE ===================== */

export default function Home() {
    const { data: account, isLoading: authLoading, error: authError } = useAccount();

    const [collapsed, setCollapsed] = useState(false);
    const [assets, setAssets] = useState<AssetView[]>([]);
    const [selectedUnity, setSelectedUnity] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!account) return;

        const accountId = account.id; // narrowing definitivo
        let cancelled = false;

        async function load() {
            try {
                setLoading(true);
                setError(null);

                const units = await getAssetUnits(accountId);

                const views: AssetView[] = await Promise.all(
                    units.map(async (unit) => {
                        const asset = await getAsset(unit.assetId);

                        return {
                            unityId: unit.id,
                            text: asset.text,
                            createdAt: unit.createdAt,
                        };
                    })
                );

                if (!cancelled) setAssets(views);

            } catch (e: any) {
                if (!cancelled) setError(e.message);
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();

        return () => {
            cancelled = true;
        };

    }, [account]);

    if (authLoading) {
        return <div className="p-10 text-center">Checking session...</div>;
    }

    if (authError || !account) {
        return <div className="p-10 text-center text-red-500">Not authenticated</div>;
    }

    return (
        <div className="min-h-screen bg-slate-100">
            <NavBar collapsed={collapsed} setCollapsed={setCollapsed} />

            <main className="transition-all duration-300 p-6" style={{ marginLeft: collapsed ? 80 : 256 }}>
                <h1 className="text-2xl font-bold text-center mb-2">Inventory</h1>

                <h2 className="text-center text-lg font-semibold mb-6">
                    Balance: ${account.balance}
                </h2>

                {loading && <p className="text-center text-slate-500">Loading...</p>}
                {error && <p className="text-center text-red-500">{error}</p>}

                {!loading && !error && (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                        {assets.map((asset) => (
                            <div
                                key={asset.unityId}
                                onClick={() => setSelectedUnity(asset.unityId)}
                                className="rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow-md hover:scale-[1.02] transition p-5 cursor-pointer flex flex-col items-center text-center"
                            >
                                <span className="text-slate-800 font-semibold">{asset.text}</span>
                                <span className="mt-2 text-xs text-slate-500">
                                    {new Date(asset.createdAt).toLocaleDateString()}
                                </span>
                            </div>
                        ))}
                    </div>
                )}
            </main>

            {selectedUnity !== null && (
                <div className="fixed bottom-6 right-6 bg-white shadow-lg rounded-xl p-4">
                    Selected asset: #{selectedUnity}
                    <button
                        className="ml-4 px-3 py-1 bg-slate-800 text-white rounded"
                        onClick={() => setSelectedUnity(null)}
                    >
                        Close
                    </button>
                </div>
            )}
        </div>
    );
}
