import { Button } from "~/components/ui/button"

export default function Home() {
  return (
    <div className="flex h-screen w-screen flex-col items-center justify-center">
      <Button onClick={() => window.location.replace("http://localhost:8080/api/clover/connect")}>
        Connect To Clover
      </Button>
    </div>
  )
}
