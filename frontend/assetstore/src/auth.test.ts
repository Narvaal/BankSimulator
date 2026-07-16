import { describe, it, expect } from "vitest";
import { getToken, setToken, clearToken, authHeader } from "./auth";

describe("auth token storage", () => {
    it("returns null when no token is stored", () => {
        expect(getToken()).toBeNull();
    });

    it("stores and retrieves the token via sessionStorage", () => {
        setToken("abc123");
        expect(getToken()).toBe("abc123");
        expect(sessionStorage.getItem("AUTH_TOKEN")).toBe("abc123");
    });

    it("clears the token", () => {
        setToken("abc123");
        clearToken();
        expect(getToken()).toBeNull();
    });
});

describe("authHeader", () => {
    it("returns empty headers when unauthenticated", () => {
        expect(authHeader()).toEqual({});
    });

    it("returns a Bearer header when a token exists", () => {
        setToken("jwt-token");
        expect(authHeader()).toEqual({ Authorization: "Bearer jwt-token" });
    });
});
