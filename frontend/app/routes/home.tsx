import { Button } from "~/components/ui/button"

import { type RouteConfig, index, route, layout } from "@react-router/dev/routes";

export default [
  index("routes/connect.tsx"),
  
  layout("components/ProtectedRoute.tsx", [
    route("dashboard", "routes/dashboard.tsx"),
    route("orders", "routes/orders.tsx"),
    route("settings", "routes/settings.tsx"),
  ]),
] satisfies RouteConfig;
