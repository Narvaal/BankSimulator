import { describe, it, expect } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Reward from "./Reward";
import {
    renderWithProviders,
    mockFetch,
    sampleAccount,
    sampleMetadata,
    type FetchHandler,
} from "../../test/helpers";

const bundle = { id: "b1", identifier: "weekly-2026-W29", createdAt: "2026-07-13T08:00:00Z" };
const artifact = { id: 30, metadata: sampleMetadata, totalSupply: 5, createdAt: "2026-07-13T08:00:00Z" };

const baseHandlers = (authenticated: boolean): FetchHandler[] => [
    {
        url: "/accounts/me",
        status: authenticated ? 200 : 401,
        body: authenticated ? sampleAccount : {},
    },
    { url: "/artifacts/bundles/b1/items", body: [artifact] },
    { url: /\/artifacts\/bundles\?page=0/, body: [bundle] },
    { url: /\/artifacts\/bundles\?page=/, body: [] },
];

describe("Reward", () => {
    it("loads bundles and auto-expands the first one", async () => {
        mockFetch(baseHandlers(false));
        renderWithProviders(<Reward />);

        expect(await screen.findByText("weekly-2026-W29")).toBeInTheDocument();
        expect(await screen.findByText("Apple Vision Pro")).toBeInTheDocument();
        expect(screen.getByText("5 left")).toBeInTheDocument();
        expect(screen.getByText(/upcoming bundle/i)).toBeInTheDocument();
    });

    it("collapses and re-expands a bundle on header click", async () => {
        mockFetch(baseHandlers(false));
        renderWithProviders(<Reward />);
        await screen.findByText("Apple Vision Pro");

        await userEvent.click(screen.getByText("weekly-2026-W29"));
        await waitFor(() =>
            expect(screen.queryByText("Apple Vision Pro")).not.toBeInTheDocument()
        );

        await userEvent.click(screen.getByText("weekly-2026-W29"));
        expect(await screen.findByText("Apple Vision Pro")).toBeInTheDocument();
    });

    it("asks anonymous users to sign in before claiming", async () => {
        mockFetch(baseHandlers(false));
        renderWithProviders(<Reward />);

        await userEvent.click(await screen.findByText("Apple Vision Pro"));
        await userEvent.click(await screen.findByRole("button", { name: "Sign in to claim" }));

        expect(await screen.findByText("Account required")).toBeInTheDocument();
    });

    it("claims an artifact and decrements the remaining supply", async () => {
        const calls = mockFetch([
            { url: "/artifacts/claim", method: "POST", body: {} },
            ...baseHandlers(true),
        ]);
        renderWithProviders(<Reward />);

        await userEvent.click(await screen.findByText("Apple Vision Pro"));
        await userEvent.click(await screen.findByRole("button", { name: "Confirm Claim" }));

        expect(await screen.findByText("Artifact claimed successfully!")).toBeInTheDocument();
        expect(calls.some((c) => c.url.includes("/artifacts/claim"))).toBe(true);
        expect(await screen.findByText("4 left")).toBeInTheDocument();
    });

    it("shows an error when the claim fails (cooldown)", async () => {
        mockFetch([
            { url: "/artifacts/claim", method: "POST", status: 403, body: {} },
            ...baseHandlers(true),
        ]);
        renderWithProviders(<Reward />);

        await userEvent.click(await screen.findByText("Apple Vision Pro"));
        await userEvent.click(await screen.findByRole("button", { name: "Confirm Claim" }));

        expect(await screen.findByText("Failed to claim artifact.")).toBeInTheDocument();
    });

    it("marks sold-out artifacts as disabled", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/artifacts/bundles/b1/items", body: [{ ...artifact, totalSupply: 0 }] },
            { url: /\/artifacts\/bundles\?page=0/, body: [bundle] },
            { url: /\/artifacts\/bundles\?page=/, body: [] },
        ]);
        renderWithProviders(<Reward />);

        expect(await screen.findByText("Sold Out")).toBeInTheDocument();
    });
});
