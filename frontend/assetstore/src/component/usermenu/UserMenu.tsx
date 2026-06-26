import {useEffect, useRef, useState} from "react";
import CountdownTimer from "../util/CountdownTimer.tsx";
import {ArrowRightOnRectangleIcon, ChevronDownIcon} from "@heroicons/react/24/outline";
import {AnimatePresence, motion} from "framer-motion";
import {clearToken} from "../../auth";
import {useNavigate} from "react-router-dom";

export interface Account {
    balance: number;
    nextFreeAssetAt: string;
    name: string;
    imageUrl: string;
}

export default function Navbar({balance, nextFreeAssetAt, name, imageUrl}: Account) {

    const navigate = useNavigate();

    const [kofiOpen, setKofiOpen] = useState(false);
    const [warningOpen, setWarningOpen] = useState(false);
    const [accepted, setAccepted] = useState(false);
    const [dropdownOpen, setDropdownOpen] = useState(false);

    const dropdownRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        function handleClickOutside(e: MouseEvent) {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
                setDropdownOpen(false);
            }
        }
        if (dropdownOpen) {
            document.addEventListener("mousedown", handleClickOutside);
        }
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [dropdownOpen]);

    function handleLogout() {
        clearToken();
        navigate("/login");
    }

    return (
        <>
            <header className="fixed top-0 left-0 w-full z-50 border-b border-zinc-100 bg-white">
                <div className="w-full px-5 h-12 flex items-center gap-3">

                    {/* Logo */}
                    <div className="flex items-center gap-2 min-w-0 flex-shrink-0">
                        <img
                            src="/icons/RareLines.png"
                            alt="Rare Lines"
                            className="h-6 w-auto object-contain"
                        />
                        <span className="font-semibold text-zinc-900 tracking-tight text-[13px]">
                            Rare Lines
                        </span>
                    </div>

                    {/* Countdown — center */}
                    <div className="flex-1 flex justify-center">
                        <div className="text-xs text-zinc-400 font-medium tabular-nums">
                            <CountdownTimer
                                key={nextFreeAssetAt}
                                targetDate={nextFreeAssetAt}
                            />
                        </div>
                    </div>

                    {/* Right side */}
                    <div className="flex items-center gap-1.5 flex-shrink-0">

                        {/* Balance — opens Ko-fi modal */}
                        <button
                            onClick={() => setWarningOpen(true)}
                            className="flex items-center gap-1 px-2.5 py-1 rounded-md border border-zinc-200 text-xs font-medium text-zinc-700 hover:bg-zinc-50 hover:border-zinc-300 transition-colors"
                        >
                            <span className="text-zinc-400">$</span>
                            <span className="tabular-nums">{balance.toFixed(2)}</span>
                        </button>

                        {/* Divider */}
                        <div className="w-px h-4 bg-zinc-100"/>

                        {/* User dropdown */}
                        <div className="relative" ref={dropdownRef}>
                            <button
                                onClick={() => setDropdownOpen(v => !v)}
                                className="flex items-center gap-1.5 px-2 py-1 rounded-md hover:bg-zinc-100 transition-colors"
                            >
                                {imageUrl ? (
                                    <img
                                        src={imageUrl}
                                        alt={name}
                                        className="w-6 h-6 rounded-full object-cover ring-1 ring-zinc-200"
                                    />
                                ) : (
                                    <div className="w-6 h-6 rounded-full bg-zinc-100 flex items-center justify-center">
                                        <span className="text-[10px] font-semibold text-zinc-600">
                                            {name.charAt(0).toUpperCase()}
                                        </span>
                                    </div>
                                )}
                                <span className="text-xs font-medium text-zinc-800 max-w-[100px] truncate">
                                    {name}
                                </span>
                                <motion.div
                                    animate={{rotate: dropdownOpen ? 180 : 0}}
                                    transition={{duration: 0.15}}
                                >
                                    <ChevronDownIcon className="w-3 h-3 text-zinc-400"/>
                                </motion.div>
                            </button>

                            <AnimatePresence>
                                {dropdownOpen && (
                                    <motion.div
                                        initial={{opacity: 0, y: -6, scale: 0.97}}
                                        animate={{opacity: 1, y: 0, scale: 1}}
                                        exit={{opacity: 0, y: -6, scale: 0.97}}
                                        transition={{duration: 0.12}}
                                        className="absolute right-0 top-full mt-1.5 w-44 bg-white rounded-xl border border-zinc-100 shadow-lg shadow-zinc-200/60 py-1 z-50"
                                    >
                                        <div className="px-3 py-2 border-b border-zinc-100 mb-1">
                                            <p className="text-xs font-medium text-zinc-900 truncate">{name}</p>
                                        </div>
                                        <button
                                            onClick={handleLogout}
                                            className="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-zinc-500 hover:text-zinc-900 hover:bg-zinc-50 transition-colors"
                                        >
                                            <ArrowRightOnRectangleIcon className="w-4 h-4"/>
                                            Sign out
                                        </button>
                                    </motion.div>
                                )}
                            </AnimatePresence>
                        </div>

                    </div>
                </div>
            </header>

            {/* WARNING MODAL */}
            {warningOpen && (
                <div
                    className="fixed inset-0 z-[100] flex items-center justify-center bg-black/40"
                    onClick={() => setWarningOpen(false)}
                >
                    <div
                        className="bg-white rounded-2xl shadow-xl w-[420px] p-6"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <h2 className="text-base font-semibold text-zinc-900 mb-1">
                            Before you continue
                        </h2>
                        <p className="text-sm text-zinc-500 mb-4">
                            Read how adding funds works before proceeding.
                        </p>

                        <div className="text-sm text-zinc-600 space-y-2.5 mb-5 border border-zinc-100 rounded-xl p-4 bg-zinc-50">
                            <p>
                                • A Ko-fi window will open. Log in, choose a donation amount, confirm the payment, and enter your email.
                            </p>
                            <p>
                                • Your balance will be credited automatically using your email.
                            </p>
                            <p>
                                • Only <b>USD</b> is accepted. Other currencies may be lost during conversion.
                            </p>
                            <p>
                                • You <b>must use the same email</b> as your account here. Otherwise, your balance <b>cannot be credited</b>.
                            </p>
                            <p>
                                • All payments are handled securely by <b>Ko-fi</b>. This app does not store any payment information.
                            </p>
                            <p>
                                • This project is built by a <b>solo developer</b> as a portfolio project. Donations help keep it online ❤️
                            </p>
                        </div>

                        <label className="flex items-center gap-2.5 text-sm text-zinc-700 mb-5 cursor-pointer">
                            <input
                                type="checkbox"
                                checked={accepted}
                                onChange={(e) => setAccepted(e.target.checked)}
                                className="accent-zinc-900 w-4 h-4"
                            />
                            I understand and accept all conditions
                        </label>

                        <div className="flex justify-end gap-2">
                            <button
                                onClick={() => setWarningOpen(false)}
                                className="px-4 py-2 text-sm rounded-lg border border-zinc-200 text-zinc-600 hover:bg-zinc-50 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                disabled={!accepted}
                                onClick={() => {
                                    setWarningOpen(false);
                                    setKofiOpen(true);
                                }}
                                className="px-4 py-2 text-sm rounded-lg bg-zinc-900 text-white hover:bg-zinc-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                            >
                                Continue to Ko-fi
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* KO-FI MODAL */}
            {kofiOpen && (
                <div
                    className="fixed inset-0 z-[100] flex items-center justify-center bg-black/40"
                    onClick={() => setKofiOpen(false)}
                >
                    <div
                        className="relative w-[420px] h-[620px] bg-white rounded-2xl shadow-xl overflow-hidden"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <button
                            onClick={() => setKofiOpen(false)}
                            className="absolute top-3 right-3 w-7 h-7 flex items-center justify-center rounded-full bg-zinc-100 hover:bg-zinc-200 text-zinc-500 hover:text-zinc-800 transition-colors text-sm z-10"
                        >
                            ✕
                        </button>
                        <iframe
                            title="Support the project on Ko-fi"
                            src="https://ko-fi.com/alessandrobezerra/?hidefeed=true&widget=true&embed=true"
                            className="w-full h-full mt-3"
                        />
                    </div>
                </div>
            )}
        </>
    );
}
