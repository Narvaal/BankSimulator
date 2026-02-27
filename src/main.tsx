import React from "react";
import ReactDOM from "react-dom/client";
import Router from "./Router.tsx"
import "./index.css"
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
            retry: false,
        },
    },
});

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <QueryClientProvider client={queryClient}>
        <Router/>
        </QueryClientProvider>
    </React.StrictMode>
);
