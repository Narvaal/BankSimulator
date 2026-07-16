import "@testing-library/jest-dom/vitest";
import { afterEach, vi } from "vitest";
import { cleanup } from "@testing-library/react";

// stubs de APIs de browser que o jsdom não implementa (Recharts/UI)
class ObserverStub {
    observe() {}
    unobserve() {}
    disconnect() {}
}
Object.assign(globalThis, {
    ResizeObserver: ObserverStub,
    IntersectionObserver: ObserverStub,
});
window.matchMedia ??= ((query: string) =>
    ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => {},
        removeListener: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
        dispatchEvent: () => false,
    }) as MediaQueryList) as typeof window.matchMedia;
window.scrollTo = (() => {}) as typeof window.scrollTo;

afterEach(() => {
    cleanup();
    sessionStorage.clear();
    localStorage.clear();
    vi.unstubAllGlobals();
});
