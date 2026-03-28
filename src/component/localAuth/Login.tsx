import GoogleLoginButton from "../google/GoogleLoginButton.tsx";
import AuthLayout from "./AuthLayout.tsx";
import {useState} from "react";
import {EnvelopeIcon, EyeIcon, EyeSlashIcon, LockClosedIcon} from "@heroicons/react/24/solid";

async function handleLogin(email: string, password: string) {
    const res = await fetch("https://api.alessandro-bezerra.me/auth/login", {
        method: "POST",
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({email, password}),
    });

    if (!res.ok) {
        const data = await res.json();

        if (data.code === "EMAIL_NOT_VERIFIED") {
            throw new Error("EMAIL_NOT_VERIFIED");
        }

        throw new Error("INVALID_CREDENTIALS");
    }

    return;
}

async function resendVerification(email: string) {
    setResending(true);

    try {
        const res = await fetch("https://api.alessandro-bezerra.me/auth/resend-verification", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email }),
        });

        if (!res.ok) throw new Error();

        setResent(true);

    } catch {
        alert("Failed to resend email");
    } finally {
        setResending(false);
    }
}

function Login() {
    const [resending, setResending] = useState(false);
    const [resent, setResent] = useState(false);
    const [show, setShow] = useState(false);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setLoading(true);

        try {
            await handleLogin(email, password);

            setSuccess("Login successful");

            window.location.href = "/inventory";

        } catch (err: any) {

            if (err.message === "EMAIL_NOT_VERIFIED") {
                setError("Please verify your email before logging in.");
            } else {
                setError("Invalid email or password");
            }

        } finally {
            setLoading(false);
        }
    }

   return (
       <AuthLayout title="Login">

           <form onSubmit={onSubmit}>

               {error && error !== "EMAIL_NOT_VERIFIED" && (
                   <p className="text-red-500 text-xs text-center mb-4">
                       {error}
                   </p>
               )}

               {error === "EMAIL_NOT_VERIFIED" && (
                   <div className="bg-amber-50 border border-amber-200 text-amber-700 text-xs p-3 rounded-lg mb-4 text-center">

                       <p className="mb-2">
                           Your email is not verified.
                       </p>

                       {!resent ? (
                           <button
                               type="button"
                               onClick={() => resendVerification(email)}
                               disabled={resending}
                               className="font-medium underline hover:text-amber-900 disabled:opacity-50"
                           >
                               {resending ? "Sending..." : "Resend verification email"}
                           </button>
                       ) : (
                           <span className="text-emerald-600 font-medium">
                               Email sent successfully ✓
                           </span>
                       )}

                   </div>
               )}

               {success && (
                   <p className="text-emerald-600 text-xs text-center mb-4">
                       {success}
                   </p>
               )}

               <label className="relative block mb-4">
                   <EnvelopeIcon className="size-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"/>
                   <input
                       type="email"
                       placeholder="Email"
                       value={email}
                       className="w-full rounded-lg bg-gray-100 pl-10 pr-3 py-2.5 text-sm
                       outline-none ring-1 ring-gray-200
                       focus:ring-2 focus:ring-black transition"
                       onChange={(e) => setEmail(e.target.value)}
                   />
               </label>

               <label className="relative block mb-1">
                   <LockClosedIcon className="size-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"/>

                   <input
                       type={show ? "text" : "password"}
                       placeholder="Password"
                       value={password}
                       className="w-full rounded-lg bg-gray-100 pl-10 pr-10 py-2.5 text-sm
                       outline-none ring-1 ring-gray-200
                       focus:ring-2 focus:ring-black transition"
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

               <a
                   href="/forgot-password"
                   className="block text-xs text-right text-gray-500 hover:text-black transition mb-4"
               >
                   Forgot password?
               </a>

               <button
                   type="submit"
                   disabled={loading}
                   className="w-full rounded-lg bg-black py-2.5 text-sm font-medium text-white
                   hover:bg-gray-900 active:scale-[0.98] transition mb-4 disabled:opacity-60 disabled:cursor-not-allowed"
               >
                   {loading ? "Signing in..." : "Login"}
               </button>
           </form>

           <div className="block text-xs text-gray-500 text-center">
               Don't have an account?{" "}
               <a className="underline text-black" href="/register">
                   Sign up
               </a>
           </div>

           <div className="flex items-center gap-2 my-4">
               <hr className="flex-1 border-dashed border-gray-300"/>
               <span className="text-xs text-gray-500 whitespace-nowrap">
                   Or sign in with
               </span>
               <hr className="flex-1 border-dashed border-gray-300"/>
           </div>

           <div className="grid grid-cols-1">
               <GoogleLoginButton/>
           </div>

       </AuthLayout>
   );
}

export default Login;