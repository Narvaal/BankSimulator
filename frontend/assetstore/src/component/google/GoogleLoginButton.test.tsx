import { describe, it, expect, vi } from "vitest";
import { waitFor } from "@testing-library/react";
import GoogleLoginButton from "./GoogleLoginButton";
import { renderWithProviders, mockFetch } from "../../test/helpers";
import { getToken } from "../../auth";

type CredentialCallback = (response: { credential: string; select_by: string }) => void;

function stubGoogleSdk() {
    let capturedCallback: CredentialCallback | null = null;
    const initialize = vi.fn((config: { callback: CredentialCallback }) => {
        capturedCallback = config.callback;
    });
    const renderButton = vi.fn();

    vi.stubGlobal("google", { accounts: { id: { initialize, renderButton } } });

    return { initialize, renderButton, callback: () => capturedCallback };
}

describe("GoogleLoginButton", () => {
    it("initializes the Google SDK and renders the button", async () => {
        mockFetch([]);
        const sdk = stubGoogleSdk();

        renderWithProviders(<GoogleLoginButton />);

        await waitFor(() => expect(sdk.initialize).toHaveBeenCalled());
        expect(sdk.renderButton).toHaveBeenCalled();
    });

    it("exchanges the Google credential for a session token", async () => {
        const calls = mockFetch([
            { url: "/auth/google", method: "POST", body: { token: "jwt-google" } },
        ]);
        const sdk = stubGoogleSdk();

        renderWithProviders(<GoogleLoginButton />);
        await waitFor(() => expect(sdk.initialize).toHaveBeenCalled());

        sdk.callback()!({ credential: "google-id-token", select_by: "btn" });

        await waitFor(() => expect(getToken()).toBe("jwt-google"));
        expect(calls[0].body).toEqual({ token: "google-id-token" });
    });
});
