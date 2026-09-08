import { useLoaderData } from "react-router"
import { Link } from "react-router"
import { columns, type Order } from "./columns"
import { DataTable } from "../customers/data-table"
import { Button } from "~/components/ui/button"
import { Plus, ReceiptText } from "lucide-react"

export async function clientLoader(): Promise<Order[]> {
  const response = await fetch("/api/orders", {
    credentials: "include",
  })

  if (!response.ok) {
    throw new Error("Failed to load orders")
  }

  return response.json()
}

export default function Orders() {
  const orders = useLoaderData() as Order[]

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Orders</h1>
          <p className="text-sm text-muted-foreground">
            Manage and view your draft orders.
          </p>
        </div>

        <Link to="/orders/new">
          <Button>
            <Plus className="h-4 w-4" />
            New Order
          </Button>
        </Link>
      </div>

      {orders.length > 0 ? (
        <DataTable columns={columns} data={orders} searchColumn="customerName" searchPlaceholder="Search by customer..." />
      ) : (
        <div className="flex flex-col items-center justify-center rounded-md border border-dashed bg-card py-16 shadow-xs">
          <ReceiptText className="size-10 text-muted-foreground/40" />
          <h3 className="mt-4 text-lg font-semibold">No orders yet</h3>
          <p className="mt-1 text-sm text-muted-foreground">
            Create your first draft order to get started.
          </p>
        </div>
      )}
    </div>
  )
}
