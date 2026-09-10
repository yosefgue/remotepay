import { type RouteConfig, route, index, layout } from "@react-router/dev/routes";

export default [
  index("routes/connect.tsx"),
  route("pay/:token", "routes/pay.tsx"),
  layout("components/ProtectedRoute.tsx", [
    route("sync", "routes/sync.tsx"),
    layout("routes/dashboard/dashboard.tsx", [
      route("dashboard", "routes/dashboard/home.tsx"),
      route("orders", "routes/dashboard/orders/orders.tsx"),
      route("orders/new", "routes/dashboard/orders/new-order.tsx"),
      route("customers", "routes/dashboard/customers/customers.tsx"),
      route("inventory", "routes/dashboard/inventory/inventory.tsx"),
    ]),
  ]),
] satisfies RouteConfig;