import { createContext, useContext, useEffect, useState, type ReactNode } from "react";

// Define the shape of data available to components
interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  merchantId: string | null;
  logout: () => void;
}

// 1. Create the Context (defaulting to null)
const AuthContext = createContext<AuthContextType | null>(null);

// 2. The Provider Component that wraps your app
export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [merchantId, setMerchantId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Check session validity with Spring Boot when the app first loads or refreshes
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

  // Logout handler to trigger Spring Boot cookie cleanup
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

// 3. Custom Hook to easily consume AuthContext in any component
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}