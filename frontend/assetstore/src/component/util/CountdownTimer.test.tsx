import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, act } from "@testing-library/react";
import CountdownTimer from "./CountdownTimer";

describe("CountdownTimer", () => {
    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date("2026-01-01T12:00:00Z"));
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it("shows the remaining time as MM:SS", () => {
        render(<CountdownTimer targetDate="2026-01-01T12:01:30Z" />);
        expect(screen.getByText("01:30")).toBeInTheDocument();
    });

    it("counts down as time passes", () => {
        render(<CountdownTimer targetDate="2026-01-01T12:01:30Z" />);
        act(() => {
            vi.advanceTimersByTime(30_000);
        });
        expect(screen.getByText("01:00")).toBeInTheDocument();
    });

    it("shows 'Available Now!' once the target date is reached", () => {
        render(<CountdownTimer targetDate="2026-01-01T12:00:05Z" />);
        act(() => {
            vi.advanceTimersByTime(6_000);
        });
        expect(screen.getByText("Available Now!")).toBeInTheDocument();
    });

    it("is already available for a past target date", () => {
        render(<CountdownTimer targetDate="2026-01-01T11:00:00Z" />);
        expect(screen.getByText("Available Now!")).toBeInTheDocument();
    });
});
