import { describe, it, expect } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ResetPassword from "./ResetPassword";
import EmailVerification from "./EmailVerification";
import { renderWithProviders, mockFetch } from "../../test/helpers";

describe("ResetPassword", () => {
    it("resets the password with the token from the URL", async () => {
        const calls = mockFetch([{ url: "/accounts/password/reset", method: "POST", body: {} }]);
        window.history.pushState({}, "", "/reset-password?token=tok-9");
        renderWithProviders(<ResetPassword />);

        await userEvent.type(screen.getByPlaceholderText("New password"), "Str0ng!Pass");
        await userEvent.type(screen.getByPlaceholderText("Confirm password"), "Str0ng!Pass");
        await userEvent.click(screen.getByRole("button", { name: "Reset password" }));

        await waitFor(() =>
            expect(calls[0].body).toEqual({ token: "tok-9", password: "Str0ng!Pass" })
        );
    });

    it("rejects weak password and mismatched confirmation", async () => {
        const calls = mockFetch([]);
        renderWithProviders(<ResetPassword />);

        await userEvent.type(screen.getByPlaceholderText("New password"), "weak");
        await userEvent.click(screen.getByRole("button", { name: "Reset password" }));
        expect(
            await screen.findByText("Password does not meet requirements")
        ).toBeInTheDocument();

        await userEvent.clear(screen.getByPlaceholderText("New password"));
        await userEvent.type(screen.getByPlaceholderText("New password"), "Str0ng!Pass");
        await userEvent.type(screen.getByPlaceholderText("Confirm password"), "Different!1");
        await userEvent.click(screen.getByRole("button", { name: "Reset password" }));
        expect(await screen.findByText("Passwords do not match")).toBeInTheDocument();

        expect(calls).toHaveLength(0);
    });

    it("shows error for invalid or expired token", async () => {
        mockFetch([{ url: "/accounts/password/reset", method: "POST", status: 400, body: {} }]);
        renderWithProviders(<ResetPassword />);

        await userEvent.type(screen.getByPlaceholderText("New password"), "Str0ng!Pass");
        await userEvent.type(screen.getByPlaceholderText("Confirm password"), "Str0ng!Pass");
        await userEvent.click(screen.getByRole("button", { name: "Reset password" }));

        expect(await screen.findByText("Invalid or expired token")).toBeInTheDocument();
    });
});

describe("EmailVerification (forgot password)", () => {
    it("sends the reset link and starts a cooldown", async () => {
        const calls = mockFetch([
            { url: "/accounts/password/reset-request", method: "POST", body: {} },
        ]);
        renderWithProviders(<EmailVerification />);

        await userEvent.type(screen.getByPlaceholderText("Email"), "john@test.com");
        await userEvent.click(screen.getByRole("button", { name: "Send reset link" }));

        expect(await screen.findByText("Password reset email sent")).toBeInTheDocument();
        expect(calls[0].body).toEqual({ email: "john@test.com" });
        expect(screen.getByRole("button", { name: /try again in/i })).toBeDisabled();
        expect(localStorage.getItem("resetCooldown")).not.toBeNull();
    });

    it("shows error when the email does not exist", async () => {
        mockFetch([
            { url: "/accounts/password/reset-request", method: "POST", status: 404, body: {} },
        ]);
        renderWithProviders(<EmailVerification />);

        await userEvent.type(screen.getByPlaceholderText("Email"), "ghost@test.com");
        await userEvent.click(screen.getByRole("button", { name: "Send reset link" }));

        expect(await screen.findByText("Email does not exist")).toBeInTheDocument();
    });

    it("restores a pending cooldown from localStorage", () => {
        mockFetch([]);
        localStorage.setItem("resetCooldown", String(Date.now() + 30_000));

        renderWithProviders(<EmailVerification />);

        expect(screen.getByRole("button", { name: /try again in/i })).toBeDisabled();
    });
});
