import { createColumnHelper } from "@tanstack/react-table"
import { type DataTableFeatures } from "~/lib/data-table-features"

export type Order = {
  id: number
  customerName: string | null
  status: string
  totalAmount: number | null
  linkToken: string | null
  createdAt: string
}

const columnHelper = createColumnHelper<DataTableFeatures, Order>()

export const columns = columnHelper.columns([
  columnHelper.accessor("id", {
    header: "Order ID",
    cell: (info) => (
      <span className="font-mono font-medium text-foreground">
        CL-000{info.getValue()}
      </span>
    ),
  }),
  columnHelper.accessor("customerName", {
    header: "Customer",
    filterFn: "includesString",
    cell: (info) => (
      <span className="font-medium text-foreground">
        {info.getValue() || "—"}
      </span>
    ),
  }),
  columnHelper.accessor("status", {
    header: "Status",
    cell: (info) => {
      const status = info.getValue()
      const styles: Record<string, string> = {
        DRAFT: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300",
        PENDING: "bg-yellow-100 text-yellow-700 dark:bg-yellow-900 dark:text-yellow-300",
        PAID: "bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300",
        CANCELLED: "bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-300",
      }
      return (
        <span
          className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${styles[status] || "bg-gray-100 text-gray-700"}`}
        >
          {status}
        </span>
      )
    },
  }),
  columnHelper.accessor("totalAmount", {
    header: "Total",
    cell: (info) => {
      const v = info.getValue()
      return v != null
        ? `$${(v / 100).toFixed(2)}`
        : <span className="text-muted-foreground">—</span>
    },
  }),
  columnHelper.accessor("linkToken", {
    header: "",
    cell: (info) => {
      const token = info.getValue()
      if (!token) return null

      const handleCopy = async () => {
        const link = `${window.location.origin}/pay/${token}`
        await navigator.clipboard.writeText(link)
        const el = info.cell.getContext().cell.id
        const btn = document.querySelector(`[data-cell="${el}"] button`) as HTMLButtonElement | null
        if (btn) {
          btn.textContent = "Copied!"
          setTimeout(() => { btn.textContent = "Copy Link" }, 1500)
        }
      }

      return (
        <button
          onClick={handleCopy}
          className="inline-flex items-center rounded-md px-2 py-1 text-xs font-medium text-primary hover:bg-primary/10 transition-colors cursor-pointer"
        >
          Copy Link
        </button>
      )
    },
  }),
  columnHelper.accessor("createdAt", {
    header: "Created",
    cell: (info) =>
      info.getValue()
        ? new Date(info.getValue()!).toLocaleDateString("en-US", {
            month: "short",
            day: "numeric",
            year: "numeric",
          })
        : <span className="text-muted-foreground">—</span>,
  }),
])
