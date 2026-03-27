/// <reference types="@types/google.accounts" />
import { useEffect, useRef } from "react";

type GoogleCredentialResponse = {
  credential: string;
  select_by: string;
};

async function handleGoogleLogin(response: GoogleCredentialResponse) {
  const res = await fetch("https://api.alessandro-bezerra.me/auth/google", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      token: response.credential,
    }),
  });

  if (!res.ok) {
    throw new Error("Google email error");
  }

  window.location.href = "/inventory";
}

function loadGoogleScript(): Promise<void> {
  return new Promise((resolve) => {
    if (window.google?.accounts?.id) {
      resolve();
      return;
    }

    const existingScript = document.querySelector(
      'script[src="https://accounts.google.com/gsi/client"]'
    );

    if (existingScript) {
      existingScript.addEventListener("load", () => resolve());
      return;
    }

    const script = document.createElement("script");
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();

    document.body.appendChild(script);
  });
}

function GoogleLoginButton() {
  const googleBtnRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let cancelled = false;

    loadGoogleScript().then(() => {
      if (cancelled) return;
      if (!googleBtnRef.current) return;

      googleBtnRef.current.innerHTML = "";

      window.google.accounts.id.initialize({
        client_id:
          "1002611612778-n0or7ldrme26ugbgmiccfsc5s1ctif9e.apps.googleusercontent.com",
        callback: handleGoogleLogin,
      });

      window.google.accounts.id.renderButton(googleBtnRef.current, {
        theme: "outline",
        size: "large",
        type: "standard",
        width: googleBtnRef.current.offsetWidth || 300,
      });
    });

    return () => {
      cancelled = true;
    };
  }, []);

  return <div ref={googleBtnRef} className="w-full" />;
}

export default GoogleLoginButton;