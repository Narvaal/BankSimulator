import {useQuery} from "@tanstack/react-query";
import { API_URL } from "../../config";
import { authHeader } from "../../auth";

export interface Account {
    id: number;
    clientId: number;
    accountNumber: string;
    accountType: string;
    balance: number;
    accountStatus: string;
    publicKey: string;
    nextFreeAssetAt: string;

    name: string;
    picture: string,
    emailVerified: boolean,
    provider: string
}

export async function fetchAccount(): Promise<Account> {
    const res = await fetch(`${API_URL}/accounts/me`, {
        credentials: "include",
        headers: authHeader(),
    });

    if (!res.ok) throw new Error("Not authenticated");

    return res.json();
}

export function useAccount() {
    return useQuery<Account>({
        queryKey: ["account"],
        queryFn: fetchAccount,
        retry: false,
        staleTime: 0,
        refetchOnWindowFocus: true,
        refetchInterval: 30_000,
    });
}
