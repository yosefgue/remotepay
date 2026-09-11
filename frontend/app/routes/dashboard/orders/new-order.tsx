import { useState, useRef, useEffect } from "react"
import { useLoaderData, useNavigate, redirect } from "react-router"
import { useForm } from "react-hook-form"
import { Card, CardContent, CardHeader, CardTitle } from "~/components/ui/card"
import { Input } from "~/components/ui/input"
import { Button } from "~/components/ui/button"
import { Label } from "~/components/ui/label"
import { Separator } from "~/components/ui/separator"
import { Plus, Minus, Trash2, ArrowLeft, Search, Link as LinkIcon } from "lucide-react"
import { Spinner } from "~/components/ui/spinner"

/* ------------------------------------------------------------------ */
/*  Types & Helpers                                                   */
/* ------------------------------------------------------------------ */

type OrderFormValues = {
  title?: string
  customerName: string
  email: string
  phone: string
}

type Item = { id: string; name: string; price: number | null; stockQuantity: number | null }
type CustomerOption = { customerId?: string; id?: string; firstName: string | null; lastName: string | null; email: string | null; phoneNumber: string | null }
type LineItem = { key: string; itemId: string | null; name: string; price: number; quantity: number }

const fmt = (cents: number) => `$${(cents / 100).toFixed(2)}`
const getCustomerId = (c: CustomerOption) => c.customerId || c.id || ""
const fullName = (c: CustomerOption) =>
  [c.firstName, c.lastName].filter(Boolean).join(" ") || c.email || c.phoneNumber || "Unnamed Customer"

export async function clientLoader(): Promise<{ items: Item[]; customers: CustomerOption[] }> {
  const [itemsRes, customersRes] = await Promise.all([
    fetch("/api/items", { credentials: "include" }),
    fetch("/api/customers", { credentials: "include" }),
  ])
  if (itemsRes.status === 401 || customersRes.status === 401) {
    throw redirect("/")
  }
  if (!itemsRes.ok) throw new Error("Failed to load items")
  if (!customersRes.ok) throw new Error("Failed to load customers")
  return { items: await itemsRes.json(), customers: await customersRes.json() }
}

/* ------------------------------------------------------------------ */
/*  Page Component                                                    */
/* ------------------------------------------------------------------ */

export default function NewOrder() {
  const { items = [], customers = [] } = (useLoaderData() || {}) as { items: Item[]; customers: CustomerOption[] }
  const navigate = useNavigate()

  // Form State via React Hook Form
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<OrderFormValues>({
    defaultValues: {
      title: "",
      customerName: "",
      email: "",
      phone: "",
    },
  })

  const [selectedCustomerId, setSelectedCustomerId] = useState<string | null>(null)
  const [showSuggestions, setShowSuggestions] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const dropdownRef = useRef<HTMLDivElement>(null)

  const customerName = watch("customerName")

  // Cart & Catalog State
  const [lineItems, setLineItems] = useState<LineItem[]>([])
  const [search, setSearch] = useState("")
  const [customName, setCustomName] = useState("")
  const [customPrice, setCustomPrice] = useState("")
  const [isSaving, setIsSaving] = useState(false)
  const [isGenerating, setIsGenerating] = useState(false)

  const subtotal = lineItems.reduce((acc, i) => acc + i.price * i.quantity, 0)
  const filteredItems = items.filter((i) => i.name.toLowerCase().includes(search.toLowerCase()))

  // Filter existing customers by typed name, email, or phone
  const matchingCustomers = customerName && customerName.trim()
    ? customers.filter((c) => {
        const query = customerName.trim().toLowerCase()
        const name = fullName(c).toLowerCase()
        const email = (c.email || "").toLowerCase()
        const phone = (c.phoneNumber || "").toLowerCase()
        return name.includes(query) || email.includes(query) || phone.includes(query)
      })
    : []

  // Close suggestions on outside click
  useEffect(() => {
    const handleOutsideClick = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setShowSuggestions(false)
      }
    }
    document.addEventListener("mousedown", handleOutsideClick)
    return () => document.removeEventListener("mousedown", handleOutsideClick)
  }, [])

  // Select an existing customer: fills fields & links customerId
  const selectExistingCustomer = (c: CustomerOption) => {
    const custId = getCustomerId(c)
    if (!custId) return
    setValue("customerName", fullName(c), { shouldValidate: true })
    setSelectedCustomerId(custId)
    setValue("email", c.email || "")
    setValue("phone", c.phoneNumber || "")
    setShowSuggestions(false)
  }

  // Cart operations
  const addItem = (item: Item) => {
    if (item.price == null) return
    const price = item.price
    setLineItems((prev) => {
      const exists = prev.find((li) => li.itemId === item.id)
      if (exists) return prev.map((li) => (li.itemId === item.id ? { ...li, quantity: li.quantity + 1 } : li))
      return [...prev, { key: `item-${item.id}`, itemId: item.id, name: item.name, price, quantity: 1 }]
    })
  }

  const addCustomItem = () => {
    const priceCents = Math.round(parseFloat(customPrice) * 100)
    if (!customName.trim() || isNaN(priceCents) || priceCents <= 0) return
    setLineItems((prev) => [...prev, { key: `custom-${Date.now()}`, itemId: null, name: customName.trim(), price: priceCents, quantity: 1 }])
    setCustomName("")
    setCustomPrice("")
  }

  const updateQty = (key: string, qty: number) => {
    if (qty < 1) return setLineItems((prev) => prev.filter((i) => i.key !== key))
    setLineItems((prev) => prev.map((i) => (i.key === key ? { ...i, quantity: qty } : i)))
  }

  const submitOrder = async (endpoint: string, data: OrderFormValues) => {
    if (lineItems.length === 0) return
    const isDraft = endpoint.includes("draft")
    if (isDraft) setIsSaving(true)
    else setIsGenerating(true)
    setSubmitError(null)

    try {
      const orderTitle = data.title?.trim() || null
      const [firstName, ...rest] = data.customerName.trim().split(" ")
      const lastName = rest.join(" ")
      const hasCustomerInfo = data.customerName.trim() || data.email.trim() || data.phone.trim()

      const res = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          title: orderTitle,
          customerId: selectedCustomerId || null,
          customer: selectedCustomerId
            ? null
            : hasCustomerInfo
            ? {
                firstName: firstName || null,
                lastName: lastName || null,
                email: data.email.trim() || null,
                phoneNumber: data.phone.trim() || null,
              }
            : null,
          items: lineItems.map((li) => ({ itemId: li.itemId, name: li.name, price: li.price, quantity: li.quantity })),
        }),
      })

      if (res.ok) {
        navigate("/orders")
      } else {
        const err = await res.json().catch(() => null)
        setSubmitError(err?.message || "Failed to process order. Please try again.")
      }
    } catch (e: any) {
      setSubmitError(e?.message || "An unexpected error occurred while processing the order.")
    } finally {
      if (isDraft) setIsSaving(false)
      else setIsGenerating(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon-sm" onClick={() => navigate("/orders")}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">New Order</h1>
          <p className="text-sm text-muted-foreground">Create a new order.</p>
        </div>
      </div>

      {/* Two-Card Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6 items-start">
        {/* LEFT: Order Form Card */}
        <Card className="lg:col-span-3">
          <CardHeader>
            <CardTitle>Order Details</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit((data) => submitOrder("/api/orders/draft", data))} className="space-y-6">
              {/* Order Title */}
              <div className="space-y-2">
                <Label htmlFor="title">
                  Order Title <span className="text-xs font-normal text-muted-foreground">(optional)</span>
                </Label>
                <Input
                  id="title"
                  placeholder="e.g. Table 4, Delivery #12"
                  {...register("title")}
                />
              </div>

              <Separator />

              {/* Customer Section */}
              <div className="space-y-4">
                <div className="space-y-2 relative" ref={dropdownRef}>
                  <div className="flex items-center justify-between">
                    <Label htmlFor="customerName">
                      Customer Name <span className="text-destructive">*</span>
                    </Label>
                    {selectedCustomerId ? (
                      <div className="flex items-center gap-2">
                        <span className="text-[11px] font-medium text-primary bg-primary/10 px-2 py-0.5 rounded-full">
                          Clover Customer
                        </span>
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedCustomerId(null)
                            setValue("customerName", "", { shouldValidate: true })
                            setValue("email", "")
                            setValue("phone", "")
                          }}
                          className="text-xs text-muted-foreground hover:text-foreground underline cursor-pointer"
                        >
                          Change
                        </button>
                      </div>
                    ) : null}
                  </div>
                  <Input
                    id="customerName"
                    placeholder="Enter name or search existing customers…"
                    readOnly={!!selectedCustomerId}
                    aria-invalid={!!errors.customerName}
                    className={selectedCustomerId ? "bg-muted/40 cursor-default" : ""}
                    autoComplete="off"
                    onFocus={() => !selectedCustomerId && setShowSuggestions(true)}
                    {...register("customerName", {
                      required: "Customer name is required",
                      validate: (v) => !!v.trim() || "Customer name cannot be empty",
                      onChange: () => {
                        setSelectedCustomerId(null)
                        setShowSuggestions(true)
                      },
                    })}
                  />
                  {errors.customerName && (
                    <p className="text-xs text-destructive">{errors.customerName.message}</p>
                  )}

                  {/* Suggestions Dropdown */}
                  {!selectedCustomerId && showSuggestions && matchingCustomers.length > 0 && (
                    <div className="absolute z-20 left-0 right-0 top-full mt-1 rounded-md border bg-popover text-popover-foreground shadow-md overflow-hidden">
                      <div className="p-1 max-h-48 overflow-y-auto">
                        <div className="px-2 py-1 text-[11px] font-medium text-muted-foreground">Existing Customers</div>
                        {matchingCustomers.map((c) => (
                          <button
                            key={getCustomerId(c)}
                            type="button"
                            onClick={() => selectExistingCustomer(c)}
                            className="w-full text-left px-2.5 py-1.5 rounded-sm text-sm hover:bg-muted transition-colors cursor-pointer flex flex-col"
                          >
                            <span className="font-medium leading-tight">{fullName(c)}</span>
                            {(c.email || c.phoneNumber) && (
                              <span className="text-xs text-muted-foreground mt-0.5">
                                {[c.email, c.phoneNumber].filter(Boolean).join(" • ")}
                              </span>
                            )}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </div>

                {/* Email & Phone - Disabled/Readonly when preexisting customer */}
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="email" className="text-muted-foreground">
                      Email {selectedCustomerId ? "" : <span className="text-xs font-normal">(optional)</span>}
                    </Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder={selectedCustomerId ? "No email on file" : "customer@email.com"}
                      disabled={!!selectedCustomerId}
                      aria-invalid={!!errors.email}
                      className={selectedCustomerId ? "disabled:opacity-80 disabled:cursor-default bg-muted/30" : ""}
                      {...register("email", {
                        pattern: {
                          value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                          message: "Please enter a valid email address",
                        },
                      })}
                    />
                    {errors.email && (
                      <p className="text-xs text-destructive">{errors.email.message}</p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="phone" className="text-muted-foreground">
                      Phone {selectedCustomerId ? "" : <span className="text-xs font-normal">(optional)</span>}
                    </Label>
                    <Input
                      id="phone"
                      type="tel"
                      placeholder={selectedCustomerId ? "No phone on file" : "+1 (555) 000-0000"}
                      disabled={!!selectedCustomerId}
                      className={selectedCustomerId ? "disabled:opacity-80 disabled:cursor-default bg-muted/30" : ""}
                      {...register("phone")}
                    />
                  </div>
                </div>
              </div>

              <Separator />

              {/* Line Items List */}
              <div className="space-y-3">
                <Label>Line Items</Label>
                {lineItems.length === 0 ? (
                  <div className="rounded-md border border-dashed py-8 text-center text-sm text-muted-foreground">
                    No items added yet. Select items from the panel →
                  </div>
                ) : (
                  <div className="space-y-2">
                    {lineItems.map((li) => (
                      <div key={li.key} className="flex items-center justify-between rounded-md border px-3 py-2">
                        <div className="min-w-0 flex-1 mr-4">
                          <p className="text-sm font-medium truncate">{li.name}</p>
                          <p className="text-xs text-muted-foreground">{fmt(li.price)} each</p>
                        </div>
                        <div className="flex items-center gap-2 shrink-0">
                          <div className="flex items-center gap-1">
                            <Button type="button" variant="outline" size="icon-xs" onClick={() => updateQty(li.key, li.quantity - 1)}><Minus className="h-3 w-3" /></Button>
                            <Input
                              type="number"
                              min={1}
                              value={li.quantity}
                              onChange={(e) => updateQty(li.key, parseInt(e.target.value) || 1)}
                              className="h-6 w-12 text-center text-xs px-1"
                            />
                            <Button type="button" variant="outline" size="icon-xs" onClick={() => updateQty(li.key, li.quantity + 1)}><Plus className="h-3 w-3" /></Button>
                          </div>
                          <span className="w-16 text-right text-sm font-medium">{fmt(li.price * li.quantity)}</span>
                          <Button type="button" variant="ghost" size="icon-xs" onClick={() => updateQty(li.key, 0)} className="text-muted-foreground hover:text-destructive">
                            <Trash2 className="h-3 w-3" />
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Totals & Actions */}
              {lineItems.length > 0 && (
                <>
                  <Separator />
                  <div className="space-y-1.5 text-sm">
                    <div className="flex justify-between text-muted-foreground"><span>Subtotal</span><span>{fmt(subtotal)}</span></div>
                    <div className="flex justify-between text-base font-semibold text-foreground"><span>Total</span><span>{fmt(subtotal)}</span></div>
                  </div>
                </>
              )}

              <Separator />

              {submitError && (
                <div className="rounded-md bg-destructive/10 p-3 text-xs text-destructive font-medium">
                  {submitError}
                </div>
              )}

              <div className="flex gap-3">
                <Button
                  type="submit"
                  variant="outline"
                  disabled={lineItems.length === 0 || isSaving || isGenerating}
                  className="flex-1"
                >
                  {isSaving ? <Spinner className="size-4" /> : "Save as Draft"}
                </Button>
                <Button
                  type="button"
                  disabled={lineItems.length === 0 || isSaving || isGenerating}
                  onClick={handleSubmit((data) => submitOrder("/api/orders", data))}
                  className="flex-1"
                >
                  {isGenerating ? (
                    <Spinner className="size-4" />
                  ) : (
                    <>
                      <LinkIcon className="h-4 w-4" /> Generate Link
                    </>
                  )}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* RIGHT: Item Selection Card */}
        <Card className="lg:col-span-2 lg:sticky lg:top-20">
          <CardHeader>
            <CardTitle>Select Items</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Search */}
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground pointer-events-none" />
              <Input placeholder="Search items…" value={search} onChange={(e) => setSearch(e.target.value)} className="pl-8" />
            </div>

            {/* 3-Col Capsules */}
            {filteredItems.length > 0 ? (
              <div className="grid grid-cols-3 gap-2 max-h-[380px] overflow-y-auto pr-1">
                {filteredItems.map((item) => {
                  const qty = lineItems.find((li) => li.itemId === item.id)?.quantity ?? 0
                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => addItem(item)}
                      disabled={item.price == null}
                      className={`flex flex-col justify-between rounded-lg border p-2 text-left text-xs transition-colors cursor-pointer hover:border-primary disabled:opacity-50 min-h-[64px] ${
                        qty > 0 ? "border-primary bg-primary/5" : "border-border"
                      }`}
                    >
                      <div className="flex items-start justify-between gap-1 w-full">
                        <span className="font-medium leading-tight line-clamp-2">{item.name}</span>
                        <Plus className={`size-3.5 shrink-0 mt-0.5 ${qty > 0 ? "text-primary" : "text-muted-foreground"}`} />
                      </div>
                      <span className="mt-1 text-muted-foreground">{item.price != null ? fmt(item.price) : "—"}</span>
                    </button>
                  )
                })}
              </div>
            ) : (
              <p className="py-6 text-center text-sm text-muted-foreground">No items found.</p>
            )}

            <Separator />

            {/* Custom Item */}
            <div className="space-y-2">
              <Label>Custom Item</Label>
              <div className="flex gap-2">
                <Input placeholder="Item name" value={customName} onChange={(e) => setCustomName(e.target.value)} className="flex-1" />
                <Input type="number" step="0.01" min="0" placeholder="Price" value={customPrice} onChange={(e) => setCustomPrice(e.target.value)} className="w-20" />
                <Button type="button" variant="outline" size="icon" onClick={addCustomItem} disabled={!customName.trim() || !customPrice.trim()}>
                  <Plus className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
