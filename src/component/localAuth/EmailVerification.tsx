import AuthLayout from "./AuthLayout.tsx";
import {useEffect, useState} from "react";
import {EnvelopeIcon} from "@heroicons/react/24/solid";

const COOLDOWN_KEY = "resetCooldown";

async function handleResetPassword(email: string) {
    const res = await fetch("https://api.alessandro-bezerra.me/accounts/password/reset-request", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({email}),
    });

    if (!res.ok) {
        throw new Error("Email does not exist");
    }
}

function ForgotPassword() {
    const [email, setEmail] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [cooldown, setCooldown] = useState(0);

    // 🔥 carregar cooldown do localStorage
    useEffect(() => {
        const saved = localStorage.getItem(COOLDOWN_KEY);
        if (!saved) return;

        const expiresAt = parseInt(saved, 10);
        const remaining = Math.floor((expiresAt - Date.now()) / 1000);

        if (remaining > 0) {
            setCooldown(remaining);
        } else {
            localStorage.removeItem(COOLDOWN_KEY);
        }
    }, []);

    useEffect(() => {
        if (cooldown <= 0) return;

        const timer = setTimeout(() => {
            setCooldown((prev) => prev - 1);
        }, 1000);

        return () => clearTimeout(timer);
    }, [cooldown]);

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();

        if (cooldown > 0) return;

        setError(null);
        setSuccess(null);
        setLoading(true);

        try {
            await handleResetPassword(email);

            setSuccess("Password reset email sent");

            const expiresAt = Date.now() + 30 * 1000;

            localStorage.setItem(COOLDOWN_KEY, expiresAt.toString());

            setCooldown(30);

        } catch {
            setError("Email does not exist");
        } finally {
            setLoading(false);
        }
    }

    return (
        <AuthLayout title="Forgot Password">

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

                <label className="relative block mb-4">
                    <EnvelopeIcon
                        className="size-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"/>
                    <input
                        type="email"
                        placeholder="Email"
                        className="w-full rounded-lg bg-gray-100 pl-10 pr-3 py-2.5 text-sm
                       outline-none ring-1 ring-gray-200
                       focus:ring-2 focus:ring-black transition"
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </label>

                <button
                    type="submit"
                    disabled={loading || cooldown > 0}
                    className="w-full rounded-lg bg-black py-2.5 text-sm font-medium text-white
                     hover:bg-gray-900 active:scale-[0.98] transition mb-4 disabled:opacity-60 disabled:cursor-not-allowed"
                >
                    {loading
                        ? "Sending..."
                        : cooldown > 0
                            ? `Try again in ${cooldown}s`
                            : "Send reset link"}
                </button>
            </form>

            <div className="block text-xs text-gray-500 text-center">
                Remember your password?{" "}
                <a className="underline text-black" href="/login">
                    Back to login
                </a>
            </div>

        </AuthLayout>
    );
}

export default ForgotPassword;