import { Button } from "~/components/ui/button";

export default function Connect() {
  const handleConnect = async () => {
    const csrfState = crypto.randomUUID();

    sessionStorage.setItem("clover_csrf_state", csrfState);

    try {
      const res = await fetch(`/api/clover/connect?state=${csrfState}`);
      if (!res.ok) throw new Error("Failed to get authorization URL");
      
      const data: { url: string } = await res.json();

      window.location.href = data.url;
    } catch (error) {
      console.error("Error initiating Clover connect:", error);
    }
  };

  return (
    <div className="flex h-screen w-screen flex-col items-center justify-center">
      <Button onClick={handleConnect}>
        Connect with Clover
      </Button>
    </div>
  );
}