import { describe, it, expect } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import Logs from "./Logs";
import ArtifactDetail from "../artifact/ArtifactDetail";
import {
    renderWithProviders,
    mockFetch,
    pageView,
    sampleMetadata,
} from "../../test/helpers";

const transfer = {
    id: 1,
    artifactName: "Apple Vision Pro",
    artifactUnitId: 20,
    salePrice: 42.5,
    fromAccountId: 3,
    toAccountId: 4,
    createdAt: "2026-07-10T10:00:00Z",
};

describe("Logs", () => {
    it("renders the public transfer feed", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/artifact-transfers", body: pageView([transfer]) },
        ]);
        renderWithProviders(<Logs />);

        expect(await screen.findByText("Transfer Log")).toBeInTheDocument();
        expect(await screen.findByText("Apple Vision Pro")).toBeInTheDocument();
        expect(screen.getByText("$42.50")).toBeInTheDocument();
    });

    it("filters by artifact from URL params and clears the filter", async () => {
        const calls = mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/artifact-transfers", body: pageView([transfer]) },
        ]);
        renderWithProviders(<Logs />, {
            route: "/logs?artifactId=30&artifactName=Apple%20Vision%20Pro",
            path: "/logs",
        });

        expect(await screen.findByText(/transfers of "apple vision pro"/i)).toBeInTheDocument();
        await waitFor(() =>
            expect(calls.some((c) => c.url.includes("artifactId=30"))).toBe(true)
        );
    });

    it("shows empty and error states", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/artifact-transfers", body: pageView([]) },
        ]);
        renderWithProviders(<Logs />);
        expect(await screen.findByText("No transfers yet.")).toBeInTheDocument();
    });

    it("surfaces load failures", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/artifact-transfers", status: 500, body: {} },
        ]);
        renderWithProviders(<Logs />);
        expect(await screen.findByText("Failed to load transfer log")).toBeInTheDocument();
    });
});

describe("ArtifactDetail", () => {
    const unitDetail = {
        unitId: 20,
        artifactId: 30,
        artifactName: "Apple Vision Pro",
        metadata: sampleMetadata,
        ownerAccountId: 5,
        status: "IN_MARKET",
        createdAt: "2026-07-01T12:00:00Z",
        priceHistory: [
            { artifactId: 30, artifactUnitId: 20, oldPrice: 0, newPrice: 80, createdAt: "2026-07-02T12:00:00Z" },
        ],
        transfers: [
            { id: 1, fromAccountId: 3, toAccountId: 5, salePrice: 80, createdAt: "2026-07-03T12:00:00Z" },
        ],
    };

    it("renders the unit with status, chain and cross-route links", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/artifact-units/20", body: unitDetail },
        ]);
        renderWithProviders(<ArtifactDetail />, { route: "/artifact/20", path: "/artifact/:id" });

        expect(await screen.findByText("Listed")).toBeInTheDocument();
        expect(screen.getByText("Ownership Chain")).toBeInTheDocument();

        expect(screen.getByText("View in Market").closest("a")).toHaveAttribute(
            "href",
            "/market?artifactId=30&artifactName=Apple%20Vision%20Pro"
        );
        expect(screen.getByText("Transfer Log").closest("a")).toHaveAttribute(
            "href",
            "/logs?artifactId=30&artifactName=Apple%20Vision%20Pro"
        );
    });

    it("shows the not-found state", async () => {
        mockFetch([
            { url: "/accounts/me", status: 401, body: {} },
            { url: "/artifact-units/99", status: 404, body: {} },
        ]);
        renderWithProviders(<ArtifactDetail />, { route: "/artifact/99", path: "/artifact/:id" });

        expect(await screen.findByText("Artifact not found")).toBeInTheDocument();
    });
});
