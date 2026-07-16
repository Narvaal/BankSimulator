import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Pagination from "./Pagination";

describe("Pagination", () => {
    it("renders nothing with a single page", () => {
        const { container } = render(
            <Pagination page={0} totalPages={1} onPageChange={() => {}} />
        );
        expect(container).toBeEmptyDOMElement();
    });

    it("disables Previous on the first page and Next on the last", () => {
        const { rerender } = render(
            <Pagination page={0} totalPages={3} onPageChange={() => {}} />
        );
        expect(screen.getByRole("button", { name: "Previous" })).toBeDisabled();
        expect(screen.getByRole("button", { name: "Next" })).toBeEnabled();

        rerender(<Pagination page={2} totalPages={3} onPageChange={() => {}} />);
        expect(screen.getByRole("button", { name: "Previous" })).toBeEnabled();
        expect(screen.getByRole("button", { name: "Next" })).toBeDisabled();
    });

    it("calls onPageChange with the clicked page (0-based)", async () => {
        const onPageChange = vi.fn();
        render(<Pagination page={0} totalPages={5} onPageChange={onPageChange} />);

        await userEvent.click(screen.getByRole("button", { name: "3" }));
        expect(onPageChange).toHaveBeenCalledWith(2);

        await userEvent.click(screen.getByRole("button", { name: "Next" }));
        expect(onPageChange).toHaveBeenCalledWith(1);
    });

    it("shows an ellipsis when there are many pages", () => {
        render(<Pagination page={0} totalPages={10} onPageChange={() => {}} />);
        expect(screen.getByText("...")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "10" })).toBeInTheDocument();
    });
});
