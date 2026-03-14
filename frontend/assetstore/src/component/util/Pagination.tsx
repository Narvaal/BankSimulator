interface PaginationProps {
    page: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

function Pagination({ page, totalPages, onPageChange }: PaginationProps) {

    function getPages() {

        const pages: (number | string)[] = [];

        const start = Math.max(0, page - 2);
        const end = Math.min(totalPages - 1, page + 2);

        if (start > 0) {
            pages.push(0);
            if (start > 1) pages.push("ellipsis-start");
        }

        for (let i = start; i <= end; i++) {
            pages.push(i);
        }

        if (end < totalPages - 1) {
            if (end < totalPages - 2) pages.push("ellipsis-end");
            pages.push(totalPages - 1);
        }

        return pages;
    }

    if (totalPages <= 1) return null;

    const pages = getPages();

    return (

        <div className="flex justify-center">

            <div className="flex items-center border border-slate-300 rounded-md overflow-hidden bg-white shadow-sm">

                {/* Previous */}

                <button
                    disabled={page === 0}
                    onClick={() => onPageChange(page - 1)}
                    className="px-3 py-2 text-sm border-r border-slate-200 hover:bg-slate-100 disabled:opacity-40"
                >
                    Previous
                </button>

                {pages.map((p, i) => {

                    if (typeof p !== "number") {
                        return (
                            <span key={p + i} className="px-3 py-2 text-sm text-slate-400">
                                ...
                            </span>
                        );
                    }

                    return (
                        <button
                            key={`page-${p}-${i}`}
                            onClick={() => onPageChange(p)}
                            className={`
                                px-3 py-2 text-sm border-r border-slate-200
                                hover:bg-slate-100
                                ${p === page
                                ? "bg-slate-900 text-white"
                                : "text-slate-700"}
                            `}
                        >
                            {p + 1}
                        </button>
                    );
                })}

                {/* Next */}

                <button
                    disabled={page === totalPages - 1}
                    onClick={() => onPageChange(page + 1)}
                    className="px-3 py-2 text-sm hover:bg-slate-100 disabled:opacity-40"
                >
                    Next
                </button>

            </div>

        </div>

    );
}

export default Pagination;