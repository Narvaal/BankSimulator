import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import {
    RarityBadge,
    ArtifactCardThumb,
    ArtifactCardDetail,
    type CardMetadata,
} from "./ArtifactCard";

const metadata: CardMetadata = {
    name: "Apple Vision Pro",
    subtitle: "Apple enters the spatial computing era",
    category: "Technology",
    rarity: "Legendary",
    illustration: "https://cdn.example.com/cards/apple-vision-pro.png",
    attributes: { influence: 91, innovation: 95, controversy: 48 },
    abilities: [
        { name: "Closed Ecosystem", description: "Commands loyalty through exclusivity." },
    ],
    passive: { name: "Silicon Monopoly", description: "Arguments backed by revenue gain weight." },
    weakness: "Premium pricing limits global adoption",
    flavorText: "The future arrived, priced accordingly.",
    traits: [{ name: "Era", value: "AI Age" }],
    references: ["https://www.apple.com/apple-vision-pro/"],
    cardNumber: "042",
};

describe("RarityBadge", () => {
    it("renders the rarity name", () => {
        render(<RarityBadge rarity="Mythic" />);
        expect(screen.getByText("Mythic")).toBeInTheDocument();
    });

    it("falls back to Common when rarity is missing", () => {
        render(<RarityBadge />);
        expect(screen.getByText("Common")).toBeInTheDocument();
    });

    it("applies the rarity-specific badge style", () => {
        render(<RarityBadge rarity="Legendary" />);
        expect(screen.getByText("Legendary").className).toContain("text-yellow-700");
    });
});

describe("ArtifactCardThumb", () => {
    it("renders name, subtitle, card number and rarity badge", () => {
        render(<ArtifactCardThumb metadata={metadata} />);
        expect(screen.getByText("Apple Vision Pro")).toBeInTheDocument();
        expect(screen.getByText("Apple enters the spatial computing era")).toBeInTheDocument();
        expect(screen.getByText("#042")).toBeInTheDocument();
        expect(screen.getByText("Legendary")).toBeInTheDocument();
    });

    it("renders the illustration when present", () => {
        render(<ArtifactCardThumb metadata={metadata} />);
        expect(screen.getByRole("img", { name: "Apple Vision Pro" }))
            .toHaveAttribute("src", metadata.illustration);
    });
});

describe("ArtifactCardDetail", () => {
    it("renders abilities, passive and weakness", () => {
        render(<ArtifactCardDetail metadata={metadata} />);
        expect(screen.getByText("Closed Ecosystem")).toBeInTheDocument();
        expect(screen.getByText("Silicon Monopoly")).toBeInTheDocument();
        expect(screen.getByText("Premium pricing limits global adoption")).toBeInTheDocument();
    });

    it("renders attribute values", () => {
        render(<ArtifactCardDetail metadata={metadata} />);
        expect(screen.getByText("influence")).toBeInTheDocument();
        expect(screen.getByText("91")).toBeInTheDocument();
    });

    it("renders the flavor text", () => {
        render(<ArtifactCardDetail metadata={metadata} />);
        expect(screen.getByText(/The future arrived, priced accordingly\./)).toBeInTheDocument();
    });
});
