import { API_URL } from "./config";

const KEY = "AUTH_TOKEN";

export const getToken = (): string | null => sessionStorage.getItem(KEY);
export const setToken = (token: string): void => sessionStorage.setItem(KEY, token);
export const clearToken = (): void => sessionStorage.removeItem(KEY);

export function authHeader(): HeadersInit {
    const token = getToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function initSession(): Promise<void> {
    if (getToken()) return;
    try {
        const res = await fetch(`${API_URL}/auth/session`, { credentials: "include" });
        if (res.ok) {
            const data = await res.json();
            if (data.token) setToken(data.token);
        }
    } catch {
        // usuário não autenticado — nenhuma ação necessária
    }
}
