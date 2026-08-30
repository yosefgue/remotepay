import { createColumnHelper } from "@tanstack/react-table"
import { type DataTableFeatures } from "~/lib/data-table-features"

export type Customer = {
  id: number
  customerId: string
  merchantId: string
  firstName: string | null
  lastName: string | null
  email: string | null
  phoneNumber: string | null
}

const columnHelper = createColumnHelper<DataTableFeatures, Customer>()

export const columns = columnHelper.columns([
  columnHelper.accessor(
    (row) => {
      const parts = [row.firstName, row.lastName].filter(Boolean)
      return parts.length > 0 ? parts.join(" ") : "Unnamed Customer"
    },
    {
      id: "fullName",
      header: "Full Name",
      cell: (info) => (
        <span className="font-medium text-foreground">{info.getValue()}</span>
      ),
    }
  ),
  columnHelper.accessor("email", {
    header: "Email",
    cell: (info) => info.getValue() || <span className="text-muted-foreground">—</span>,
  }),
  columnHelper.accessor("phoneNumber", {
    header: "Phone Number",
    cell: (info) => info.getValue() || <span className="text-muted-foreground">—</span>,
  }),
])