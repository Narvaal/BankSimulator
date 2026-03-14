import AuthLayout from "./AuthLayout.tsx";
import {EnvelopeIcon, EyeIcon, EyeSlashIcon, LockClosedIcon, UserIcon} from "@heroicons/react/24/solid";
import {useState} from "react";
import {AnimatePresence, motion} from "framer-motion";
import GoogleLoginButton from "../google/GoogleLoginButton.tsx";

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

function checkEmail(email: string): string | null {
    if (!email) return "Email is required";
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!regex.test(email)) return "Invalid email format";
    return null;
}

function checkName(name: string): string | null {
    if (!name) return "Name is required";
    if (name.length < 3) return "Name too short";
    if (!/^[a-zA-ZÀ-ÿ' ]+$/.test(name)) return "Invalid characters in name";
    return null;
}

async function handleAccountCreation(name: string, email: string, password: string) {
    const res = await fetch("http://BankSimulator.us-east-2.elasticbeanstalk.com/accounts", {
        method: "POST",
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({name, email, password}),
    });

    if (!res.ok) {
        throw new Error("Email already registered");
    }

    return await res.json();
}

function Rule({ok, text}: { ok: boolean, text: string }) {
    return (
        <div className={`flex items-center gap-2 text-xs ${ok ? "text-green-600" : "text-red-500"}`}>
            <span className="font-bold">{ok ? "✓" : "×"}</span>
            <span>{text}</span>
        </div>
    );
}

function CreateAccount() {
    const [show, setShow] = useState(false);

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [nameError, setNameError] = useState<string | null>(null);
    const [emailError, setEmailError] = useState<string | null>(null);
    const [serverError, setServerError] = useState<string | null>(null);

    const [passFocused, setPassFocused] = useState(false);
    const [loading, setLoading] = useState(false);

    const rules = getPassRules(password);

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setServerError(null);

        const nErr = checkName(name);
        const eErr = checkEmail(email);

        setNameError(nErr);
        setEmailError(eErr);

        if (nErr || eErr || !isPasswordValid(password)) return;

        try {
            setLoading(true);
            await handleAccountCreation(name, email, password);
        } catch {
            setServerError("Email already registered");
        } finally {
            setLoading(false);
        }
    }

    return (
        <AuthLayout title={"Create a free account"} description={"Sing in for a complete experience"}>
            <form onSubmit={onSubmit}>

                {serverError && <p className="text-red-500 text-xs text-center mb-4">{serverError}</p>}

                <GoogleLoginButton/>

                <div className="flex items-center gap-2 my-4">
                    <hr className="flex-1 border-dashed border-gray-300"/>
                    <span className="text-xs text-gray-500 whitespace-nowrap">Or create an account</span>
                    <hr className="flex-1 border-dashed border-gray-300"/>
                </div>

                {nameError && <p className="text-red-500 text-xs mb-1">{nameError}</p>}

                <label className="relative block mb-4">
                    <UserIcon className="size-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"/>
                    <input
                        placeholder="Name"
                        className="w-full rounded-lg bg-gray-100 pl-10 pr-3 py-2.5 text-sm outline-none ring-1 ring-gray-200 focus:ring-2 focus:ring-black transition"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        onBlur={(e) => setNameError(checkName(e.target.value))}
                    />
                </label>

                {emailError && <p className="text-red-500 text-xs mb-1">{emailError}</p>}

                <label className="relative block mb-4">
                    <EnvelopeIcon className="size-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"/>
                    <input
                        type="email"
                        placeholder="Email"
                        className="w-full rounded-lg bg-gray-100 pl-10 pr-3 py-2.5 text-sm outline-none ring-1 ring-gray-200 focus:ring-2 focus:ring-black transition"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        onBlur={(e) => setEmailError(checkEmail(e.target.value))}
                    />
                </label>

                <label className="relative block mb-2">
                    <LockClosedIcon className="size-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"/>
                    <input
                        type={show ? "text" : "password"}
                        placeholder="Password"
                        className="w-full rounded-lg bg-gray-100 pl-10 pr-10 py-2.5 text-sm outline-none ring-1 ring-gray-200 focus:ring-2 focus:ring-black transition"
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

                <div className="block text-xs text-gray-500 text-center p-3">
                    Already have an account? <a className="underline text-black" href="/singin">Sing in</a>
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    className="w-full rounded-lg bg-black py-2.5 text-sm font-medium text-white hover:bg-gray-800 active:scale-[0.98] transition disabled:opacity-60 disabled:cursor-not-allowed"
                >
                    {loading ? "Creating..." : "Sign up"}
                </button>

            </form>
        </AuthLayout>
    );
}

export default CreateAccount;
