// app/routes/dashboard/customers.tsx
import { useLoaderData, useRevalidator } from "react-router"
import { useState } from "react"
import { columns, type Customer } from "./columns"
import { DataTable } from "./data-table"
import { Button } from "~/components/ui/button"
import { RefreshCw } from "lucide-react"

export async function clientLoader(): Promise<Customer[]> {
  const response = await fetch("/api/customers", {
    credentials: "include",
  })

  if (!response.ok) {
    throw new Error("Failed to load customers from backend")
  }

  return response.json()
}

export default function Customers() {
  const customers = useLoaderData() as Customer[]
  const revalidator = useRevalidator()
  const [isSyncing, setIsSyncing] = useState(false)

  const handleSync = async () => {
    setIsSyncing(true)
    try {
      const res = await fetch("/api/customers/sync", {
        method: "POST",
        credentials: "include",
      })

      if (!res.ok) throw new Error("Sync failed")

      revalidator.revalidate()
    } catch (error) {
      console.error("Failed to sync customers:", error)
    } finally {
      setIsSyncing(false)
    }
  }

  return (
    <div className="container mx-auto py-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Customers</h1>
          <p className="text-sm text-muted-foreground">
            Manage and view customer profiles.
          </p>
        </div>

        <Button onClick={handleSync} disabled={isSyncing} variant="outline" size="sm">
          <RefreshCw className={`mr-2 h-4 w-4 ${isSyncing ? "animate-spin" : ""}`} />
          {isSyncing ? "Syncing..." : "Sync from Clover"}
        </Button>
      </div>

      <DataTable columns={columns} data={customers} />
    </div>
  )
}