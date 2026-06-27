import {useNavigate} from "react-router-dom";

interface Props {
    onClose: () => void;
}

export default function AuthRequiredModal({onClose}: Props) {
    const navigate = useNavigate();

    return (
        <div
            className="fixed inset-0 z-[100] flex items-center justify-center bg-black/40"
            onClick={onClose}
        >
            <div
                className="bg-white rounded-2xl shadow-xl w-[400px] p-6"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-center gap-3 mb-3">
                    <div className="flex items-center justify-center w-9 h-9 rounded-full bg-zinc-100 shrink-0">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                             strokeWidth={1.5} stroke="currentColor" className="w-4.5 h-4.5 text-zinc-600">
                            <path strokeLinecap="round" strokeLinejoin="round"
                                  d="M16.5 10.5V6.75a4.5 4.5 0 1 0-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 0 0 2.25-2.25v-6.75a2.25 2.25 0 0 0-2.25-2.25H6.75a2.25 2.25 0 0 0-2.25 2.25v6.75a2.25 2.25 0 0 0 2.25 2.25Z"/>
                        </svg>
                    </div>
                    <h2 className="text-base font-semibold text-zinc-900">Account required</h2>
                </div>

                <p className="text-sm text-zinc-500 mb-6">
                    You need an account to use this feature. It's free and takes less than a minute.
                </p>

                <div className="flex justify-end gap-2">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-sm rounded-lg border border-zinc-200 text-zinc-600 hover:bg-zinc-50 transition-colors"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={() => navigate("/register")}
                        className="px-4 py-2 text-sm rounded-lg bg-zinc-900 text-white hover:bg-zinc-700 transition-colors"
                    >
                        Create account
                    </button>
                </div>
            </div>
        </div>
    );
}
