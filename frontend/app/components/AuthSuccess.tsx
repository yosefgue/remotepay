import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router";

export default function AuthSuccess() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  
  // Guard against React StrictMode double-execution in dev
  const isExecuting = useRef(false);

  useEffect(() => {
    // Prevent duplicate execution during React Strict Mode remount
    if (isExecuting.current) return;
    isExecuting.current = true;

    const merchantId = searchParams.get("merchantId");
    const returnedState = searchParams.get("state");
    const savedState = sessionStorage.getItem("clover_csrf_state");

    // Clear saved token after reading it
    if (savedState) {
      sessionStorage.removeItem("clover_csrf_state");
    }

    // 1. VERIFY STATE (CSRF Protection)
    if (!savedState || savedState !== returnedState) {
      console.error("CSRF State mismatch!");
      setError("Security verification failed. Request may be forged.");
      return;
    }

    if (!merchantId) {
      setError("Missing merchant information.");
      return;
    }

    // 2. ESTABLISH SESSION COOKIE
    fetch("/api/auth/session", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ merchantId }),
      credentials: "include", // Saves HttpOnly cookie on localhost
    })
      .then((res) => {
        if (res.ok) {
          // Full page redirect reloads AuthContext state cleanly
          window.location.href = "/dashboard";
        } else {
          setError("Failed to establish local user session.");
        }
      })
      .catch(() => setError("Network error during session setup."));
  }, [searchParams]);

  if (error) {
    return (
      <div className="flex h-screen flex-col items-center justify-center text-red-500">
        <p className="font-semibold">{error}</p>
        <button 
          onClick={() => navigate("/")} 
          className="mt-4 text-sm text-blue-500 underline"
        >
          Return to Home
        </button>
      </div>
    );
  }

  return (
    <div className="flex h-screen items-center justify-center">
      <p>Logging you in...</p>
    </div>
  );
}