import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "~/components/ui/card";
import { Check, Circle, Loader2 } from "lucide-react";
import { Button } from "~/components/ui/button";

type StepStatus = "pending" | "loading" | "completed";

interface StepItemProps {
  title: string;
  stepStatus?: StepStatus;
}

async function handleSync() {
  const response = await fetch(`/api/clover/sync/customer`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  });
  if (!response.ok) throw new Error("Sync failed");
  return response.json();
}

function SyncStepItem({ title, stepStatus = "pending" }: StepItemProps) {
  return (
    <div className="flex items-center gap-3 py-2">
      <div>
        {stepStatus === "pending" && (
          <Circle className="h-5 w-5 text-muted-foreground/30 transition-colors duration-300" />
        )}
        {stepStatus === "loading" && (
          <Loader2 className="h-5 w-5 animate-spin text-primary transition-colors duration-300" />
        )}
        {stepStatus === "completed" && (
          <Check className="h-5 w-5 text-green-500 animate-in zoom-in-75 transition-colors duration-300" />
        )}
      </div>

      <div>
        <p className="text-sm font-medium leading-none">{title}</p>
      </div>
    </div>
  );
}

export default function Sync() {
  const [merchantStatus, setMerchantStatus] = useState<StepStatus>("pending");

  return (
    <div className="flex h-screen w-screen items-center justify-center bg-background px-4">
      <Card className="w-full max-w-sm">
        <CardHeader className="border-b">
          <CardTitle className="text-lg">Setting up your store</CardTitle>
          <CardDescription>Syncing data with Clover API...</CardDescription>
        </CardHeader>
        <CardContent className="space-y-2">
          <SyncStepItem
            title="Merchant Details"
            stepStatus={merchantStatus}
          />
          <SyncStepItem
            title="Item Catalog"
            stepStatus={merchantStatus}
          />
          <SyncStepItem
            title="Client List"
            stepStatus={merchantStatus}
          />
          <SyncStepItem
            title="Recent Orders"
            stepStatus={merchantStatus}
          />
        </CardContent>
        <CardFooter className="flex justify-center border-t">
          <Button 
            size="lg" 
            onClick={async () => {
              setMerchantStatus("loading");
              try {
                await handleSync();
                setMerchantStatus("completed");
              } catch (error) {
                setMerchantStatus("pending");
                console.error("Sync error:", error);
              }
            }}
            disabled={merchantStatus === "loading"}
          >
            Sync Data
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}