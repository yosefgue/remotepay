// app/components/ProtectedRoute.tsx
import { Navigate, Outlet } from "react-router";
import { useAuth, AuthProvider } from "../context/AuthContext";
import { Spinner } from "~/components/ui/spinner"

export default function ProtectedRoute() {
  return (
    <AuthProvider>
      <ProtectedContent />
    </AuthProvider>
  );
}

function ProtectedContent() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}