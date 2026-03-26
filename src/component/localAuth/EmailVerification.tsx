import AuthLayout from "./AuthLayout.tsx";
import {useState} from "react";
import {EnvelopeIcon} from "@heroicons/react/24/solid";

async function handleResetPassword(email: string) {
    const res = await fetch("https://api.alessandro-bezerra.me/accounts/password/reset-request", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({email}),
    });

    if (!res.ok) {
        throw new Error("Email does not exist");
    }

    return await res.json();
}

function ForgotPassword() {
    const [email, setEmail] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setLoading(true);

        try {
            await handleResetPassword(email);
            setSuccess("Password reset email sent");
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
                    disabled={loading}
                    className="w-full rounded-lg bg-black py-2.5 text-sm font-medium text-white
                     hover:bg-gray-900 active:scale-[0.98] transition mb-4 disabled:opacity-60 disabled:cursor-not-allowed"
                >
                    {loading ? "Sending..." : "Send reset link"}
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