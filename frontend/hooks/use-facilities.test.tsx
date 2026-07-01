import { describe, expect, test, beforeAll, afterEach, afterAll } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { http, HttpResponse } from "msw";
import { setupServer } from "msw/node";
import { useFacilities } from "./use-facilities";
import type { Facility, SearchFilters } from "@/types/facility";

const server = setupServer(
  http.get("http://localhost:8080/api/facilities", () => {
    return HttpResponse.json(mockFacilities);
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

const mockFacilities: Facility[] = [
  {
    id: "1", name: "ДГ Слънце", kidFacilityType: "KINDERGARTEN",
    kidFacilityOwnershipType: "MUNICIPAL", address: "гр. София, ул. А",
    latitude: 42.7, longitude: 23.33,
    distanceMeters: 0, walkingTimeSeconds: 0, walkingDistanceMeters: 0, transitTimeSeconds: null,
  },
  {
    id: "2", name: "ДГ Звезда", kidFacilityType: "KINDERGARTEN_WITH_NURSERY",
    kidFacilityOwnershipType: "PRIVATE_MON", address: "гр. София, ул. Б",
    latitude: 42.71, longitude: 23.34,
    distanceMeters: 0, walkingTimeSeconds: 0, walkingDistanceMeters: 0, transitTimeSeconds: null,
  },
  {
    id: "3", name: "ДГ Иглика", kidFacilityType: "NURSERY",
    kidFacilityOwnershipType: "PRIVATE_SRZI", address: "гр. София, ул. В",
    latitude: 42.72, longitude: 23.35,
    distanceMeters: 0, walkingTimeSeconds: 0, walkingDistanceMeters: 0, transitTimeSeconds: null,
  },
];

const emptyFilters: SearchFilters = { kidFacilityTypes: [], kidFacilityOwnershipTypes: [] };

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}

describe("useFacilities", () => {
  test("returns all facilities with distance when no filters active", async () => {
    const { result } = renderHook(
      () => useFacilities({ filters: emptyFilters }),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data!.results).toHaveLength(3);
    expect(result.current.data!.results[0].distanceMeters).toBeDefined();
  });

  test("sorts by distance nearest first", async () => {
    const { result } = renderHook(
      () => useFacilities({ filters: emptyFilters, latitude: 42.6977, longitude: 23.3219 }),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    const distances = result.current.data!.results.map((f) => f.distanceMeters);
    for (let i = 1; i < distances.length; i++) {
      expect(distances[i]).toBeGreaterThanOrEqual(distances[i - 1]);
    }
  });

  test("falls back to Sofia center when no coordinates provided", async () => {
    const { result } = renderHook(
      () => useFacilities({ filters: emptyFilters }),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data!.centerLatitude).toBe(42.6977);
    expect(result.current.data!.centerLongitude).toBe(23.3219);
  });

  test("uses user coordinates when provided", async () => {
    const { result } = renderHook(
      () => useFacilities({ filters: emptyFilters, latitude: 42.7, longitude: 23.33 }),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data!.centerLatitude).toBe(42.7);
  });

  test("auto-includes KINDERGARTEN_WITH_NURSERY when filtering KINDERGARTEN", async () => {
    const filters: SearchFilters = {
      kidFacilityTypes: ["KINDERGARTEN"],
      kidFacilityOwnershipTypes: [],
    };

    const { result } = renderHook(
      () => useFacilities({ filters }),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    const types = result.current.data!.results.map((f) => f.kidFacilityType);
    expect(types).toContain("KINDERGARTEN");
    expect(types).toContain("KINDERGARTEN_WITH_NURSERY");
    expect(types).not.toContain("NURSERY");
  });

  test("auto-includes KINDERGARTEN_WITH_NURSERY when filtering NURSERY", async () => {
    const filters: SearchFilters = {
      kidFacilityTypes: ["NURSERY"],
      kidFacilityOwnershipTypes: [],
    };

    const { result } = renderHook(
      () => useFacilities({ filters }),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    const types = result.current.data!.results.map((f) => f.kidFacilityType);
    expect(types).toContain("NURSERY");
    expect(types).toContain("KINDERGARTEN_WITH_NURSERY");
    expect(types).not.toContain("KINDERGARTEN");
  });

  test("KINDERGARTEN_WITH_NURSERY alone does not expand", async () => {
    const filters: SearchFilters = {
      kidFacilityTypes: ["KINDERGARTEN_WITH_NURSERY"],
      kidFacilityOwnershipTypes: [],
    };

    const { result } = renderHook(
      () => useFacilities({ filters }),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    const types = result.current.data!.results.map((f) => f.kidFacilityType);
    expect(types).toEqual(["KINDERGARTEN_WITH_NURSERY"]);
  });

  test("filters by ownership", async () => {
    const filters: SearchFilters = {
      kidFacilityTypes: [],
      kidFacilityOwnershipTypes: ["MUNICIPAL"],
    };

    const { result } = renderHook(
      () => useFacilities({ filters }),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data!.results).toHaveLength(1);
    expect(result.current.data!.results[0].kidFacilityOwnershipType).toBe("MUNICIPAL");
  });
});
