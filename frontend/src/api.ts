const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export type User = { id: string; email: string; name: string; emailVerified: boolean; role: string; createdAt: string };
export type AuthResponse = { accessToken: string; tokenType: string; expiresInSeconds: number; user: User };
export type Wallet = { id: string; userId: string; name: string; createdAt: string };
export type Asset = { id: string; ticker: string; type: string; name: string };
export type Transaction = {
  id: string; walletId: string; walletName: string; assetId: string; assetTicker: string;
  operationType: string; quantity: number; unitPrice: number; fees: number | null;
};
export type DailyQuote = { id: string; assetId: string; assetTicker: string; quoteDate: string; closePrice: number };

let token: string | null = localStorage.getItem("token");

export function setToken(t: string | null) {
  token = t;
  if (t) localStorage.setItem("token", t);
  else localStorage.removeItem("token");
}

export const hasToken = () => token !== null;

export async function api<T>(path: string, method = "GET", body?: unknown): Promise<T> {
  const res = await fetch(BASE_URL + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (res.status === 401) setToken(null);
  if (!res.ok) {
    const err = await res.json().catch(() => null);
    const fields = err?.fieldErrors?.map((f: { field: string; message: string }) => `${f.field}: ${f.message}`).join("; ");
    throw new Error(fields || err?.message || `Erro ${res.status}`);
  }
  return res.status === 204 ? (undefined as T) : res.json();
}
