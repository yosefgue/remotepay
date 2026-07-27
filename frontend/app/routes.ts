import { type RouteConfig, route, index, layout } from "@react-router/dev/routes";

export default [
  index("routes/Connect.tsx"),
  route("auth-success", "components/AuthSuccess.tsx"),
  layout("components/ProtectedRoute.tsx", [
    route("dashboard", "routes/dashboard/Dashboard.tsx", [
      index("routes/dashboard/Home.tsx"),
    ]),
  ]),
] satisfies RouteConfig;