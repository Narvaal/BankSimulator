/* Shared card visual component — used in Reward, Marketplace, Inventory, Profile, ArtifactDetail */

import {useEffect, type ReactNode} from "react";
import {ShieldCheckIcon, BoltIcon, ExclamationTriangleIcon} from "@heroicons/react/24/outline";

export interface CardMetadata {
    name?: string;
    subtitle?: string;
    category?: string;
    rarity?: string;
    illustration?: string;
    background?: string;
    attributes?: Record<string, number>;
    abilities?: { name: string; description: string }[];
    passive?: { name: string; description: string };
    weakness?: string;
    flavorText?: string;
    lore?: string;
    traits?: { name: string; value: string }[];
    timeline?: { date: string; event: string }[];
    references?: string[];
    collection?: string;
    cardNumber?: string;
    releaseDate?: string;
    artist?: string;
    model?: string;
    prompt?: string;
    seed?: string;
    chosenStyle?: string;
    [key: string]: unknown;
}

const RARITY_STYLES: Record<string, { badge: string; border: string; glow: string }> = {
    Common:    { badge: "bg-slate-100 text-slate-600",   border: "border-slate-200",  glow: "" },
    Rare:      { badge: "bg-slate-200 text-slate-700",   border: "border-slate-400",  glow: "" },
    Epic:      { badge: "bg-purple-100 text-purple-700", border: "border-purple-400", glow: "shadow-purple-200" },
    Legendary: { badge: "bg-yellow-100 text-yellow-700", border: "border-yellow-400", glow: "shadow-yellow-200" },
    Mythic:    { badge: "bg-cyan-100 text-cyan-700",     border: "border-cyan-400",   glow: "shadow-cyan-200" },
    Ultimate:  { badge: "bg-pink-100 text-pink-700",     border: "border-pink-400",   glow: "shadow-pink-200" },
};

export function RarityBadge({ rarity }: { rarity?: string }) {
    const r = rarity ?? "Common";
    const s = RARITY_STYLES[r] ?? RARITY_STYLES.Common;
    return (
        <span className={`text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full ${s.badge}`}>
            {r}
        </span>
    );
}

/* Compact card thumbnail — used in grids */
export function ArtifactCardThumb({
    metadata,
    disabled = false,
    onClick,
}: {
    metadata: CardMetadata;
    disabled?: boolean;
    onClick?: () => void;
}) {
    const rarity = metadata.rarity ?? "Common";
    const s = RARITY_STYLES[rarity] ?? RARITY_STYLES.Common;

    return (
        <div
            onClick={!disabled ? onClick : undefined}
            className={`relative rounded-xl overflow-hidden border-2 transition-all duration-200 flex flex-col
                ${s.border} ${s.glow ? `shadow-lg ${s.glow}` : "shadow-sm"}
                ${disabled ? "opacity-50 cursor-not-allowed grayscale" : "cursor-pointer hover:scale-[1.03] hover:shadow-xl"}
            `}
            style={{ aspectRatio: "2/3" }}
        >
            {/* Illustration */}
            {metadata.illustration ? (
                <img
                    src={metadata.illustration}
                    alt={metadata.name}
                    className="absolute inset-0 w-full h-full object-cover"
                    loading="lazy"
                />
            ) : (
                <div className="absolute inset-0 bg-gradient-to-br from-slate-800 to-slate-600" />
            )}

            {/* Gradient overlay at bottom */}
            <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />

            {/* Rarity badge top-right */}
            <div className="absolute top-2 right-2">
                <RarityBadge rarity={rarity} />
            </div>

            {/* Card number top-left */}
            {metadata.cardNumber && (
                <span className="absolute top-2 left-2 text-[9px] text-white/60 font-mono">
                    #{metadata.cardNumber}
                </span>
            )}

            {/* Name + subtitle at bottom */}
            <div className="absolute bottom-0 left-0 right-0 p-2.5">
                <p className="text-white font-bold text-sm leading-tight line-clamp-1">{metadata.name}</p>
                {metadata.subtitle && (
                    <p className="text-white/60 text-[10px] leading-tight line-clamp-1 mt-0.5">{metadata.subtitle}</p>
                )}
            </div>
        </div>
    );
}

/* Tiny radar chart of the card attributes — overlaid on the full-art card */
function AttributeRadar({ attributes }: { attributes: Record<string, number> }) {
    const entries = Object.entries(attributes);
    if (entries.length < 3) return null;

    const size = 104;
    const c = size / 2;
    const rMax = 33;
    const rLabel = rMax + 11;

    const point = (i: number, r: number): [number, number] => {
        const angle = (Math.PI * 2 * i) / entries.length - Math.PI / 2;
        return [c + r * Math.cos(angle), c + r * Math.sin(angle)];
    };

    const ring = (r: number) =>
        entries.map((_, i) => point(i, r).map(n => n.toFixed(1)).join(",")).join(" ");

    const data = entries
        .map(([, v], i) => point(i, (Math.min(Math.max(v, 0), 100) / 100) * rMax).map(n => n.toFixed(1)).join(","))
        .join(" ");

    return (
        <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
            {[1 / 3, 2 / 3, 1].map(f => (
                <polygon key={f} points={ring(rMax * f)} fill="none" stroke="rgba(255,255,255,0.22)" strokeWidth="0.5" />
            ))}
            {entries.map(([k], i) => {
                const [x, y] = point(i, rMax);
                return <line key={k} x1={c} y1={c} x2={x} y2={y} stroke="rgba(255,255,255,0.22)" strokeWidth="0.5" />;
            })}
            <polygon points={data} fill="rgba(255,255,255,0.3)" stroke="white" strokeWidth="1.5" strokeLinejoin="round" />
            {entries.map(([k], i) => {
                const [x, y] = point(i, rLabel);
                return (
                    <text
                        key={k}
                        x={x}
                        y={y}
                        textAnchor="middle"
                        dominantBaseline="middle"
                        fill="rgba(255,255,255,0.8)"
                        fontSize="6.5"
                        fontWeight="600"
                        letterSpacing="0.5"
                    >
                        {k.slice(0, 3).toUpperCase()}
                    </text>
                );
            })}
        </svg>
    );
}

/* Fullscreen "full art" view — image fills the whole card, info overlaid on top (Pokemon-style) */
export function ArtifactCardFullscreen({
    metadata,
    onClose,
    title,
    children,
}: {
    metadata: CardMetadata;
    onClose: () => void;
    title?: string;
    children?: ReactNode;
}) {
    const rarity = metadata.rarity ?? "Common";
    const s = RARITY_STYLES[rarity] ?? RARITY_STYLES.Common;

    useEffect(() => {
        const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") onClose(); };
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [onClose]);

    return (
        <div
            className="fixed inset-0 bg-black/90 backdrop-blur-sm z-[100] overflow-y-auto"
            onClick={onClose}
        >
            <button
                onClick={onClose}
                className="fixed top-4 right-4 text-white/60 hover:text-white text-3xl leading-none transition-colors z-10"
            >
                ✕
            </button>

            <div className="min-h-full flex flex-col items-center justify-center p-4 py-12">
                <div onClick={(e) => e.stopPropagation()}>

                    {title && (
                        <p className="text-white/50 text-xs font-semibold uppercase tracking-widest text-center mb-3">
                            {title}
                        </p>
                    )}

                    <div className="flex flex-wrap gap-4 justify-center items-start">

                    {/* Full-art card (front) */}
                    <div
                        className={`relative shrink-0 rounded-2xl overflow-hidden border-2 ${s.border} ${s.glow ? `shadow-2xl ${s.glow}` : "shadow-2xl"}`}
                        style={{ height: "min(75vh, 640px)", aspectRatio: "2/3" }}
                    >
                        {metadata.illustration ? (
                            <img
                                src={metadata.illustration}
                                alt={metadata.name}
                                className="absolute inset-0 w-full h-full object-cover"
                            />
                        ) : (
                            <div className="absolute inset-0 bg-gradient-to-br from-slate-800 to-slate-600" />
                        )}

                        {/* Top overlay */}
                        <div className="absolute inset-x-0 top-0 h-24 bg-gradient-to-b from-black/85 via-black/30 to-transparent" />
                        <div className="absolute top-3 left-3 right-3 flex items-start justify-between gap-2">
                            <div className="flex items-center gap-1.5 flex-wrap">
                                {metadata.cardNumber && (
                                    <span className="text-[10px] text-white/80 font-mono bg-black/40 backdrop-blur-sm px-1.5 py-0.5 rounded">
                                        #{metadata.cardNumber}
                                    </span>
                                )}
                                {metadata.category && (
                                    <span className="text-[10px] text-white/80 bg-black/40 backdrop-blur-sm px-1.5 py-0.5 rounded">
                                        {metadata.category}
                                    </span>
                                )}
                            </div>
                            <RarityBadge rarity={rarity} />
                        </div>

                        {/* Attribute radar top-right, below the rarity badge */}
                        {metadata.attributes && Object.keys(metadata.attributes).length >= 3 && (
                            <div className="absolute top-11 right-3 bg-black/35 backdrop-blur-sm rounded-xl p-1">
                                <AttributeRadar attributes={metadata.attributes} />
                            </div>
                        )}

                        {/* Bottom overlay */}
                        <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black via-black/85 to-transparent pt-20 pb-4 px-4 space-y-2.5">
                            <div>
                                <p className="text-white font-extrabold text-2xl leading-tight drop-shadow-lg">{metadata.name}</p>
                                {metadata.subtitle && (
                                    <p className="text-white/70 text-xs mt-0.5">{metadata.subtitle}</p>
                                )}
                            </div>

                            {metadata.passive && (
                                <div className="w-full bg-white/10 backdrop-blur-sm rounded-lg px-2.5 py-2 border border-white/10">
                                    <div className="flex items-center gap-1 text-white/50">
                                        <ShieldCheckIcon className="w-3 h-3" />
                                        <span className="text-[9px] font-semibold uppercase tracking-widest">Passive</span>
                                    </div>
                                    <p className="text-white text-xs font-bold mt-1">{metadata.passive.name}</p>
                                    <p className="text-white/60 text-[10px] mt-0.5 leading-snug">{metadata.passive.description}</p>
                                </div>
                            )}

                            {metadata.abilities && metadata.abilities.length > 0 && (
                                <div className="space-y-1.5">
                                    {metadata.abilities.map((ab, i) => (
                                        <div key={i} className="bg-white/10 backdrop-blur-sm rounded-lg px-2.5 py-2 border border-white/10">
                                            <div className="flex items-center gap-1 text-white/50">
                                                <BoltIcon className="w-3 h-3" />
                                                <span className="text-[9px] font-semibold uppercase tracking-widest">Ability</span>
                                            </div>
                                            <p className="text-white text-xs font-bold mt-1">{ab.name}</p>
                                            <p className="text-white/60 text-[10px] mt-0.5 leading-snug">{ab.description}</p>
                                        </div>
                                    ))}
                                </div>
                            )}

                            {metadata.weakness && (
                                <div className="w-full bg-white/10 backdrop-blur-sm rounded-lg px-2.5 py-2 border border-white/10">
                                    <div className="flex items-center gap-1 text-white/50">
                                        <ExclamationTriangleIcon className="w-3 h-3" />
                                        <span className="text-[9px] font-semibold uppercase tracking-widest">Weakness</span>
                                    </div>
                                    <p className="text-white/80 text-[11px] mt-1 leading-snug">{metadata.weakness}</p>
                                </div>
                            )}

                            {metadata.flavorText && (
                                <p className="text-white/50 text-[10px] italic leading-snug pt-0.5">"{metadata.flavorText}"</p>
                            )}
                        </div>
                    </div>

                    {/* Back — everything not already on the front */}
                    <ArtifactCardBack metadata={metadata} borderClass={s.border} glowClass={s.glow} />

                    </div>
                </div>

                {/* Extra content — actions, price history, etc. */}
                {children && (
                    <div className="mt-3 w-full max-w-[420px] bg-white rounded-2xl shadow-2xl overflow-hidden">
                        {children}
                    </div>
                )}
            </div>
        </div>
    );
}

function ArtifactCardBack({
    metadata,
    borderClass,
    glowClass,
}: {
    metadata: CardMetadata;
    borderClass: string;
    glowClass: string;
}) {
    return (
        <div
            className={`relative shrink-0 rounded-2xl overflow-hidden border-2 bg-white flex flex-col ${borderClass} ${glowClass ? `shadow-2xl ${glowClass}` : "shadow-2xl"}`}
            style={{ height: "min(75vh, 640px)", aspectRatio: "2/3" }}
        >
            <div className="overflow-y-auto flex-1 p-4 space-y-3">
                <div className="text-center pb-2 border-b border-slate-100">
                    <p className="text-xs font-bold text-zinc-700">{metadata.name}</p>
                    {(metadata.collection || metadata.cardNumber) && (
                        <p className="text-[10px] text-zinc-400 mt-0.5">
                            {metadata.collection}{metadata.collection && metadata.cardNumber ? " · " : ""}{metadata.cardNumber ? `#${metadata.cardNumber}` : ""}
                        </p>
                    )}
                </div>

                {metadata.lore && (
                    <div>
                        <p className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider mb-1">Lore</p>
                        <p className="text-xs text-zinc-600 leading-relaxed">{metadata.lore}</p>
                    </div>
                )}

                {metadata.traits && metadata.traits.length > 0 && (
                    <div>
                        <p className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider mb-1.5">Traits</p>
                        <div className="flex flex-wrap gap-1.5">
                            {metadata.traits.map((t, i) => (
                                <div key={i} className="bg-slate-50 rounded-md px-2 py-1">
                                    <span className="text-[8px] text-zinc-400 block">{t.name}</span>
                                    <span className="text-[10px] font-semibold text-zinc-700">{t.value}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {metadata.timeline && metadata.timeline.length > 0 && (
                    <div>
                        <p className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider mb-1.5">Timeline</p>
                        <ol className="space-y-1">
                            {metadata.timeline.map((t, i) => (
                                <li key={i} className="flex gap-2 text-[10px]">
                                    <span className="text-zinc-400 font-mono shrink-0">{t.date}</span>
                                    <span className="text-zinc-600">{t.event}</span>
                                </li>
                            ))}
                        </ol>
                    </div>
                )}

                {metadata.references && metadata.references.length > 0 && (
                    <div>
                        <p className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider mb-1.5">Sources</p>
                        <ul className="space-y-1">
                            {metadata.references.map((url, i) => {
                                let hostname = url;
                                try { hostname = new URL(url).hostname.replace(/^www\./, ""); } catch { /* keep raw url */ }
                                return (
                                    <li key={i}>
                                        <a
                                            href={url}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="text-[10px] text-blue-600 hover:text-blue-800 hover:underline truncate block"
                                        >
                                            {hostname}
                                        </a>
                                    </li>
                                );
                            })}
                        </ul>
                    </div>
                )}

                {(metadata.prompt || metadata.chosenStyle) && (
                    <AiInfoPanel metadata={metadata} />
                )}
            </div>

            {(metadata.artist || metadata.releaseDate) && (
                <div className="shrink-0 border-t border-slate-100 px-4 py-2 flex justify-between text-[9px] text-zinc-400">
                    {metadata.artist && <span>Art by {metadata.artist}</span>}
                    {metadata.releaseDate && <span>{new Date(metadata.releaseDate).toLocaleDateString()}</span>}
                </div>
            )}
        </div>
    );
}

const ATTR_COLORS: Record<string, string> = {
    influence:   "bg-blue-500",
    innovation:  "bg-emerald-500",
    controversy: "bg-red-500",
    longevity:   "bg-amber-500",
    reach:       "bg-purple-500",
};

/* Full card detail panel — used in modals and ArtifactDetail */
export function ArtifactCardDetail({ metadata }: { metadata: CardMetadata }) {
    const rarity = metadata.rarity ?? "Common";
    const s = RARITY_STYLES[rarity] ?? RARITY_STYLES.Common;

    return (
        <div className="space-y-4">

            {/* Hero: illustration + core info */}
            <div className={`rounded-2xl overflow-hidden border-2 ${s.border} ${s.glow ? `shadow-xl ${s.glow}` : "shadow-md"}`}>
                {metadata.illustration && (
                    <div className="relative w-full" style={{ aspectRatio: "2/3", maxHeight: 340 }}>
                        <img
                            src={metadata.illustration}
                            alt={metadata.name}
                            className="w-full h-full object-cover"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent" />
                        <div className="absolute bottom-0 left-0 right-0 p-4">
                            <div className="flex items-end justify-between gap-2">
                                <div>
                                    <p className="text-white font-bold text-xl leading-tight">{metadata.name}</p>
                                    {metadata.subtitle && (
                                        <p className="text-white/70 text-sm mt-0.5">{metadata.subtitle}</p>
                                    )}
                                </div>
                                <RarityBadge rarity={rarity} />
                            </div>
                        </div>
                    </div>
                )}

                <div className="p-4 bg-white space-y-1">
                    <div className="flex items-center gap-2 flex-wrap">
                        {!metadata.illustration && (
                            <p className="font-bold text-zinc-900 text-lg">{metadata.name}</p>
                        )}
                        {metadata.category && (
                            <span className="text-xs text-zinc-400 bg-zinc-100 px-2 py-0.5 rounded-full">{metadata.category}</span>
                        )}
                        {metadata.collection && (
                            <span className="text-xs text-zinc-400">{metadata.collection} {metadata.cardNumber ? `· #${metadata.cardNumber}` : ""}</span>
                        )}
                    </div>
                    {metadata.flavorText && (
                        <p className="text-sm text-zinc-500 italic">"{metadata.flavorText}"</p>
                    )}
                </div>
            </div>

            {/* Attributes */}
            {metadata.attributes && Object.keys(metadata.attributes).length > 0 && (
                <div className="bg-white rounded-xl border border-slate-200 p-4">
                    <p className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-3">Attributes</p>
                    <div className="space-y-2">
                        {Object.entries(metadata.attributes).map(([key, val]) => (
                            <div key={key} className="flex items-center gap-2">
                                <span className="text-xs text-zinc-500 w-20 capitalize shrink-0">{key}</span>
                                <div className="flex-1 bg-slate-100 rounded-full h-1.5 overflow-hidden">
                                    <div
                                        className={`h-full rounded-full ${ATTR_COLORS[key] ?? "bg-zinc-400"}`}
                                        style={{ width: `${Math.min(val, 100)}%` }}
                                    />
                                </div>
                                <span className="text-xs font-semibold text-zinc-700 w-6 text-right">{val}</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Abilities */}
            {metadata.abilities && metadata.abilities.length > 0 && (
                <div className="bg-white rounded-xl border border-slate-200 p-4">
                    <p className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-3">Abilities</p>
                    <div className="space-y-2">
                        {metadata.abilities.map((ab, i) => (
                            <div key={i} className="bg-slate-50 rounded-lg p-3">
                                <p className="text-xs font-bold text-zinc-800">{ab.name}</p>
                                <p className="text-xs text-zinc-500 mt-0.5">{ab.description}</p>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Passive + Weakness */}
            {(metadata.passive || metadata.weakness) && (
                <div className="bg-white rounded-xl border border-slate-200 p-4 space-y-3">
                    {metadata.passive && (
                        <div>
                            <p className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-1">Passive</p>
                            <div className="bg-amber-50 rounded-lg p-3">
                                <p className="text-xs font-bold text-amber-800">{metadata.passive.name}</p>
                                <p className="text-xs text-amber-700 mt-0.5">{metadata.passive.description}</p>
                            </div>
                        </div>
                    )}
                    {metadata.weakness && (
                        <div>
                            <p className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-1">Weakness</p>
                            <p className="text-xs text-red-600 bg-red-50 rounded-lg p-3">{metadata.weakness}</p>
                        </div>
                    )}
                </div>
            )}

            {/* Lore */}
            {metadata.lore && (
                <div className="bg-white rounded-xl border border-slate-200 p-4">
                    <p className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Lore</p>
                    <p className="text-sm text-zinc-600 leading-relaxed">{metadata.lore}</p>
                </div>
            )}

            {/* Traits */}
            {metadata.traits && metadata.traits.length > 0 && (
                <div className="bg-white rounded-xl border border-slate-200 p-4">
                    <p className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-3">Traits</p>
                    <div className="flex flex-wrap gap-2">
                        {metadata.traits.map((t, i) => (
                            <div key={i} className="bg-slate-50 rounded-lg px-3 py-1.5">
                                <span className="text-[10px] text-zinc-400 block">{t.name}</span>
                                <span className="text-xs font-semibold text-zinc-700">{t.value}</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Timeline */}
            {metadata.timeline && metadata.timeline.length > 0 && (
                <div className="bg-white rounded-xl border border-slate-200 p-4">
                    <p className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-3">Timeline</p>
                    <ol className="space-y-2">
                        {metadata.timeline.map((t, i) => (
                            <li key={i} className="flex gap-3 text-xs">
                                <span className="text-zinc-400 font-mono shrink-0 w-20">{t.date}</span>
                                <span className="text-zinc-600">{t.event}</span>
                            </li>
                        ))}
                    </ol>
                </div>
            )}

            {/* Sources */}
            {metadata.references && metadata.references.length > 0 && (
                <div className="bg-white rounded-xl border border-slate-200 p-4">
                    <p className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-3">Sources</p>
                    <ul className="space-y-1.5">
                        {metadata.references.map((url, i) => {
                            let hostname = url;
                            try { hostname = new URL(url).hostname.replace(/^www\./, ""); } catch {}
                            return (
                                <li key={i}>
                                    <a
                                        href={url}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="text-xs text-blue-600 hover:text-blue-800 hover:underline truncate block"
                                    >
                                        {hostname}
                                    </a>
                                </li>
                            );
                        })}
                    </ul>
                </div>
            )}

            {/* AI Info */}
            {(metadata.prompt || metadata.chosenStyle) && (
                <AiInfoPanel metadata={metadata} />
            )}
        </div>
    );
}

function AiInfoPanel({ metadata }: { metadata: CardMetadata }) {
    return (
        <div className="bg-white rounded-xl border border-slate-200 p-3 min-w-0 overflow-hidden">
            <p className="text-[9px] font-semibold text-zinc-400 uppercase tracking-wider mb-2">AI Info</p>
            <div className="space-y-2 min-w-0">
                {metadata.chosenStyle && (
                    <div className="min-w-0">
                        <p className="text-[9px] text-zinc-400 uppercase tracking-wider mb-0.5">Art Style</p>
                        <p className="text-[10px] text-zinc-600 italic leading-snug break-words">{metadata.chosenStyle}</p>
                    </div>
                )}
                {metadata.prompt && (
                    <div className="min-w-0">
                        <p className="text-[9px] text-zinc-400 uppercase tracking-wider mb-0.5">Image Prompt</p>
                        <p className="text-[10px] text-zinc-500 leading-snug break-words">{metadata.prompt}</p>
                    </div>
                )}
                <div className="space-y-1 min-w-0">
                    {metadata.model && (
                        <div className="min-w-0 flex items-baseline gap-1.5">
                            <span className="text-[9px] text-zinc-400 uppercase tracking-wider shrink-0">Model</span>
                            <span className="text-[10px] text-zinc-600 break-all">{metadata.model}</span>
                        </div>
                    )}
                    {metadata.seed && (
                        <div className="min-w-0 flex items-baseline gap-1.5">
                            <span className="text-[9px] text-zinc-400 uppercase tracking-wider shrink-0">Seed</span>
                            <span className="text-[10px] font-mono text-zinc-600 break-all">{metadata.seed}</span>
                        </div>
                    )}
                    {metadata.artist && (
                        <div className="min-w-0 flex items-baseline gap-1.5">
                            <span className="text-[9px] text-zinc-400 uppercase tracking-wider shrink-0">Artist</span>
                            <span className="text-[10px] text-zinc-600 break-words">{metadata.artist}</span>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
