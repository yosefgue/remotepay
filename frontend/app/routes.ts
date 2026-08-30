import { type RouteConfig, route, index, layout } from "@react-router/dev/routes";

export default [
  index("routes/Connect.tsx"),
  layout("components/ProtectedRoute.tsx", [
    route("sync", "routes/sync.tsx"),
    layout("routes/dashboard/dashboard.tsx", [
      route("dashboard", "routes/dashboard/home.tsx"),
      route("customers", "routes/dashboard/customers/customers.tsx"),
    ]),
  ]),
] satisfies RouteConfig;