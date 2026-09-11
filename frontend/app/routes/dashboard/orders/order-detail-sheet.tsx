import { useEffect, useState } from "react"
import { useRevalidator } from "react-router"
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
} from "~/components/ui/sheet"
import { Button } from "~/components/ui/button"
import { Separator } from "~/components/ui/separator"
import {
  Mail,
  Phone,
  User,
  Link as LinkIcon,
  Copy,
  Check,
  Calendar,
  AlertCircle,
  Trash2,
} from "lucide-react"
import { Spinner } from "~/components/ui/spinner"

export interface OrderItemDetail {
  id: number
  itemId: string | null
  name: string
  price: number // in cents
  quantity: number
}

export interface CustomerDetail {
  customerId: string
  firstName: string | null
  lastName: string | null
  email: string | null
  phoneNumber: string | null
}

export interface OrderDetail {
  id: number
  title: string | null
  customer: CustomerDetail | null
  status: string
  currency: string
  subtotalAmount: number // in cents
  taxAmount: number // in cents
  totalAmount: number // in cents
  linkToken: string | null
  createdAt: string
  items: OrderItemDetail[]
}

interface OrderDetailSheetProps {
  orderId: number | null
  onClose: () => void
}

const fmt = (cents: number | null | undefined, currency: string = "USD") => {
  if (cents == null) return "—"
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: currency || "USD",
  }).format(cents / 100)
}

const formatDate = (isoString: string) => {
  try {
    return new Date(isoString).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "numeric",
      minute: "2-digit",
    })
  } catch {
    return isoString
  }
}

const statusConfig: Record<
  string,
  { label: string; badgeClass: string }
> = {
  DRAFT: {
    label: "DRAFT",
    badgeClass: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300 border-gray-200 dark:border-gray-700",
  },
  OPEN: {
    label: "OPEN",
    badgeClass: "bg-blue-50 text-blue-700 dark:bg-blue-950 dark:text-blue-300 border-blue-200 dark:border-blue-800",
  },
  PAID: {
    label: "PAID",
    badgeClass: "bg-green-50 text-green-700 dark:bg-green-950 dark:text-green-300 border-green-200 dark:border-green-800",
  },
  CANCELLED: {
    label: "CANCELLED",
    badgeClass: "bg-red-50 text-red-700 dark:bg-red-950 dark:text-red-300 border-red-200 dark:border-red-800",
  },
  EXPIRED: {
    label: "EXPIRED",
    badgeClass: "bg-amber-50 text-amber-700 dark:bg-amber-950 dark:text-amber-300 border-amber-200 dark:border-amber-800",
  },
}

const getInitials = (firstName?: string | null, lastName?: string | null) => {
  const f = firstName?.trim().charAt(0) || ""
  const l = lastName?.trim().charAt(0) || ""
  const initials = (f + l).toUpperCase()
  return initials || null
}

export function OrderDetailSheet({ orderId, onClose }: OrderDetailSheetProps) {
  const [order, setOrder] = useState<OrderDetail | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const revalidator = useRevalidator()

  useEffect(() => {
    if (!orderId) {
      setOrder(null)
      setError(null)
      setActionError(null)
      return
    }

    let isMounted = true
    setLoading(true)
    setError(null)
    setActionError(null)

    fetch(`/api/orders/${orderId}`, { credentials: "include" })
      .then(async (res) => {
        if (!res.ok) {
          throw new Error(`Failed to load order #${orderId} (${res.status})`)
        }
        return res.json()
      })
      .then((data: OrderDetail) => {
        if (isMounted) {
          setOrder(data)
        }
      })
      .catch((err) => {
        if (isMounted) {
          setError(err.message || "Could not retrieve order details.")
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoading(false)
        }
      })

    return () => {
      isMounted = false
    }
  }, [orderId])

  const handleCopyLink = () => {
    if (!order?.linkToken) return
    const payUrl = `${window.location.origin}/pay/${order.linkToken}`
    navigator.clipboard.writeText(payUrl)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const handleDelete = async () => {
    if (!order?.id || isDeleting) return
    setIsDeleting(true)
    setActionError(null)

    try {
      const res = await fetch(`/api/orders/${order.id}`, {
        method: "DELETE",
        credentials: "include",
      })

      if (!res.ok) {
        throw new Error("Failed to delete order")
      }

      revalidator.revalidate()
      onClose()
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "Failed to delete order")
      setIsDeleting(false)
    }
  }

  const currentStatus = order?.status?.toUpperCase() || "DRAFT"
  const statusMeta = statusConfig[currentStatus] || {
    label: currentStatus,
    badgeClass: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300 border-gray-200",
  }

  const customerFullName = order?.customer
    ? [order.customer.firstName, order.customer.lastName]
        .filter(Boolean)
        .join(" ")
    : null

  const initials = getInitials(order?.customer?.firstName, order?.customer?.lastName)

  return (
    <Sheet open={!!orderId} onOpenChange={(open) => !open && onClose()}>
      <SheetContent
        side="right"
        className="sm:max-w-md w-full flex flex-col p-0 gap-0 h-full overflow-hidden bg-background shadow-xl"
      >
        {/* Top Header */}
        <SheetHeader className="p-5 border-b bg-card">
          <div className="flex items-start justify-between gap-4 pr-8">
            <div className="min-w-0 flex-1">
              <span className="font-mono text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                {order ? `CL-000${order.id}` : orderId ? `CL-000${orderId}` : "Order Details"}
              </span>
              <SheetTitle className="text-xl font-semibold mt-0.5 truncate">
                {order?.title ? order.title : order ? `Order #CL-000${order.id}` : orderId ? `Order #CL-000${orderId}` : "Order Details"}
              </SheetTitle>
              {order?.createdAt && (
                <SheetDescription className="flex items-center gap-1.5 mt-1 text-xs">
                  <Calendar className="size-3.5" />
                  {formatDate(order.createdAt)}
                </SheetDescription>
              )}
            </div>

            {order && (
              <span
                className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold shrink-0 mr-2 mt-0.5 ${statusMeta.badgeClass}`}
              >
                {statusMeta.label}
              </span>
            )}
          </div>
        </SheetHeader>

        {/* Sheet Content Area */}
        <div
          className={`flex-1 overflow-y-auto p-5 ${
            loading ? "flex items-center justify-center" : "space-y-4"
          }`}
        >
          {loading && <Spinner className="size-6 text-muted-foreground" />}

          {error && (
            <div className="rounded-lg border border-destructive/20 bg-destructive/10 p-4 text-sm text-destructive flex items-start gap-3">
              <AlertCircle className="size-5 shrink-0 mt-0.5" />
              <div className="space-y-1">
                <p className="font-medium">Failed to load order</p>
                <p className="text-xs opacity-90">{error}</p>
              </div>
            </div>
          )}

          {!loading && !error && order && (
            <>
              {/* Customer Information: Unified Block */}
              <div className="rounded-lg border bg-card p-4 space-y-3 shadow-xs">
                <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Customer
                </div>

                {customerFullName || order.customer?.email || order.customer?.phoneNumber ? (
                  <div className="flex items-start gap-3 pt-0.5">
                    <div className="size-9 rounded-full bg-primary/10 text-primary flex items-center justify-center font-semibold text-xs shrink-0">
                      {initials || <User className="size-4 text-primary" />}
                    </div>
                    <div className="min-w-0 flex-1 space-y-1">
                      <p className="text-sm font-semibold text-foreground truncate">
                        {customerFullName || "Unnamed Customer"}
                      </p>
                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted-foreground">
                        {order.customer?.email && (
                          <div className="flex items-center gap-1.5 min-w-0">
                            <Mail className="size-3 text-muted-foreground/70 shrink-0" />
                            <span className="truncate">{order.customer.email}</span>
                          </div>
                        )}
                        {order.customer?.phoneNumber && (
                          <div className="flex items-center gap-1.5 shrink-0">
                            <Phone className="size-3 text-muted-foreground/70 shrink-0" />
                            <span>{order.customer.phoneNumber}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center gap-2 py-1 text-xs text-muted-foreground">
                    <User className="size-4 opacity-50 shrink-0" />
                    <span>No customer attached to this order</span>
                  </div>
                )}
              </div>

              {/* Invoice Breakdown: Unified Block */}
              <div className="rounded-lg border bg-card p-4 space-y-3 shadow-xs">
                <div className="flex items-center justify-between border-b pb-2.5">
                  <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Items
                  </span>
                  <span className="text-xs text-muted-foreground">
                    {order.items?.length || 0} {order.items?.length === 1 ? "item" : "items"}
                  </span>
                </div>

                {/* Items List */}
                {order.items && order.items.length > 0 ? (
                  <div className="divide-y divide-border/40">
                    {order.items.map((item) => {
                      const lineTotal = (item.price || 0) * (item.quantity || 1)
                      return (
                        <div
                          key={item.id}
                          className="py-2.5 first:pt-0 last:pb-0 flex items-start justify-between gap-3 text-xs"
                        >
                          <div className="min-w-0 flex-1">
                            <p className="font-medium text-foreground truncate">{item.name}</p>
                            <p className="text-muted-foreground mt-0.5">
                              {item.quantity} × {fmt(item.price, order.currency)}
                            </p>
                          </div>
                          <span className="font-medium text-foreground shrink-0 tabular-nums">
                            {fmt(lineTotal, order.currency)}
                          </span>
                        </div>
                      )
                    })}
                  </div>
                ) : (
                  <p className="text-xs text-muted-foreground italic py-1">
                    No line items in this order.
                  </p>
                )}

                {/* Financial Summary */}
                <div className="border-t pt-3 space-y-1.5 text-xs">
                  <div className="flex justify-between text-muted-foreground">
                    <span>Subtotal</span>
                    <span className="tabular-nums">{fmt(order.subtotalAmount, order.currency)}</span>
                  </div>
                  {order.taxAmount != null && order.taxAmount > 0 && (
                    <div className="flex justify-between text-muted-foreground">
                      <span>Tax</span>
                      <span className="tabular-nums">{fmt(order.taxAmount, order.currency)}</span>
                    </div>
                  )}
                  <div className="flex justify-between pt-2 text-sm font-semibold text-foreground border-t border-dashed">
                    <span>Total</span>
                    <span className="tabular-nums text-base">{fmt(order.totalAmount, order.currency)}</span>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Footer Actions */}
        {!loading && order && (
          <div className="p-4 border-t bg-card mt-auto space-y-2">
            {actionError && (
              <p className="text-xs text-destructive font-medium">{actionError}</p>
            )}

            {currentStatus === "DRAFT" && (
              <div className="flex flex-col gap-2">
                <Button type="button" className="w-full gap-2" disabled={isDeleting}>
                  <LinkIcon className="size-4" />
                  Generate Payment Link
                </Button>
                <Button
                  type="button"
                  variant="destructive"
                  onClick={handleDelete}
                  disabled={isDeleting}
                  className="w-full gap-2"
                >
                  {isDeleting ? <Spinner className="size-4" /> : <Trash2 className="size-4" />}
                  Delete Order
                </Button>
              </div>
            )}

            {currentStatus === "OPEN" && (
              <Button
                type="button"
                variant="default"
                onClick={handleCopyLink}
                className="w-full gap-2"
              >
                {copied ? (
                  <>
                    <Check className="size-4 text-green-600" />
                    Link Copied!
                  </>
                ) : (
                  <>
                    <Copy className="size-4"/>
                    Copy Payment Link
                  </>
                )}
              </Button>
            )}
          </div>
        )}
      </SheetContent>
    </Sheet>
  )
}
