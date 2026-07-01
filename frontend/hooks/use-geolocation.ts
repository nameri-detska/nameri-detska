"use client";

import { useState, useCallback } from "react";

type PermissionState = "granted" | "denied" | "prompt" | "unsupported";

async function checkGeolocationPermission(): Promise<PermissionState> {
  if (!navigator.permissions) return "unsupported";
  try {
    const result = await navigator.permissions.query({ name: "geolocation" });
    return result.state as PermissionState;
  } catch {
    return "unsupported";
  }
}

interface GeolocationResult {
  latitude: number;
  longitude: number;
}

export function useGeolocation() {
  const [locating, setLocating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const requestLocation = useCallback(
    async (options?: PositionOptions): Promise<GeolocationResult | null> => {
      setLocating(true);
      setError(null);

      if (!navigator.geolocation) {
        setError("Браузърът не поддържа геолокация.");
        setLocating(false);
        return null;
      }

      const permissionState = await checkGeolocationPermission();
      if (permissionState === "denied") {
        setError("Достъпът до местоположение е блокиран. Разрешете го от настройките на браузъра.");
        setLocating(false);
        return null;
      }

      return new Promise((resolve) => {
        navigator.geolocation.getCurrentPosition(
          (pos) => {
            setLocating(false);
            resolve({
              latitude: pos.coords.latitude,
              longitude: pos.coords.longitude,
            });
          },
          (err) => {
            setError(
              err.code === err.PERMISSION_DENIED
                ? "Достъпът до местоположение е отказан."
                : "Не можахме да определим местоположението."
            );
            setLocating(false);
            resolve(null);
          },
          options ?? {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 300000,
          }
        );
      });
    },
    []
  );

  return { locating, error, requestLocation };
}
