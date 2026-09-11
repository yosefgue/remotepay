import { useLoaderData } from "react-router"
import { Card, CardContent, CardHeader } from "~/components/ui/card"
import { Separator } from "~/components/ui/separator"
import { Badge } from "~/components/ui/badge"

const fmt = (cents: number = 0) => `$${(cents / 100).toFixed(2)}`

export async function clientLoader({ params }: { params: { token?: string } }) {
  const res = await fetch(`/public/checkout/${params.token}`)
  if (!res.ok) {
    return { error: res.status === 410 ? "This payment link has expired." : "Order not found." }
  }
  return { order: await res.json() }
}

export default function PayPage() {
  const { order, error } = useLoaderData() as { order?: any; error?: string }

  if (error || !order) {
    return (
      <div className="min-h-screen flex items-center justify-center p-4 bg-muted/30">
        <Card className="w-full max-w-sm p-6 text-center shadow-sm">
          <h1 className="text-base font-semibold">{error || "Order not found."}</h1>
        </Card>
      </div>
    )
  }

  const customer = order.customer
  const customerName = [customer?.firstName, customer?.lastName].filter(Boolean).join(" ")

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-muted/30">
      <Card className="w-full max-w-lg shadow-sm border bg-background">
        <CardHeader className="p-6 pb-4 border-b">
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-0.5">
              <p className="text-xs font-mono text-muted-foreground uppercase">Invoice #CL-000{order.id}</p>
              <h1 className="text-xl font-bold tracking-tight">{order.merchantName || "Merchant Payment"}</h1>
              {order.title && <p className="text-sm text-muted-foreground">{order.title}</p>}
              {order.createdAt && (
                <p className="text-xs text-muted-foreground">
                  {new Date(order.createdAt).toLocaleString("en-US", { dateStyle: "medium", timeStyle: "short" })}
                </p>
              )}
              {(customerName || customer?.email || customer?.phoneNumber) && (
                <p className="text-sm text-muted-foreground pt-3.5">
                  Billed to: <strong className="font-semibold text-foreground">{customerName || customer?.email || customer?.phoneNumber}</strong>
                </p>
              )}
            </div>
            <Badge variant="outline" className="bg-blue-500/10 text-blue-700 border-blue-500/20">{order.status}</Badge>
          </div>
        </CardHeader>

        <CardContent className="p-6 space-y-4">
          <div className="divide-y rounded-md border text-sm">
            {order.items?.map((item: any) => (
              <div key={item.id} className="flex justify-between p-3">
                <div>
                  <p className="font-medium">{item.name}</p>
                  <p className="text-xs text-muted-foreground">{item.quantity} × {fmt(item.price)}</p>
                </div>
                <span className="font-medium">{fmt(item.price * item.quantity)}</span>
              </div>
            ))}
          </div>

          <div className="space-y-1.5 text-sm pt-2">
            <div className="flex justify-between text-muted-foreground">
              <span>Subtotal</span>
              <span>{fmt(order.subtotalAmount)}</span>
            </div>
            {!!order.taxAmount && (
              <div className="flex justify-between text-muted-foreground">
                <span>Tax</span>
                <span>{fmt(order.taxAmount)}</span>
              </div>
            )}
            <Separator />
            <div className="flex justify-between text-base font-semibold pt-1">
              <span>Total</span>
              <span className="text-xl">{fmt(order.totalAmount)}</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}