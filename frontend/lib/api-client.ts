import type { Facility } from "@/types/facility";

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

export async function searchFacilities(): Promise<Facility[]> {
  const res = await fetch(`${BACKEND_URL}/api/facilities`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    next: { revalidate: 300 },
  });

  if (!res.ok) {
    throw new Error(`Search failed: ${res.status}`);
  }

  return res.json();
}
