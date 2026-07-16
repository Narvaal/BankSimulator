import { describe, it, expect } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SearchPage from "./SearchPage";
import ProfilePage from "../profile/ProfilePage";
import { renderWithProviders, mockFetch, pageView, sampleUnit } from "../../test/helpers";

const profile = { accountId: 7, name: "Jane Roe", picture: null, accountNumber: "222-333-444" };

describe("SearchPage", () => {
    it("requires at least 2 characters", async () => {
        const calls = mockFetch([{ url: "/accounts/me", status: 401, body: {} }]);
        renderWithProviders(<SearchPage />);

        await userEvent.type(screen.getByPlaceholderText("Type a name..."), "j");

        expect(
            await screen.findByText(/type at least 2 characters/i)
        ).toBeInTheDocument();
        expect(calls.some((c) => c.url.includes("/accounts/search"))).toBe(false);
    });

    it("searches with debounce and links to the profile", async () => {
        const calls = mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/accounts/search", body: pageView([profile]) },
        ]);
        renderWithProviders(<SearchPage />);

        await userEvent.type(screen.getByPlaceholderText("Type a name..."), "jane");

        expect(await screen.findByText("Jane Roe")).toBeInTheDocument();
        expect(screen.getByText("#222-333-444")).toBeInTheDocument();
        expect(screen.getByText("Jane Roe").closest("a")).toHaveAttribute(
            "href",
            "/profile/7"
        );
        await waitFor(() => expect(calls.some((c) => c.url.includes("q=jane"))).toBe(true));
    });

    it("shows the empty state and search errors", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/accounts/search", body: pageView([]) },
        ]);
        renderWithProviders(<SearchPage />);

        await userEvent.type(screen.getByPlaceholderText("Type a name..."), "ghost");
        expect(await screen.findByText(/no profiles found for "ghost"/i)).toBeInTheDocument();
    });
});

describe("ProfilePage", () => {
    it("renders the public profile with read-only inventory", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/accounts/7/profile", body: profile },
            { url: /\/artifact-units\?ownerId=7/, body: pageView([sampleUnit]) },
        ]);
        renderWithProviders(<ProfilePage />, { route: "/profile/7", path: "/profile/:accountId" });

        expect(await screen.findByText("Jane Roe")).toBeInTheDocument();
        expect(await screen.findByText("Apple Vision Pro")).toBeInTheDocument();
        // iniciais no avatar sem foto
        expect(screen.getByText("JR")).toBeInTheDocument();
    });

    it("shows not-found error with a way back", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/accounts/99/profile", status: 404, body: {} },
        ]);
        renderWithProviders(<ProfilePage />, { route: "/profile/99", path: "/profile/:accountId" });

        expect(await screen.findByText("Profile not found")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Go back" })).toBeInTheDocument();
    });
});
