export interface GeocodingResult {
  latitude: number;
  longitude: number;
  displayName: string;
}

export async function geocodeAddress(address: string): Promise<GeocodingResult | null> {
  const query = address.toLowerCase().includes("софия") ? address : `София, ${address}`;
  const params = new URLSearchParams({
    q: query,
    format: "json",
    limit: "1",
    countrycodes: "bg",
    viewbox: "23.05108,42.41237,23.67867,42.89656",
    bounded: "1",
  });

  const res = await fetch(`https://nominatim.openstreetmap.org/search?${params}`, {
    headers: {
      "User-Agent": "NameriDetska/1.0",
    },
  });

  if (!res.ok) return null;

  const data = await res.json();
  if (!Array.isArray(data) || data.length === 0) return null;

  return {
    latitude: parseFloat(data[0].lat),
    longitude: parseFloat(data[0].lon),
    displayName: data[0].display_name,
  };
}
