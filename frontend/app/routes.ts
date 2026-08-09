import { type RouteConfig, route, index, layout } from "@react-router/dev/routes";

export default [
  index("routes/Connect.tsx"),
  route("auth-success", "components/AuthSuccess.tsx"),
  layout("components/ProtectedRoute.tsx", [
    route("sync", "routes/sync.tsx"),
    route("dashboard", "routes/dashboard/dashboard.tsx", [
      index("routes/dashboard/home.tsx"),
    ]),
  ]),
] satisfies RouteConfig;