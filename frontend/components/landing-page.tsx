"use client";

import { useRouter } from "next/navigation";
import { useCallback } from "react";
import { LocateFixed, Map } from "lucide-react";
import { LandingHero } from "./landing-hero";
import { useGeolocation } from "@/hooks/use-geolocation";

export function LandingPage() {
  const router = useRouter();
  const { locating, error: locationError, requestLocation } = useGeolocation();

  const handleLocate = useCallback(async () => {
    const pos = await requestLocation();
    if (pos) {
      router.push(`/map?lat=${pos.latitude}&lng=${pos.longitude}`);
    }
  }, [router, requestLocation]);

  const handleSearchWithAddress = useCallback(
    (addr: string) => {
      router.push(`/map?address=${encodeURIComponent(addr)}`);
    },
    [router]
  );

  return (
    <LandingHero
      onUseMyLocation={handleLocate}
      onSearchWithAddress={handleSearchWithAddress}
      isLoading={locating}
      locationError={locationError}
      LocateIcon={LocateFixed}
      MapIcon={Map}
    />
  );
}
