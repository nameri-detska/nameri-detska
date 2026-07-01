"use client";

import { useRouter } from "next/navigation";
import { useState, useCallback } from "react";
import { LocateFixed, Map } from "lucide-react";
import { LandingHero } from "./landing-hero";

export function LandingPage() {
  const router = useRouter();
  const [locating, setLocating] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);

  const handleLocate = useCallback(() => {
    setLocating(true);
    setLocationError(null);
    if (!navigator.geolocation) {
      setLocationError("Браузърът не поддържа геолокация.");
      setLocating(false);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLocating(false);
        router.push(`/map?lat=${pos.coords.latitude}&lng=${pos.coords.longitude}`);
      },
      (err) => {
        setLocationError(
          err.code === err.PERMISSION_DENIED
            ? "Достъпът до местоположение е отказан."
            : "Не можахме да определим местоположението."
        );
        setLocating(false);
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 300000 }
    );
  }, [router]);

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
