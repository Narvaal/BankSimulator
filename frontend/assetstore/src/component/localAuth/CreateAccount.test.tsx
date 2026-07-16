import { describe, it, expect } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import CreateAccount from "./CreateAccount";
import { renderWithProviders, mockFetch } from "../../test/helpers";

async function fill(name: string, email: string, password: string) {
    if (name) await userEvent.type(screen.getByPlaceholderText("Name"), name);
    if (email) await userEvent.type(screen.getByPlaceholderText("Email"), email);
    if (password) await userEvent.type(screen.getByPlaceholderText("Password"), password);
}

describe("CreateAccount", () => {
    it("creates an account and shows verification notice", async () => {
        const calls = mockFetch([{ url: "/accounts", method: "POST", body: {} }]);
        renderWithProviders(<CreateAccount />);

        await fill("John Doe", "john@test.com", "Str0ng!Pass");
        await userEvent.click(screen.getByRole("button", { name: "Sign up" }));

        expect(
            await screen.findByText(/account created! check your email/i)
        ).toBeInTheDocument();
        expect(calls[0].body).toEqual({
            name: "John Doe",
            email: "john@test.com",
            password: "Str0ng!Pass",
        });
    });

    it("validates name and email before submitting", async () => {
        const calls = mockFetch([]);
        renderWithProviders(<CreateAccount />);

        await fill("Jo", "not-an-email", "Str0ng!Pass");
        await userEvent.click(screen.getByRole("button", { name: "Sign up" }));

        expect(await screen.findByText("Name too short")).toBeInTheDocument();
        expect(screen.getByText("Invalid email format")).toBeInTheDocument();
        expect(calls).toHaveLength(0);
    });

    it("requires all password rules before submitting", async () => {
        const calls = mockFetch([]);
        renderWithProviders(<CreateAccount />);

        await fill("John Doe", "john@test.com", "weakpass");
        await userEvent.click(screen.getByRole("button", { name: "Sign up" }));

        await waitFor(() => expect(calls).toHaveLength(0));
    });

    it("shows password rules while typing", async () => {
        mockFetch([]);
        renderWithProviders(<CreateAccount />);

        const password = screen.getByPlaceholderText("Password");
        await userEvent.click(password);
        await userEvent.type(password, "Abc1!");

        expect(await screen.findByText(/at least 8/i)).toBeInTheDocument();
    });

    it("surfaces server error when email is taken", async () => {
        mockFetch([{ url: "/accounts", method: "POST", status: 409, body: {} }]);
        renderWithProviders(<CreateAccount />);

        await fill("John Doe", "john@test.com", "Str0ng!Pass");
        await userEvent.click(screen.getByRole("button", { name: "Sign up" }));

        expect(await screen.findByText(/email already registered/i)).toBeInTheDocument();
    });
});
