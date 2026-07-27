import { createContext, useContext, useEffect, useState, type ReactNode } from "react";

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  merchantId: string | null;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [merchantId, setMerchantId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetch("/api/auth/me", { credentials: "include" })
      .then((res) => {
        if (!res.ok) throw new Error("Unauthenticated");
        return res.json();
      })
      .then((data) => {
        setIsAuthenticated(true);
        setMerchantId(data.merchantId);
      })
      .catch(() => {
        setIsAuthenticated(false);
        setMerchantId(null);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

  const logout = () => {
    fetch("/api/auth/logout", { method: "POST", credentials: "include" })
      .finally(() => {
        setIsAuthenticated(false);
        setMerchantId(null);
        window.location.href = "/";
      });
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, isLoading, merchantId, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}