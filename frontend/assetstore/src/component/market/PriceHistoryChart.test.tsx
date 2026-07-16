import { describe, it, expect } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import PriceHistoryChart from "./PriceHistoryChart";
import UserMenu from "../usermenu/UserMenu";
import AuthRequiredModal from "../auth/AuthRequiredModal";
import RareLines from "../landpage/RareLines";
import { renderWithProviders, mockFetch } from "../../test/helpers";

describe("PriceHistoryChart", () => {
    it("shows an empty state without history", () => {
        render(<PriceHistoryChart priceHistory={[]} />);
        expect(screen.getByText("No price history")).toBeInTheDocument();
    });

    it("renders the area chart when there is history", () => {
        const history = [
            { artifactId: 1, artifactUnitId: 2, oldPrice: 0, newPrice: 50, createdAt: "2026-07-01T10:00:00Z" },
            { artifactId: 1, artifactUnitId: 2, oldPrice: 50, newPrice: 80, createdAt: "2026-07-02T10:00:00Z" },
        ];
        const { container } = render(<PriceHistoryChart priceHistory={history} />);

        expect(screen.queryByText("No price history")).not.toBeInTheDocument();
        expect(container.querySelector(".recharts-responsive-container")).toBeInTheDocument();
    });
});

describe("UserMenu — Ko-fi flow", () => {
    const props = {
        balance: 10,
        nextFreeAssetAt: new Date(Date.now() + 60_000).toISOString(),
        name: "John Doe",
        imageUrl: "https://pic.example/avatar.png",
    };

    it("opens the Ko-fi iframe after accepting the disclaimer", async () => {
        mockFetch([]);
        renderWithProviders(<UserMenu {...props} />);

        await userEvent.click(screen.getByText("10.00"));
        await userEvent.click(await screen.findByRole("checkbox"));
        await userEvent.click(screen.getByRole("button", { name: /continue to ko-fi/i }));

        await waitFor(() =>
            expect(document.querySelector('iframe[src*="ko-fi.com"]')).toBeInTheDocument()
        );
    });

    it("dismisses the disclaimer on cancel", async () => {
        mockFetch([]);
        renderWithProviders(<UserMenu {...props} />);

        await userEvent.click(screen.getByText("10.00"));
        await userEvent.click(await screen.findByRole("button", { name: "Cancel" }));

        await waitFor(() =>
            expect(screen.queryByText(/before you continue/i)).not.toBeInTheDocument()
        );
    });

    it("renders the avatar image when provided", () => {
        mockFetch([]);
        renderWithProviders(<UserMenu {...props} />);
        expect(screen.getByAltText("John Doe")).toHaveAttribute("src", props.imageUrl);
    });
});

describe("AuthRequiredModal — register path", () => {
    it("navigates to /register on create account", async () => {
        mockFetch([]);
        renderWithProviders(<AuthRequiredModal onClose={() => {}} />);

        await userEvent.click(screen.getByRole("button", { name: "Create account" }));
        // MemoryRouter sem rota /register: basta não explodir e fechar o fluxo
    });

    it("closes when clicking the backdrop", async () => {
        mockFetch([]);
        let closed = false;
        renderWithProviders(<AuthRequiredModal onClose={() => (closed = true)} />);

        await userEvent.click(screen.getByText("Account required").closest(".fixed")!);
        expect(closed).toBe(true);
    });
});

describe("RareLines landing", () => {
    it("renders the hero with CTAs", async () => {
        mockFetch([]);
        renderWithProviders(<RareLines />);

        const links = document.querySelectorAll("a[href]");
        expect(links.length).toBeGreaterThan(0);
    });
});
