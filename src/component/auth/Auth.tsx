import {useQuery} from "@tanstack/react-query";

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
    const res = await fetch("https://bankapi.alessandro-bezerra.me/accounts/me", {
        credentials: "include",
    });

    if (!res.ok) throw new Error("Not authenticated");

    return res.json();
}

export function useAccount() {
    return useQuery<Account>({
        queryKey: ["account"],
        queryFn: fetchAccount,
        retry: false,
        staleTime: 1000 * 60 * 5,
    });
}
