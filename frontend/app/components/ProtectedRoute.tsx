// app/components/ProtectedRoute.tsx
import { redirect, Outlet, useOutletContext } from "react-router";

export type AuthData = {
  authenticated: boolean;
  merchantId: string;
};

export async function clientLoader() {
  try {
    const res = await fetch("/api/auth/me", { credentials: "include" });
    if (!res.ok) {
      throw redirect("/");
    }
    const data = (await res.json()) as AuthData;
    if (!data.authenticated) {
      throw redirect("/");
    }
    return data;
  } catch (err) {
    if (err instanceof Response) {
      throw err;
    }
    throw redirect("/");
  }
}

export default function ProtectedRoute() {
  return <Outlet />;
}

export function useAuth() {
  return useOutletContext<AuthData>();
}