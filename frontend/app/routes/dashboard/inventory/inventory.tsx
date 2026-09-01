import { useLoaderData, useRevalidator } from "react-router"
import { useState } from "react"
import { columns, type Item } from "./columns"
import { DataTable } from "../customers/data-table"
import { Button } from "~/components/ui/button"
import { RefreshCw } from "lucide-react"

export async function clientLoader(): Promise<Item[]> {
  const response = await fetch("/api/items", {
    credentials: "include",
  })

  if (!response.ok) {
    throw new Error("Failed to load items from backend")
  }

  return response.json()
}

export default function Inventory() {
  const items = useLoaderData() as Item[]
  const revalidator = useRevalidator()
  const [isSyncing, setIsSyncing] = useState(false)

  const handleSync = async () => {
    setIsSyncing(true)
    try {
      const res = await fetch("/api/items/sync", {
        method: "POST",
        credentials: "include",
      })

      if (!res.ok) throw new Error("Sync failed")

      revalidator.revalidate()
    } catch (error) {
      console.error("Failed to sync items:", error)
    } finally {
      setIsSyncing(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Inventory</h1>
          <p className="text-sm text-muted-foreground">
            Manage and view your inventory items.
          </p>
        </div>

        <Button onClick={handleSync} disabled={isSyncing} variant="outline" size="sm">
          <RefreshCw className={`mr-2 h-4 w-4 ${isSyncing ? "animate-spin" : ""}`} />
          {isSyncing ? "Syncing..." : "Sync from Clover"}
        </Button>
      </div>

      <DataTable columns={columns} data={items} />
    </div>
  )
}

