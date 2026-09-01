import { createColumnHelper } from "@tanstack/react-table"
import { type DataTableFeatures } from "~/lib/data-table-features"

export type Item = {
  id: string
  name: string
  price: number | null
  stockQuantity: number | null
}

const columnHelper = createColumnHelper<DataTableFeatures, Item>()

export const columns = columnHelper.columns([
  columnHelper.accessor("name", {
    header: "Item Name",
    filterFn: "includesString",
    cell: (info) => (
      <span className="font-medium text-foreground">{info.getValue() || "—"}</span>
    ),
  }),
  columnHelper.accessor("stockQuantity", {
    header: "Stock Count",
    cell: (info) => {
      const v = info.getValue()
      return v != null ? v : <span className="text-muted-foreground">—</span>
    },
  }),
  columnHelper.accessor("price", {
    header: "Price",
    cell: (info) => {
      const v = info.getValue()
      return v != null
        ? `$${(v / 100).toFixed(2)}`
        : <span className="text-muted-foreground">—</span>
    },
  }),
])

