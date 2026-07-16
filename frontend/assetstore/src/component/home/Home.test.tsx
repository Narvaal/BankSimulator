import { describe, it, expect } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Home from "./Home";
import {
    renderWithProviders,
    mockFetch,
    pageView,
    sampleAccount,
    sampleUnit,
    type FetchHandler,
} from "../../test/helpers";

const baseHandlers: FetchHandler[] = [
    { url: "/accounts/me", body: sampleAccount },
    { url: /\/artifact-units\?ownerId=/, body: pageView([sampleUnit]) },
    { url: "/price-history", body: [] },
];

async function openSellModal() {
    await userEvent.click(await screen.findByText("Apple Vision Pro"));
    return screen.findByPlaceholderText("Price");
}

describe("Home (inventory)", () => {
    it("shows the auth modal when the session is invalid", async () => {
        mockFetch([{ url: "/accounts/me", status: 401, body: {} }]);
        renderWithProviders(<Home />);

        expect(await screen.findByText("Account required")).toBeInTheDocument();
    });

    it("renders the owned units grid", async () => {
        const calls = mockFetch(baseHandlers);
        renderWithProviders(<Home />);

        expect(await screen.findByText("Apple Vision Pro")).toBeInTheDocument();
        expect(calls.some((c) => c.url.includes("ownerId=1"))).toBe(true);
    });

    it("rejects invalid sale prices", async () => {
        mockFetch(baseHandlers);
        renderWithProviders(<Home />);
        const priceInput = await openSellModal();

        await userEvent.click(screen.getByRole("button", { name: "List for sale" }));
        expect(await screen.findByText("Invalid price")).toBeInTheDocument();

        await userEvent.type(priceInput, "10.999");
        await userEvent.click(screen.getByRole("button", { name: "List for sale" }));
        expect(
            await screen.findByText("Price cannot have more than 2 decimal places")
        ).toBeInTheDocument();
    });

    it("lists a unit for sale and removes it from the grid", async () => {
        const calls = mockFetch([
            { url: "/artifact-offers", method: "POST", body: {} },
            ...baseHandlers,
        ]);
        renderWithProviders(<Home />);
        const priceInput = await openSellModal();

        await userEvent.type(priceInput, "49.90");
        await userEvent.click(screen.getByRole("button", { name: "List for sale" }));

        expect(await screen.findByText("Artifact listed successfully")).toBeInTheDocument();
        expect(
            calls.find((c) => c.url.includes("/artifact-offers") && c.method === "POST")!.body
        ).toEqual({ artifactUnitId: 20, price: 49.9 });

        await waitFor(() =>
            expect(screen.queryByText("Apple Vision Pro")).not.toBeInTheDocument()
        );
    });

    it("surfaces server errors when listing fails", async () => {
        mockFetch([
            { url: "/artifact-offers", method: "POST", status: 400, body: {} },
            ...baseHandlers,
        ]);
        renderWithProviders(<Home />);
        const priceInput = await openSellModal();

        await userEvent.type(priceInput, "49.90");
        await userEvent.click(screen.getByRole("button", { name: "List for sale" }));

        expect(await screen.findByText("Artifact offer not valid")).toBeInTheDocument();
    });
});
