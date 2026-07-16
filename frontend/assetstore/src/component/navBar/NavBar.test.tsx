import { describe, it, expect, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import userEvent from "@testing-library/user-event";
import NavBar from "./NavBar";
import UserMenu from "../usermenu/UserMenu";
import AuthRequiredModal from "../auth/AuthRequiredModal";
import Router from "../../Router";
import { renderWithProviders, mockFetch } from "../../test/helpers";
import { getToken, setToken } from "../../auth";

describe("NavBar", () => {
    it("renders navigation links and toggles collapse", async () => {
        mockFetch([]);
        const setCollapsed = vi.fn();
        renderWithProviders(<NavBar collapsed={false} setCollapsed={setCollapsed} />);

        for (const [text, href] of [
            ["Search", "/search"],
            ["Inventory", "/inventory"],
            ["Marketplace", "/market"],
            ["Rewards", "/reward"],
            ["Logs", "/logs"],
        ]) {
            expect(screen.getByText(text).closest("a")).toHaveAttribute("href", href);
        }

        await userEvent.click(screen.getByRole("button"));
        expect(setCollapsed).toHaveBeenCalled();
    });
});

describe("UserMenu", () => {
    const props = {
        balance: 150.5,
        nextFreeAssetAt: new Date(Date.now() + 60_000).toISOString(),
        name: "John Doe",
        imageUrl: "",
    };

    it("shows balance and signs out clearing the token", async () => {
        mockFetch([]);
        setToken("jwt");
        renderWithProviders(<UserMenu {...props} />);

        expect(screen.getByText("150.50")).toBeInTheDocument();

        await userEvent.click(screen.getByText("John Doe"));
        await userEvent.click(await screen.findByText("Sign out"));

        expect(getToken()).toBeNull();
    });

    it("requires accepting the disclaimer before opening Ko-fi", async () => {
        mockFetch([]);
        renderWithProviders(<UserMenu {...props} />);

        await userEvent.click(screen.getByText("150.50"));

        const continueButton = await screen.findByRole("button", {
            name: /continue to ko-fi/i,
        });
        expect(continueButton).toBeDisabled();

        await userEvent.click(screen.getByRole("checkbox"));
        expect(continueButton).toBeEnabled();
    });
});

describe("AuthRequiredModal", () => {
    it("closes on cancel and navigates to register", async () => {
        mockFetch([]);
        const onClose = vi.fn();
        renderWithProviders(<AuthRequiredModal onClose={onClose} />);

        expect(screen.getByText("Account required")).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: "Cancel" }));
        expect(onClose).toHaveBeenCalled();
    });
});

describe("Router", () => {
    it("migrates the session cookie token into sessionStorage on mount", async () => {
        mockFetch([{ url: "/auth/session", body: { token: "cookie-jwt" } }]);

        // Router traz o próprio BrowserRouter — renderiza só com o QueryClientProvider
        const queryClient = new QueryClient({
            defaultOptions: { queries: { retry: false, gcTime: 0 } },
        });
        render(
            <QueryClientProvider client={queryClient}>
                <Router />
            </QueryClientProvider>
        );

        await waitFor(() => expect(getToken()).toBe("cookie-jwt"));
    });
});
