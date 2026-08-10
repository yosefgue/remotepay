import * as React from "react"

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
} from "~/components/ui/sidebar"
import { Box, CirclePlus, Contact, LayoutDashboard, PiggyBank, ReceiptText, SquareArrowLeft } from "lucide-react"

// This is sample data.
const data = {
  items: [
    {
      title: "Dashboard",
      url: "#",
      icon: LayoutDashboard,
    },
    {
      title: "Invoices",
      url: "#",
      icon: ReceiptText,
    },
    {
      title: "Inventory",
      url: "#",
      icon: Box,
    },
    {
      title: "Customers",
      url: "#",
      icon: Contact,
    }
  ]
}
export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  return (
    <Sidebar {...props}>
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton size="lg" render={<a href="#" />}>
              <div className="flex aspect-square size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                <PiggyBank className="size-4" />
              </div>
              <div className="flex flex-col gap-0.5 leading-none">
                <span className="font-medium">RemotePay</span>
              </div>
            </SidebarMenuButton>
          </SidebarMenuItem>
          <SidebarMenu className="mt-2">
          <SidebarMenuItem>
            <SidebarMenuButton
              tooltip="New Invoice"
              render={<a href="#" />}
              className=" flex items-center justify-center bg-primary text-primary-foreground hover:bg-primary/80 hover:text-primary-foreground font-semibold shadow-sm"
            >
              <CirclePlus className="size-4 stroke-3" />
              <span>New Invoice</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarMenu>
            {data.items.map((item) => (
              <SidebarMenuItem key={item.title}>
                <SidebarMenuButton
                  size="md"
                  tooltip={item.title}
                  render={<a href={item.url} className="font-medium" />}
                >
                  {item.icon && <item.icon className="size-4" />}
                  <span>{item.title}</span>
                </SidebarMenuButton>
              </SidebarMenuItem>
            ))}
          </SidebarMenu>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter>
        <SidebarGroup>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton
                tooltip="Logout"
                render={<a href="#" className="font-medium" />}
                size="md"
              >
                <SquareArrowLeft className="size-4" />
                <span>Logout</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroup>
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  )
}
