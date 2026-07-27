import { Outlet, Link, useNavigate } from "react-router";
import { useAuth } from "../../context/AuthContext";

export default function DashboardLayout() {
  const { merchantId, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <div className="flex h-screen bg-slate-100 font-sans text-slate-900">
      {/* 1. FIXED SIDEBAR */}
      <aside className="w-64 bg-slate-900 text-slate-100 flex flex-col justify-between p-4 shadow-lg">
        <div>
          {/* App Branding */}
          <div className="flex items-center gap-2 mb-8 px-2">
            <div className="h-8 w-8 rounded-lg bg-emerald-500 flex items-center justify-center font-bold text-slate-950">
              ☘️
            </div>
            <span className="font-semibold text-lg tracking-wide">Clover App</span>
          </div>

          {/* Navigation Links */}
          <nav className="space-y-1">
            <Link
              to="/dashboard"
              className="flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-slate-800 transition-colors text-sm font-medium"
            >
              📊 Home
            </Link>
            <Link
              to="/dashboard/orders"
              className="flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-slate-800 transition-colors text-sm font-medium"
            >
              📦 Orders
            </Link>
            <Link
              to="/dashboard/settings"
              className="flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-slate-800 transition-colors text-sm font-medium"
            >
              ⚙️ Settings
            </Link>
          </nav>
        </div>

        {/* User Info & Logout */}
        <div className="border-t border-slate-800 pt-4 px-2">
          <div className="text-xs text-slate-400 mb-2 truncate">
            Logged in as:
            <span className="block text-slate-200 font-mono text-sm mt-0.5 truncate">
              {merchantId || "Unknown"}
            </span>
          </div>
          <button
            onClick={handleLogout}
            className="w-full mt-2 bg-red-600/20 text-red-400 hover:bg-red-600/30 border border-red-500/30 py-2 px-3 rounded-md text-sm font-medium transition-colors"
          >
            Disconnect
          </button>
        </div>
      </aside>

      {/* 2. MAIN RIGHT CONTENT AREA */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* TOP NAVBAR */}
        <header className="h-16 bg-white border-b border-slate-200 px-6 flex items-center justify-between shadow-sm">
          <h1 className="text-lg font-semibold text-slate-800">Merchant Portal</h1>
          <div className="flex items-center gap-3">
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800 border border-emerald-200">
              ● Active Session
            </span>
          </div>
        </header>

        {/* DYNAMIC CHILD ROUTES RENDER HERE */}
        <main className="flex-1 overflow-y-auto p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}