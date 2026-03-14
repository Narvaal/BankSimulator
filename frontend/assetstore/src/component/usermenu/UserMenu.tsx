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

    return (
        <>
            <header className="fixed top-0 left-0 w-full z-50 border-slate-200 bg-white/70 border backdrop-blur">
                <div className="w-full px-6 h-16 flex items-center text-slate-800">

                    <div className="flex-1"></div>

                    <div className="absolute left-1/2 -translate-x-1/2">
                        <CountdownTimer targetDate={nextFreeAssetAt}/>
                    </div>

                    <div className="flex-1 flex justify-end items-center gap-4">

                        <div className="px-3 py-1 rounded-lg bg-emerald-100 text-emerald-700 text-sm font-semibold">
                            Balance: ${balance.toFixed(2)}
                        </div>

                        <button
                            onClick={() => setKofiOpen(true)}
                            className="flex items-center gap-2 px-3 py-1 rounded-lg bg-blue-500 text-white text-sm font-semibold hover:bg-blue-600 transition"
                        >
                            Add balance
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