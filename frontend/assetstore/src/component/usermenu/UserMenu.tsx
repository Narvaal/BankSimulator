import {useState} from "react";
import CountdownTimer from "../util/CountdownTimer.tsx";
import {UserIcon} from "@heroicons/react/24/outline";

export interface Account {
    balance: number;
    nextFreeAssetAt: string;
    name: string;
    imageUrl: string;
}

export default function Navbar({balance, nextFreeAssetAt, name, imageUrl}: Account) {

    const [kofiOpen, setKofiOpen] = useState(false);
    const [warningOpen, setWarningOpen] = useState(false);
    const [accepted, setAccepted] = useState(false);

    return (
        <>
            <header className="fixed top-0 left-0 w-full z-50 border-slate-200 bg-white/70 border backdrop-blur">
                <div className="w-full px-6 h-16 flex items-center text-slate-800">

                    <div className="flex-1"></div>

                    <div className="flex-1 flex justify-end items-center gap-4">
                        <CountdownTimer key={nextFreeAssetAt} targetDate={nextFreeAssetAt}/>
                    </div>

                    <div className="flex-1 flex justify-end items-center gap-4">

                        <button
                            onClick={() => setWarningOpen(true)}
                            className="flex items-center w-full max-w-xs bg-gray-100 rounded-full p-1 hover:bg-gray-200 transition"
                        >
                            <div className="flex-1 px-4 py-2 text-sm text-gray-700 text-left font-medium">
                                Balance: ${balance.toFixed(2)}
                            </div>

                            <div className="flex items-center justify-center w-10 h-10 rounded-full
                                            bg-blue-600 text-white text-lg font-bold hover:bg-blue-700 transition">
                                +
                            </div>
                        </button>

                        <div className="flex items-center gap-2">
                            <span className="text-sm font-medium">
                                {name}
                            </span>

                            {imageUrl ? (
                                <img
                                    src={imageUrl}
                                    alt={name}
                                    className="w-9 h-9 rounded-full object-cover border border-slate-300"
                                />
                            ) : (
                                <UserIcon className="w-9 h-9 p-1 rounded-full border border-slate-300 text-slate-600"/>
                            )}
                        </div>

                    </div>

                </div>
            </header>

            {/* ================= WARNING MODAL ================= */}
            {warningOpen && (
                <div
                    className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50"
                    onClick={() => setWarningOpen(false)}
                >
                    <div
                        className="bg-white rounded-xl shadow-2xl w-[420px] p-6"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <h2 className="text-lg font-semibold mb-3">
                            Before you continue
                        </h2>

                        <div className="text-sm text-gray-600 space-y-3 mb-4">

                            <p>
                                • This app only accepts donations in <b>USD</b>. Any other currency may be lost during conversion.
                            </p>

                            <p>
                                • You <b>must use the same email</b> in Ko-fi as your account here. Otherwise, your balance <b>cannot be credited</b>.
                            </p>

                            <p>
                                • This project is built and maintained by a <b>solo developer</b> as part of a portfolio.
                                Donations help keep the project online and cover infrastructure costs.
                            </p>

                            <p>
                                • I am currently unemployed, so any support truly helps and is greatly appreciated ❤️
                            </p>

                        </div>

                        <label className="flex items-center gap-2 text-sm mb-4 cursor-pointer">
                            <input
                                type="checkbox"
                                checked={accepted}
                                onChange={(e) => setAccepted(e.target.checked)}
                                className="accent-blue-600"
                            />
                            I understand and accept all conditions
                        </label>

                        <div className="flex justify-end gap-2">
                            <button
                                onClick={() => setWarningOpen(false)}
                                className="px-4 py-2 text-sm rounded-lg border border-gray-300 hover:bg-gray-100"
                            >
                                Cancel
                            </button>

                            <button
                                disabled={!accepted}
                                onClick={() => {
                                    setWarningOpen(false);
                                    setKofiOpen(true);
                                }}
                                className="px-4 py-2 text-sm rounded-lg bg-blue-600 text-white
                                           hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Continue
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ================= KO-FI MODAL ================= */}
            {kofiOpen && (
                <div
                    className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50"
                    onClick={() => setKofiOpen(false)}
                >
                    <div
                        className="relative w-[420px] h-[620px] bg-white rounded-xl shadow-2xl overflow-hidden"
                        onClick={(e) => e.stopPropagation()}
                    >

                        <button
                            onClick={() => setKofiOpen(false)}
                            className="absolute top-3 right-3 text-slate-400 hover:text-slate-700 text-lg"
                        >
                            ✕
                        </button>

                        <iframe
                            title="Support the project on Ko-fi ☕"
                            src="https://ko-fi.com/alessandrobezerra/?hidefeed=true&widget=true&embed=true"
                            className="w-full h-full mt-3"
                        />

                    </div>
                </div>
            )}
        </>
    );
}