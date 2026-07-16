import { render } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { vi } from "vitest";
import type { ReactElement } from "react";
import type { CardMetadata } from "../component/artifact/ArtifactCard";

/* ===================== RENDER ===================== */

export function renderWithProviders(
    ui: ReactElement,
    { route = "/", path }: { route?: string; path?: string } = {}
) {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: { retry: false, gcTime: 0, refetchOnWindowFocus: false, refetchInterval: false },
        },
    });

    return render(
        <QueryClientProvider client={queryClient}>
            <MemoryRouter initialEntries={[route]}>
                {path ? (
                    <Routes>
                        <Route path={path} element={ui} />
                    </Routes>
                ) : (
                    ui
                )}
            </MemoryRouter>
        </QueryClientProvider>
    );
}

/* ===================== FETCH MOCK ===================== */

export interface FetchHandler {
    /** substring ou regex casada contra a URL */
    url: string | RegExp;
    method?: string;
    status?: number;
    body?: unknown;
}

export interface RecordedCall {
    url: string;
    method: string;
    body: unknown;
}

/**
 * Stub global de fetch roteado por URL/method.
 * Handlers são testados em ordem; o primeiro que casar responde.
 * Requisições sem handler retornam 404 com body vazio.
 */
export function mockFetch(handlers: FetchHandler[]) {
    const calls: RecordedCall[] = [];

    vi.stubGlobal(
        "fetch",
        vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
            const url = String(input);
            const method = (init?.method ?? "GET").toUpperCase();
            let parsedBody: unknown = null;
            if (typeof init?.body === "string") {
                try {
                    parsedBody = JSON.parse(init.body);
                } catch {
                    parsedBody = init.body;
                }
            }
            calls.push({ url, method, body: parsedBody });

            const handler = handlers.find(
                (h) =>
                    (typeof h.url === "string" ? url.includes(h.url) : h.url.test(url)) &&
                    (h.method ?? "GET").toUpperCase() === method
            );

            const status = handler?.status ?? (handler ? 200 : 404);
            const body = handler?.body ?? {};

            return new Response(JSON.stringify(body), {
                status,
                headers: { "Content-Type": "application/json" },
            });
        })
    );

    return calls;
}

/* ===================== FIXTURES ===================== */

export const sampleMetadata: CardMetadata = {
    name: "Apple Vision Pro",
    subtitle: "Apple enters the spatial computing era",
    category: "Technology",
    rarity: "Legendary",
    illustration: "https://cdn.example.com/cards/apple-vision-pro.png",
    attributes: { influence: 91, innovation: 95, controversy: 48, longevity: 72, reach: 88 },
    abilities: [
        { name: "Closed Ecosystem", description: "Commands loyalty through exclusivity." },
        { name: "Silicon Design", description: "Custom chips nobody else can buy." },
    ],
    passive: { name: "Silicon Monopoly", description: "Arguments backed by revenue gain weight." },
    weakness: "Premium pricing limits global adoption",
    flavorText: "The future arrived, priced accordingly.",
    lore: "When Apple unveiled its headset in 2024, it marked a new computing paradigm.",
    traits: [
        { name: "Era", value: "AI Age" },
        { name: "Origin", value: "United States" },
    ],
    timeline: [
        { date: "2023-06-05", event: "Announced at WWDC" },
        { date: "2024-02-02", event: "Released in the United States" },
    ],
    references: ["https://www.apple.com/apple-vision-pro/"],
    collection: "Tech Giants 2024",
    cardNumber: "042",
    releaseDate: "2024-06-03",
    artist: "RareLines AI",
    model: "stability-ai-sd3-ultra",
    chosenStyle: "soviet propaganda poster",
    prompt: "Futuristic spatial computing headset",
    seed: "4829301",
};

export const sampleAccount = {
    id: 1,
    clientId: 2,
    accountNumber: "111-222-333",
    accountType: "DEFAULT",
    balance: 150.0,
    accountStatus: "ACTIVE",
    publicKey: "pk",
    nextFreeAssetAt: new Date(Date.now() + 60_000).toISOString(),
    name: "John Doe",
    picture: null,
    emailVerified: true,
    provider: "LOCAL",
};

export function pageView<T>(items: T[], page = 0, pageSize = 12) {
    return {
        items,
        page,
        pageSize,
        totalPages: items.length > 0 ? 1 : 0,
        totalItems: items.length,
    };
}

export const sampleListing = {
    id: 10,
    artifactUnitId: 20,
    artifactId: 30,
    artifactName: "Apple Vision Pro",
    metadata: sampleMetadata,
    price: 99.5,
    sellerAccountId: 5,
    sellerName: "Seller",
    status: "ACTIVE",
    createdAt: "2026-07-01T12:00:00Z",
    updatedAt: "2026-07-01T12:00:00Z",
};

export const sampleUnit = {
    artifactUnitId: 20,
    artifactId: 30,
    artifactName: "Apple Vision Pro",
    metadata: sampleMetadata,
    status: "AVAILABLE",
    createdAt: "2026-07-01T12:00:00Z",
};
