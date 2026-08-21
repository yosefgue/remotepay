import { Button } from "~/components/ui/button";

export default function Connect() {
  const handleConnect = () => {
    window.location.href = "/api/clover/connect";
  };

  return (
    <div className="flex h-screen w-screen flex-col items-center justify-center">
      <Button onClick={handleConnect}>
        Connect with Clover
      </Button>
    </div>
  );
}