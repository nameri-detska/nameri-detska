export interface Facility {
  id: string;
  name: string;
  kidFacilityType: "KINDERGARTEN" | "KINDERGARTEN_WITH_NURSERY" | "NURSERY";
  kidFacilityOwnershipType: "MUNICIPAL" | "PRIVATE_SRZI" | "PRIVATE_MON";
  address: string;
  latitude: number;
  longitude: number;
  distanceMeters: number;
  walkingTimeSeconds: number;
  walkingDistanceMeters: number;
  transitTimeSeconds: number | null;
}

export interface SearchFilters {
  kidFacilityTypes: ("KINDERGARTEN" | "KINDERGARTEN_WITH_NURSERY" | "NURSERY")[];
  kidFacilityOwnershipTypes: ("MUNICIPAL" | "PRIVATE_SRZI" | "PRIVATE_MON")[];
}

export interface SearchResponse {
  centerLatitude: number;
  centerLongitude: number;
  results: Facility[];
}
