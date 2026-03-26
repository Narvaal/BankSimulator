import AuthLayout from "./AuthLayout.tsx";
import {
    EyeIcon,
    EyeSlashIcon,
    LockClosedIcon
} from "@heroicons/react/24/solid";
import {useState} from "react";
import {AnimatePresence, motion} from "framer-motion";

type PassRules = {
    length: boolean;
    uppercase: boolean;
    number: boolean;
    special: boolean;
};

function getPassRules(pass: string): PassRules {
    return {
        length: pass.length >= 8,
        uppercase: /[A-Z]/.test(pass),
        number: /[0-9]/.test(pass),
        special: /[!@#$%^&*(),.?":{}|<>_\-\\[\];'/+=`~]/.test(pass),
    };
}

function isPasswordValid(pass: string) {
    const r = getPassRules(pass);
    return r.length && r.uppercase && r.number && r.special;
}

function Rule({ok, text}: { ok: boolean, text: string }) {
    return (
        <div className={`flex items-center gap-2 text-xs ${ok ? "text-green-600" : "text-red-500"}`}>
            <span className="font-bold">{ok ? "✓" : "×"}</span>
            <span>{text}</span>
        </div>
    );
}

async function handleResetPassword(token: string, password: string) {
    const res = await fetch("https://api.alessandro-bezerra.me/accounts/password/reset", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            token,
            password
        }),
    });

    if (!res.ok) {
        throw new Error("Invalid or expired token");
    }
}

function ResetPassword() {
    const [show, setShow] = useState(false);

    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    const [passFocused, setPassFocused] = useState(false);
    const [loading, setLoading] = useState(false);

    const rules = getPassRules(password);

    const params = new URLSearchParams(window.location.search);
    const token = params.get("token") || "";

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);
        setSuccess(null);

        if (!isPasswordValid(password)) {
            setError("Password does not meet requirements");
            return;
        }

        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        try {
            setLoading(true);
            await handleResetPassword(token, password);
            setSuccess("Password successfully reset");
        } catch {
            setError("Invalid or expired token");
        } finally {
            setLoading(false);
        }
    }

    return (
        <AuthLayout title="Create new password">

            <form onSubmit={onSubmit}>

                {error && (
                    <p className="text-red-500 text-xs text-center mb-4">
                        {error}
                    </p>
                )}

                {success && (
                    <p className="text-green-500 text-xs text-center mb-4">
                        {success}
                    </p>
                )}

                {/* NEW PASSWORD */}
                <label className="relative block mb-2">
                    <LockClosedIcon className="size-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"/>

                    <input
                        type={show ? "text" : "password"}
                        placeholder="New password"
                        className="w-full rounded-lg bg-gray-100 pl-10 pr-10 py-2.5 text-sm
                       outline-none ring-1 ring-gray-200
                       focus:ring-2 focus:ring-black transition"
                        value={password}
                        onFocus={() => setPassFocused(true)}
                        onBlur={() => setPassFocused(false)}
                        onChange={(e) => setPassword(e.target.value)}
                    />

                    <button
                        type="button"
                        onClick={() => setShow(!show)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-black transition"
                    >
                        {show ? <EyeSlashIcon className="size-5"/> : <EyeIcon className="size-5"/>}
                    </button>
                </label>

                {/* CONFIRM PASSWORD */}
                <label className="relative block mb-2">
                    <LockClosedIcon className="size-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"/>

                    <input
                        type={show ? "text" : "password"}
                        placeholder="Confirm password"
                        className="w-full rounded-lg bg-gray-100 pl-10 pr-10 py-2.5 text-sm
                       outline-none ring-1 ring-gray-200
                       focus:ring-2 focus:ring-black transition"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                </label>

                <AnimatePresence initial={false}>
                    {passFocused && (
                        <motion.div
                            key="password-rules"
                            initial={{opacity: 0, y: -10, height: 0}}
                            animate={{opacity: 1, y: 0, height: "auto"}}
                            exit={{opacity: 0, y: -10, height: 0}}
                            transition={{duration: 0.30, ease: "easeOut"}}
                            className="overflow-hidden"
                        >
                            <Rule ok={rules.length} text="At least 8 characters"/>
                            <Rule ok={rules.number} text="At least one number (0-9)"/>
                            <Rule ok={rules.uppercase} text="At least one uppercase (A-Z)"/>
                            <Rule ok={rules.special} text="At least one symbol (!@#$...)"/>
                        </motion.div>
                    )}
                </AnimatePresence>

                <button
                    type="submit"
                    disabled={loading}
                    className="w-full mt-4 rounded-lg bg-black py-2.5 text-sm font-medium text-white
                     hover:bg-gray-900 active:scale-[0.98] transition disabled:opacity-60 disabled:cursor-not-allowed"
                >
                    {loading ? "Resetting..." : "Reset password"}
                </button>

            </form>

        </AuthLayout>
    );
}

export default ResetPassword;