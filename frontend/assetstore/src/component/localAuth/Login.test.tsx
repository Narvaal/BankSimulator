import { describe, it, expect } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Login from "./Login";
import { renderWithProviders, mockFetch } from "../../test/helpers";
import { getToken } from "../../auth";

async function fillAndSubmit(email = "john@test.com", password = "Str0ng!Pass") {
    await userEvent.type(screen.getByPlaceholderText("Email"), email);
    await userEvent.type(screen.getByPlaceholderText("Password"), password);
    await userEvent.click(screen.getByRole("button", { name: "Login" }));
}

describe("Login", () => {
    it("authenticates and stores the token", async () => {
        const calls = mockFetch([
            { url: "/auth/login", method: "POST", body: { token: "jwt-123" } },
        ]);
        renderWithProviders(<Login />);

        await fillAndSubmit();

        await waitFor(() => expect(getToken()).toBe("jwt-123"));
        expect(calls[0].body).toEqual({ email: "john@test.com", password: "Str0ng!Pass" });
    });

    it("shows invalid credentials error", async () => {
        mockFetch([
            { url: "/auth/login", method: "POST", status: 400, body: { code: "INVALID_CREDENTIALS" } },
        ]);
        renderWithProviders(<Login />);

        await fillAndSubmit();

        expect(await screen.findByText("Invalid email or password")).toBeInTheDocument();
        expect(getToken()).toBeNull();
    });

    it("offers resend when email is not verified and resends it", async () => {
        const calls = mockFetch([
            { url: "/auth/login", method: "POST", status: 400, body: { code: "EMAIL_NOT_VERIFIED" } },
            { url: "/auth/resend-verification", method: "POST", body: {} },
        ]);
        renderWithProviders(<Login />);

        await fillAndSubmit();

        const resendButton = await screen.findByRole("button", {
            name: /resend verification email/i,
        });
        await userEvent.click(resendButton);

        await waitFor(() =>
            expect(calls.some((c) => c.url.includes("/auth/resend-verification"))).toBe(true)
        );
    });

    it("toggles password visibility", async () => {
        mockFetch([]);
        renderWithProviders(<Login />);
        const passwordInput = screen.getByPlaceholderText("Password");

        expect(passwordInput).toHaveAttribute("type", "password");
        // botão de olho é o primeiro type=button dentro do label da senha
        await userEvent.click(passwordInput.closest("label")!.querySelector("button")!);
        expect(passwordInput).toHaveAttribute("type", "text");
    });

    it("links to sign up", () => {
        mockFetch([]);
        renderWithProviders(<Login />);
        expect(screen.getByText(/sign up/i)).toBeInTheDocument();
    });
});
