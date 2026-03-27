import { useState } from "react";
import CountdownTimer from "../util/CountdownTimer.tsx";
import { UserIcon } from "@heroicons/react/24/outline";
import { HeartIcon } from "@heroicons/react/24/solid";

export interface Account {
    balance: number;
    nextFreeAssetAt: string;
    name: string;
    imageUrl: string;
}

export default function Navbar({ balance, nextFreeAssetAt, name, imageUrl }: Account) {

    const [kofiOpen, setKofiOpen] = useState(false);
    const [warningOpen, setWarningOpen] = useState(false);
    const [accepted, setAccepted] = useState(false);

    return (
        <>
            <header className="fixed top-0 left-0 w-full z-50 border-b border-slate-200 bg-white/80 backdrop-blur-md">
                <div className="w-full px-6 h-16 flex items-center text-slate-800">

                    <div className="flex-1 flex items-center gap-3">
                        <img
                            src="/icons/RareLines.png"
                            alt="Rare Lines"
                            className="h-9 w-auto object-contain drop-shadow-sm"
                        />
                        <span className="font-semibold text-slate-900 tracking-tight">
                            Rare Lines
                        </span>
                    </div>

                    <div className="flex-1 flex justify-center">
                        <div className="px-4 py-1.5 text-slate-600 text-xs font-medium">
                            <CountdownTimer
                                key={nextFreeAssetAt}
                                targetDate={nextFreeAssetAt}
                            />
                        </div>
                    </div>

                    <div className="flex-1 flex justify-end items-center gap-4">

                        <button
                          onClick={() => setWarningOpen(true)}
                          className="flex items-center"
                        >
                          <div className="flex items-center bg-emerald-50 border border-emerald-200 rounded-xl overflow-hidden shadow-sm hover:shadow-md transition">

                            <div className="px-4 py-2 text-sm text-emerald-700">
                              Balance:
                              <span className="ml-1 mr-1 font-semibold text-emerald-600">
                                ${balance.toFixed(2)}
                              </span>
                            </div>

                            <div className="flex items-stretch border-l border-emerald-200">
                              <div className="flex items-center px-3 h-9 active:scale-95 transition">
                                <HeartIcon className="w-4 h-4 text-emerald-600" />
                              </div>
                            </div>

                          </div>
                        </button>

                        <div className="flex items-center gap-2">
                            <span className="text-sm font-medium text-slate-700">
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

                            <p className="text-gray-700">
                                Before continuing, please read how this works:
                            </p>

                            <p>
                                • After clicking <b>Continue</b>, a Ko-fi window will open.
                                You will need to <b>log in</b>, choose a donation amount, confirm the payment,
                                and enter your email.
                            </p>

                            <p>
                                • Once the payment is completed, your balance will be credited automatically
                                to your account using your email.
                            </p>

                            <p>
                                • This app only accepts donations in <b>USD</b>. Any other currency may be lost during conversion.
                            </p>

                            <p>
                                • You <b>must use the same email</b> in Ko-fi as your account here.
                                Otherwise, your balance <b>cannot be credited</b>.
                            </p>

                            <p>
                                • Due to infrastructure and transaction costs, it's not feasible to allow unlimited free usage.
                                A <b>small symbolic donation</b> is required to unlock asset purchases.
                            </p>

                            <p>
                                • All payments are handled securely by <b>Ko-fi</b>.
                                This app <b>does not process or store any payment information</b>, ensuring a 100% safe transaction.
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
                                Continue to Ko-fi
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