/// <reference types="@types/google.accounts" />
import { useEffect, useRef } from "react";

type GoogleCredentialResponse = {
    credential: string;
    select_by: string;
};

async function handleGoogleLogin(response: GoogleCredentialResponse) {
    const res = await fetch("http://localhost:8080/auth/google", {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            token: response.credential,
        }),
    });

    const data = await res.json();
    console.log("Login success:", data);
}


function GoogleLoginButton() {
    const googleBtnRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!window.google?.accounts?.id || !googleBtnRef.current) return;

        window.google.accounts.id.initialize({
            client_id: "1002611612778-n0or7ldrme26ugbgmiccfsc5s1ctif9e.apps.googleusercontent.com",
            callback: (response: GoogleCredentialResponse) => {
                handleGoogleLogin(response);
            },
        });
        
        window.google.accounts.id.renderButton(googleBtnRef.current, {
            theme: "outline",
            size: "large",
            text: "signin_with",
            shape: "rectangular",
            logo_alignment: "left",
            width: "100%",
        });
    }, []);

    return <div ref={googleBtnRef} className="w-full" />;
}

export default GoogleLoginButton;
