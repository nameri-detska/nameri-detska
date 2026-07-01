"use client";

import { useQuery } from "@tanstack/react-query";
import { searchFacilities } from "@/lib/api-client";
import { haversineDistance } from "@/lib/haversine";
import type { Facility, SearchFilters, SearchResponse } from "@/types/facility";

interface UseFacilitiesParams {
  latitude?: number;
  longitude?: number;
  filters: SearchFilters;
  address?: string;
  enabled?: boolean;
}

const SOFIA_CENTER = {latitude: 42.6977, longitude: 23.3219};

export function useFacilities({
                                latitude,
                                longitude,
                                filters,
                                address,
                                enabled = true,
                              }: UseFacilitiesParams) {
  return useQuery<Facility[], Error, SearchResponse>({
    queryKey: ["facilities"],
    queryFn: searchFacilities,
    select: (results) => {
      const centerLat = latitude ?? SOFIA_CENTER.latitude;
      const centerLon = longitude ?? SOFIA_CENTER.longitude;

      if (filters.kidFacilityTypes.length > 0) {
        const types = new Set(filters.kidFacilityTypes);
        if (types.has("KINDERGARTEN") || types.has("NURSERY")) {
          types.add("KINDERGARTEN_WITH_NURSERY");
        }
        results = results.filter((f) => types.has(f.kidFacilityType));
      }

      if (filters.kidFacilityOwnershipTypes.length > 0) {
        const ownership = new Set(filters.kidFacilityOwnershipTypes);
        results = results.filter((f) => ownership.has(f.kidFacilityOwnershipType));
      }

      results = results
        .map((f) => ({
          ...f,
          distanceMeters: haversineDistance(
            centerLat,
            centerLon,
            f.latitude,
            f.longitude
          ),
        }))
        .sort((a, b) => a.distanceMeters - b.distanceMeters);

      return {centerLatitude: centerLat, centerLongitude: centerLon, results};
    },
    enabled,
    staleTime: 5 * 60 * 1000,
  });
}
