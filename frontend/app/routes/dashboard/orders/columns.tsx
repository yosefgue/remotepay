import { useState } from "react"
import { useRevalidator } from "react-router"
import { createColumnHelper } from "@tanstack/react-table"
import { type DataTableFeatures } from "~/lib/data-table-features"
import { Tooltip, TooltipTrigger, TooltipContent } from "~/components/ui/tooltip"
import { Copy, Check, Pencil, Trash2, Loader2 } from "lucide-react"

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
        OPEN: "bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300",
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
  columnHelper.display({
    id: "actions",
    cell: ({ row }) => <OrderRowActions order={row.original} />,
  }),
])

function OrderRowActions({ order }: { order: Order }) {
  const revalidator = useRevalidator()
  const [copied, setCopied] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  const handleCopy = async () => {
    if (!order.linkToken) return
    const link = `${window.location.origin}/pay/${order.linkToken}`
    await navigator.clipboard.writeText(link)
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
  }

  const handleDelete = async () => {
    if (isDeleting) return
    setIsDeleting(true)
    try {
      const res = await fetch(`/api/orders/${order.id}`, {
        method: "DELETE",
        credentials: "include",
      })
      if (res.ok) {
        revalidator.revalidate()
      }
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <div className="flex items-center justify-end gap-1" onClick={(e) => e.stopPropagation()}>
      {/* Copy */}
      <Tooltip>
        <TooltipTrigger
          type="button"
          onClick={handleCopy}
          className="inline-flex size-7 items-center justify-center rounded-md text-muted-foreground hover:text-foreground hover:bg-muted transition-colors cursor-pointer"
        >
          {copied ? <Check className="size-3.5 text-green-600" /> : <Copy className="size-3.5" />}
        </TooltipTrigger>
        <TooltipContent side="top">
          {copied ? "Copied!" : "Copy link"}
        </TooltipContent>
      </Tooltip>

      {/* Modify */}
      <Tooltip>
        <TooltipTrigger
          type="button"
          className="inline-flex size-7 items-center justify-center rounded-md text-muted-foreground hover:text-foreground hover:bg-muted transition-colors cursor-pointer"
        >
          <Pencil className="size-3.5" />
        </TooltipTrigger>
        <TooltipContent side="top">Modify</TooltipContent>
      </Tooltip>

      {/* Delete */}
      <Tooltip>
        <TooltipTrigger
          type="button"
          disabled={isDeleting}
          onClick={handleDelete}
          className="inline-flex size-7 items-center justify-center rounded-md text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors cursor-pointer disabled:opacity-50"
        >
          {isDeleting ? (
            <Loader2 className="size-3.5 animate-spin text-destructive" />
          ) : (
            <Trash2 className="size-3.5" />
          )}
        </TooltipTrigger>
        <TooltipContent side="top">Delete</TooltipContent>
      </Tooltip>
    </div>
  )
}
