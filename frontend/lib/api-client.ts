import type { Facility } from "@/types/facility";

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

console.log(`[API] NEXT_PUBLIC_BACKEND_URL=${process.env.NEXT_PUBLIC_BACKEND_URL || "(unset)"} -> using ${BACKEND_URL}`);

export async function searchFacilities(): Promise<Facility[]> {
  const url = `${BACKEND_URL}/api/facilities`;
  console.log(`[API] GET ${url}`);

  const res = await fetch(url, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    next: { revalidate: 300 },
  });

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    console.error(`[API] GET ${url} -> ${res.status} ${body}`);
    throw new Error(`Search failed: ${res.status}`);
  }

  console.log(`[API] GET ${url} -> ${res.status}`);

  return res.json();
}
