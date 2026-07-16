import { describe, it, expect } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Marketplace from "./Marketplace";
import {
    renderWithProviders,
    mockFetch,
    pageView,
    sampleAccount,
    sampleListing,
    type FetchHandler,
} from "../../test/helpers";

const listingsHandler = (overrides: Partial<FetchHandler> = {}): FetchHandler => ({
    url: /\/artifact-listings\?/,
    body: pageView([sampleListing]),
    ...overrides,
});

const baseHandlers = (authenticated: boolean): FetchHandler[] => [
    {
        url: "/accounts/me",
        status: authenticated ? 200 : 401,
        body: authenticated ? sampleAccount : {},
    },
    { url: "/price-history", body: [] },
    listingsHandler(),
];

describe("Marketplace", () => {
    it("renders listings for anonymous visitors with a sign-in header", async () => {
        mockFetch(baseHandlers(false));
        renderWithProviders(<Marketplace />);

        expect(await screen.findByText("Apple Vision Pro")).toBeInTheDocument();
        expect(screen.getByText("$99.50")).toBeInTheDocument();
        expect(screen.getByText("Sign in")).toBeInTheDocument();
    });

    it("applies the artifact filter from URL params with a removable chip", async () => {
        const calls = mockFetch(baseHandlers(false));
        renderWithProviders(<Marketplace />, {
            route: "/market?artifactId=30&artifactName=Apple%20Vision%20Pro",
            path: "/market",
        });

        await screen.findAllByText("Apple Vision Pro");
        await waitFor(() =>
            expect(calls.some((c) => c.url.includes("artifactId=30"))).toBe(true)
        );

        await userEvent.click(screen.getByLabelText("Clear artifact filter"));
        await waitFor(() =>
            expect(calls.at(-1)!.url.includes("artifactId=30")).toBe(false)
        );
    });

    it("searches by name with debounce and sorts by price", async () => {
        const calls = mockFetch(baseHandlers(false));
        renderWithProviders(<Marketplace />);
        await screen.findByText("Apple Vision Pro");

        await userEvent.type(screen.getByPlaceholderText("Search by name..."), "vision");
        await waitFor(
            () => expect(calls.some((c) => c.url.includes("q=vision"))).toBe(true),
            { timeout: 2000 }
        );

        await userEvent.click(screen.getByRole("button", { name: "Price ↑" }));
        await waitFor(() =>
            expect(calls.some((c) => c.url.includes("sort=price_asc"))).toBe(true)
        );

        await userEvent.click(screen.getByRole("button", { name: "Clear filters" }));
        await waitFor(() => expect(calls.at(-1)!.url.includes("sort=")).toBe(false));
    });

    it("filters by price range", async () => {
        const calls = mockFetch(baseHandlers(false));
        renderWithProviders(<Marketplace />);
        await screen.findByText("Apple Vision Pro");

        await userEvent.type(screen.getByPlaceholderText("Min $"), "10");
        await userEvent.type(screen.getByPlaceholderText("Max $"), "200");

        await waitFor(() =>
            expect(
                calls.some((c) => c.url.includes("minPrice=10") && c.url.includes("maxPrice=200"))
            ).toBe(true)
        );
    });

    it("asks anonymous buyers to sign in", async () => {
        mockFetch(baseHandlers(false));
        renderWithProviders(<Marketplace />);

        await userEvent.click(await screen.findByText("Apple Vision Pro"));
        await userEvent.click(await screen.findByRole("button", { name: "Sign in to buy" }));

        expect(await screen.findByText("Account required")).toBeInTheDocument();
    });

    async function waitForAuthenticatedGrid(calls: { url: string }[]) {
        // o grid recarrega quando o account resolve — espera a 2ª busca para clicar em nós estáveis
        await screen.findByText("John Doe");
        await waitFor(() =>
            expect(
                calls.filter((c) => c.url.includes("/artifact-listings")).length
            ).toBeGreaterThanOrEqual(2)
        );
    }

    it("completes a purchase for an authenticated user with balance", async () => {
        const calls = mockFetch([
            { url: "/purchase", method: "POST", body: {} },
            ...baseHandlers(true),
        ]);
        renderWithProviders(<Marketplace />);

        await waitForAuthenticatedGrid(calls);
        await userEvent.click(await screen.findByText("Apple Vision Pro"));
        expect(await screen.findByText("Purchase summary")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Confirm Purchase" }));

        expect(await screen.findByText("Purchase successful")).toBeInTheDocument();
        expect(
            calls.some((c) => c.url.includes("/artifact-listings/10/purchase") && c.method === "POST")
        ).toBe(true);
    });

    it("blocks purchase when balance is insufficient", async () => {
        const calls = mockFetch([
            { url: "/accounts/me", body: { ...sampleAccount, balance: 1 } },
            { url: "/price-history", body: [] },
            listingsHandler(),
        ]);
        renderWithProviders(<Marketplace />);

        await waitForAuthenticatedGrid(calls);
        await userEvent.click(await screen.findByText("Apple Vision Pro"));

        expect(await screen.findByRole("button", { name: "Confirm Purchase" })).toBeDisabled();
    });

    it("lists and cancels the user's own offers in My Listings", async () => {
        const calls = mockFetch([
            { url: "/artifact-offers/cancel", method: "POST", body: {} },
            { url: "/artifact-listings/me", body: pageView([sampleListing]) },
            ...baseHandlers(true),
        ]);
        renderWithProviders(<Marketplace />);
        await screen.findByText("Apple Vision Pro");

        await userEvent.click(screen.getByRole("button", { name: "My Listings" }));
        await userEvent.click(await screen.findByText("Apple Vision Pro"));
        await userEvent.click(await screen.findByRole("button", { name: "Cancel Offer" }));

        expect(await screen.findByText("Listing canceled")).toBeInTheDocument();
        expect(calls.some((c) => c.url.includes("/artifact-offers/cancel"))).toBe(true);
    });

    it("opens the auth modal when anonymous users click My Listings", async () => {
        mockFetch(baseHandlers(false));
        renderWithProviders(<Marketplace />);
        await screen.findByText("Apple Vision Pro");

        await userEvent.click(screen.getByRole("button", { name: "My Listings" }));
        expect(await screen.findByText("Account required")).toBeInTheDocument();
    });

    it("shows an error message when listings fail to load", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            listingsHandler({ status: 500 }),
        ]);
        renderWithProviders(<Marketplace />);

        expect(await screen.findByText("Failed to load listings")).toBeInTheDocument();
    });
});
