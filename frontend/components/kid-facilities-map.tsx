"use client";

import { useFacilities } from "@/hooks/use-facilities";
import type { Facility, SearchFilters } from "@/types/facility";
import { useState, useCallback, useEffect, useRef } from "react";
import { useSearchParams } from "next/navigation";
import { ChevronDown, ChevronUp, List, LocateFixed, Map, Search, SlidersHorizontal, X } from "lucide-react";
import { FacilityMap } from "./facility-map";
import { FacilityList } from "./facility-list";
import { FacilityDetailCard } from "./facility-detail-card";
import { geocodeAddress } from "@/lib/geocode";
import { OWNERSHIP_COLORS } from "@/lib/utils";

const SOFIA_CENTER = { latitude: 42.6977, longitude: 23.3219 };

export function KidFacilitiesMap() {
  const searchParams = useSearchParams();
  const initialAddress = searchParams.get("address") ?? undefined;
  const isBrowse = searchParams.get("browse") === "1";

  const [hasSearched, setHasSearched] = useState(false);
  const [userLocation, setUserLocation] = useState<{ latitude: number; longitude: number } | null>(null);

  useEffect(() => {
    const latParam = searchParams.get("lat");
    const lngParam = searchParams.get("lng");
    if (!initialAddress && !isBrowse && latParam && lngParam) {
      const lat = parseFloat(latParam);
      const lng = parseFloat(lngParam);
      if (!isNaN(lat) && !isNaN(lng)) {
        setUserLocation({ latitude: lat, longitude: lng });
        setDisplayCount(10);
        setShowAllRequested(false);
      }
    }
    setHasSearched(true);
  }, []);
  const [searchAddress, setSearchAddress] = useState<string | undefined>(initialAddress);
  const [locating, setLocating] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [address, setAddress] = useState(initialAddress ?? "");

  const [filters, setFilters] = useState<SearchFilters>({
    kidFacilityTypes: [],
    kidFacilityOwnershipTypes: [],
  });

  const [filtersOpen, setFiltersOpen] = useState(true);
  const [listVisible, setListVisible] = useState(false);
  const [isDesktop, setIsDesktop] = useState(false);
  const [selectedFacilityId, setSelectedFacilityId] = useState<string | null>(null);
  const [displayCount, setDisplayCount] = useState(10);
  const [showAllRequested, setShowAllRequested] = useState(false);
  const [textFilter, setTextFilter] = useState("");
  const showAllRequestedRef = useRef(false);
  const displayCountRef = useRef(10);
  const allFacilitiesRef = useRef<Facility[]>([]);
  const [hydrated, setHydrated] = useState(false);
  useEffect(() => { setHydrated(true); }, []);

  useEffect(() => {
    const mq = window.matchMedia("(min-width: 1024px)");
    setIsDesktop(mq.matches);
    const handler = (e: MediaQueryListEvent) => setIsDesktop(e.matches);
    mq.addEventListener("change", handler);
    return () => mq.removeEventListener("change", handler);
  }, []);


  const hasUserLocation = userLocation != null || searchAddress != null;

  const { data, isLoading, error } = useFacilities({
    latitude: userLocation?.latitude,
    longitude: userLocation?.longitude,
    filters,
    address: searchAddress,
    enabled: hasSearched,
  });

  const [showSpinner, setShowSpinner] = useState(true);
  const handleMapReady = useCallback(() => setShowSpinner(false), []);

  useEffect(() => {
    if (!hydrated) return;
    const t = setTimeout(() => setShowSpinner(false), 5000);
    return () => clearTimeout(t);
  }, [hydrated]);
  const searchResponse = data;
  const allFacilities: Facility[] = searchResponse?.results ?? [];
  displayCountRef.current = displayCount;
  allFacilitiesRef.current = allFacilities;
  const filteredFacilities = textFilter.trim()
    ? allFacilities.filter((f) =>
        f.name.toLowerCase().includes(textFilter.toLowerCase()) ||
        f.address.toLowerCase().includes(textFilter.toLowerCase())
      )
    : allFacilities;
  const effectiveDisplayCount = showAllRequested ? Infinity : displayCount;
  const displayedFacilities = hasUserLocation ? filteredFacilities.slice(0, effectiveDisplayCount) : filteredFacilities;
  const hiddenCount = hasUserLocation && !showAllRequested ? filteredFacilities.length - displayCount : 0;
  const mapFacilities = displayedFacilities;
  const selectedFacility = selectedFacilityId ? allFacilities.find((f) => f.id === selectedFacilityId) ?? null : null;
  showAllRequestedRef.current = showAllRequested;

  const centerLat = searchResponse?.centerLatitude ?? userLocation?.latitude ?? SOFIA_CENTER.latitude;
  const centerLon = searchResponse?.centerLongitude ?? userLocation?.longitude ?? SOFIA_CENTER.longitude;

  const hasFilters = filters.kidFacilityTypes.length > 0 || filters.kidFacilityOwnershipTypes.length > 0;
  const locationLabel = searchAddress ?? (userLocation ? "Вашето местоположение" : undefined);

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
        setUserLocation({ latitude: pos.coords.latitude, longitude: pos.coords.longitude });
        setSearchAddress(undefined);
        setLocating(false);
        setHasSearched(true);
        setDisplayCount(10);
        setShowAllRequested(false);
      },
      (err) => {
        setLocationError(err.code === err.PERMISSION_DENIED ? "Достъпът до местоположение е отказан." : "Не можахме да определим местоположението.");
        setLocating(false);
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 300000 }
    );
  }, []);

  const handleSearch = useCallback(async () => {
    if (!address.trim()) return;
    const addr = address.trim();
    setSearchAddress(addr);
    setUserLocation(null);
    setHasSearched(true);
    setSelectedFacilityId(null);
    setDisplayCount(10);
    setShowAllRequested(false);

    try {
      const geo = await geocodeAddress(addr);
      if (geo) {
        setUserLocation({ latitude: geo.latitude, longitude: geo.longitude });
      }
    } catch {
      // geocoding failed, backend will handle the raw address
    }
  }, [address]);

  const didAutoSearch = useRef(false);
  useEffect(() => {
    if (initialAddress && hydrated && hasSearched && !didAutoSearch.current) {
      didAutoSearch.current = true;
      handleSearch();
    }
  }, [initialAddress, hydrated, hasSearched, handleSearch]);

  const handleFilterToggle = useCallback((key: keyof SearchFilters, value: string) => {
    setFilters((prev) => {
      const arr = prev[key] as string[];
      return { ...prev, [key]: arr.includes(value) ? arr.filter((v) => v !== value) : [...arr, value] };
    });
    if (!showAllRequestedRef.current) {
      setDisplayCount(10);
    }
  }, []);

  const handleClearFilters = useCallback(() => {
    setFilters({ kidFacilityTypes: [], kidFacilityOwnershipTypes: [] });
    setDisplayCount(10);
    setShowAllRequested(false);
  }, []);

  const handleSelectFacility = useCallback((facilityId: string | null) => {
    setSelectedFacilityId(facilityId);
  }, []);

  const handleMapSelectFacility = useCallback((facilityId: string | null) => {
    setSelectedFacilityId(facilityId);
    if (facilityId && !showAllRequestedRef.current) {
      const idx = allFacilitiesRef.current.findIndex((f) => f.id === facilityId);
      if (idx >= 0 && idx >= displayCountRef.current) {
        setDisplayCount(idx + 1);
      }
    }
  }, []);

  const handleUserLocationChange = useCallback((lat: number, lng: number) => {
    setUserLocation({ latitude: lat, longitude: lng });
    setSearchAddress(undefined);
    setHasSearched(true);
    if (!showAllRequestedRef.current) {
      setDisplayCount(10);
    }
    setSelectedFacilityId(null);
  }, []);

  const typeOptions = [
    { key: "kidFacilityTypes" as const, value: "KINDERGARTEN", label: "Градина", icon: "/teddy-bear.svg" },
    { key: "kidFacilityTypes" as const, value: "NURSERY", label: "Ясла", icon: "/pacifier-baby.svg" },
  ];
  const ownershipOptions = [
    { key: "kidFacilityOwnershipTypes" as const, value: "MUNICIPAL", label: "Общинска", color: OWNERSHIP_COLORS.MUNICIPAL },
    { key: "kidFacilityOwnershipTypes" as const, value: "PRIVATE_SRZI", label: "Частна СРЗИ", color: OWNERSHIP_COLORS.PRIVATE_SRZI },
    { key: "kidFacilityOwnershipTypes" as const, value: "PRIVATE_MON", label: "Частна МОН", color: OWNERSHIP_COLORS.PRIVATE_MON },
  ];

  const renderPaginationFooter = (mobile?: boolean) => {
    const btnSize = mobile ? "py-2.5 text-xs" : "py-2 text-[11px]";
    const spacing = mobile ? "px-3 py-2.5 space-y-2" : "px-3 py-2 space-y-1.5";
    const hideSpacing = mobile ? "px-3 py-2.5" : "px-3 py-2";

    return (
    <div className="shrink-0 bg-[var(--surface)]">
      <div className="px-4 py-1.5 bg-[var(--bg-alt)] border-y border-[var(--border-light)]">
        <span className="text-[11px] text-[var(--text-muted)]">
          {displayedFacilities.length} от {filteredFacilities.length} показани
        </span>
      </div>
      {hasUserLocation && !isLoading && !error && hiddenCount > 0 && (
        <div className={spacing}>
          <div className="flex gap-1.5">
            <button
              onClick={() => { setDisplayCount((p) => Math.max(10, p - 10)); setShowAllRequested(false); }}
              disabled={displayCount <= 10}
              className={`flex-1 rounded-lg ${btnSize} font-medium transition-colors ${
                displayCount <= 10
                  ? "border border-[var(--border)] bg-gray-100 text-gray-400 dark:bg-gray-800 dark:text-gray-500 cursor-not-allowed"
                  : "text-white hover:opacity-90 cursor-pointer"
              }`}
              style={displayCount > 10 ? { background: "var(--red)" } : undefined}
            >
              По-малко
            </button>
            <button
              onClick={() => { setDisplayCount((p) => p + 10); setShowAllRequested(false); }}
              style={{ background: "var(--green)" }}
              className={`flex-1 rounded-lg ${btnSize} font-medium text-white hover:opacity-90 transition-opacity cursor-pointer`}
            >
              Още
            </button>
          </div>
          <button
            onClick={() => setShowAllRequested(true)}
            className={`w-full rounded-lg ${btnSize} font-medium text-[var(--accent)] hover:underline cursor-pointer`}
          >
            Покажи всички ({filteredFacilities.length})
          </button>
        </div>
      )}
      {hasUserLocation && !isLoading && !error && (showAllRequested || displayCount > 10) && hiddenCount <= 0 && (
        <div className={hideSpacing}>
          <button
            onClick={() => { setDisplayCount(10); setShowAllRequested(false); }}
            className={`w-full rounded-lg ${btnSize} font-medium text-[var(--accent)] hover:underline cursor-pointer`}
          >
            Покажи само първите 10
          </button>
        </div>
      )}
    </div>
  );
  };

  const sidebarContent = (
    <div className="flex flex-col h-full">
      {/* Search bar */}
      <div className="px-4 py-3 border-b border-[var(--border)]">
        <div className="flex items-stretch rounded-lg border border-[var(--border)] bg-[var(--bg)] overflow-hidden focus-within:border-[var(--accent)] focus-within:ring-2 focus-within:ring-[var(--accent)]/20 transition-shadow">
          <button
            onClick={handleLocate}
            disabled={locating}
            className="flex items-center justify-center shrink-0 w-9 hover:bg-[var(--bg-alt)] disabled:opacity-50 transition-colors cursor-pointer"
            title="Използвай локация"
          >
            {locating ? (
              <span className="inline-block w-4 h-4 border-2 border-[var(--text-muted)] border-t-transparent rounded-full animate-spin" />
            ) : (
              <LocateFixed className="size-4 text-[var(--accent)]" />
            )}
          </button>
          <input
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSearch()}
            placeholder="Квартал или улица в София..."
            className="flex-1 min-w-0 bg-transparent px-1 py-2 text-sm leading-tight text-[var(--text)] placeholder:text-[var(--text-muted)] focus:outline-none"
          />
          <button
            onClick={handleSearch}
            disabled={!address.trim()}
            className="flex items-center justify-center shrink-0 w-9 bg-[var(--accent)] text-white hover:bg-[var(--accent-hover)] disabled:opacity-40 transition-colors cursor-pointer"
          >
            <Search className="size-4" />
          </button>
        </div>
        {locationError && (
          <p className="mt-2 text-xs text-red-500">{locationError}</p>
        )}
        {locationError && (
          <p className="mt-2 text-xs text-red-500">{locationError}</p>
        )}
      </div>

      {/* Filters */}
      <div className="border-b border-[var(--border)]">
        <button
          onClick={() => setFiltersOpen(!filtersOpen)}
          className="w-full flex items-center justify-between px-4 py-3 text-sm font-semibold text-[var(--text)] hover:bg-[var(--bg-alt)] transition-colors cursor-pointer"
        >
          <span className="flex items-center gap-2">
            <SlidersHorizontal className="size-4 text-[var(--text-muted)]" />
            Филтри
            {hasFilters && (
              <span className="inline-flex items-center justify-center min-w-[18px] h-[18px] rounded-full bg-[var(--accent)] text-[10px] font-bold text-white px-1">
                {filters.kidFacilityTypes.length + filters.kidFacilityOwnershipTypes.length}
              </span>
            )}
          </span>
          {filtersOpen ? <ChevronUp className="size-4 text-[var(--text-muted)]" /> : <ChevronDown className="size-4 text-[var(--text-muted)]" />}
        </button>
        {filtersOpen && (
          <div className="px-4 pb-4 space-y-4">
            {/* Type filter section */}
            <div>
              <h4 className="text-[11px] font-semibold text-[var(--text-muted)] uppercase tracking-wider mb-2">
                Тип заведение
              </h4>
              <div className="space-y-1.5">
                {typeOptions.map((opt) => (
                  <label
                    key={opt.value}
                    className="flex items-center gap-2.5 px-2 py-1.5 -mx-2 rounded-lg hover:bg-[var(--bg-alt)] cursor-pointer transition-colors"
                  >
                    <input
                      type="checkbox"
                      checked={(filters[opt.key] as string[]).includes(opt.value)}
                      onChange={() => handleFilterToggle(opt.key, opt.value)}
                      className="w-4 h-4 rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)] cursor-pointer"
                    />
                    <span className="flex items-center gap-2 text-sm text-[var(--text-secondary)]">
                      <img src={opt.icon} alt="" style={{ width: 20, height: 20 }} />
                      {opt.label}
                    </span>
                  </label>
                ))}
              </div>
            </div>

            {/* Ownership filter section */}
            <div>
              <h4 className="text-[11px] font-semibold text-[var(--text-muted)] uppercase tracking-wider mb-2">
                Вид собственост
              </h4>
              <div className="space-y-1.5">
                {ownershipOptions.map((opt) => (
                  <label
                    key={opt.value}
                    className="flex items-center gap-2.5 px-2 py-1.5 -mx-2 rounded-lg hover:bg-[var(--bg-alt)] cursor-pointer transition-colors"
                  >
                    <input
                      type="checkbox"
                      checked={(filters[opt.key] as string[]).includes(opt.value)}
                      onChange={() => handleFilterToggle(opt.key, opt.value)}
                      className="w-4 h-4 rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)] cursor-pointer"
                    />
                    <span className="flex items-center gap-2 text-sm text-[var(--text-secondary)]">
                      <span className="inline-block w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: opt.color }} />
                      {opt.label}
                    </span>
                  </label>
                ))}
              </div>
            </div>

            {hasSearched && allFacilities.length > 0 && (
              <div>
                <input
                  type="text"
                  value={textFilter}
                  onChange={(e) => { setTextFilter(e.target.value); setDisplayCount(10); }}
                  placeholder="Филтрирай по име или адрес..."
                  className="w-full rounded-lg border border-[var(--border)] bg-[var(--bg)] px-3 py-2 text-xs text-[var(--text)] placeholder:text-[var(--text-muted)] focus:border-[var(--accent)] focus:outline-none focus:ring-1 focus:ring-[var(--accent)]/20"
                />
              </div>
            )}

            {hasFilters && (
              <button
                onClick={handleClearFilters}
                className="flex items-center gap-1.5 text-xs text-[var(--accent)] hover:underline cursor-pointer"
              >
                <X className="size-3" />
                Изчисти всички
              </button>
            )}
          </div>
        )}
      </div>

      {/* Results */}
      <div className="flex-1 flex flex-col min-h-0">
        <div className="flex-1 overflow-y-auto">
          {hasSearched && (
            <div className="px-4 pt-3 pb-1 text-xs text-[var(--text-muted)]">
              Търсене близо до:{" "}
              {hasUserLocation && locationLabel ? (
                <span className="font-medium text-[var(--text-secondary)]">{locationLabel}</span>
              ) : (
                "Център"
              )}
            </div>
          )}
          <FacilityList facilities={displayedFacilities} isLoading={isLoading} error={error} selectedFacilityId={selectedFacilityId} onSelectFacility={handleSelectFacility} hasSearched={hasSearched} />
        </div>
        {renderPaginationFooter()}
      </div>
    </div>
  );

  return (
    <div className="h-[calc(100dvh-3.5rem)] bg-[var(--bg)] overscroll-none">
      <div className="lg:flex h-full overflow-hidden">
        {/* Desktop sidebar */}
        <div className="hidden lg:flex w-[420px] shrink-0 flex-col border-r border-[var(--border)] bg-[var(--surface)]">
          {sidebarContent}
        </div>

        {/* Map container - always mounted */}
        <div className="h-full lg:flex-1 relative" style={{ isolation: "isolate" }}>
          <FacilityMap
            facilities={mapFacilities}
            centerLatitude={centerLat}
            centerLongitude={centerLon}
            locationLabel={hasSearched ? locationLabel : undefined}
            selectedFacilityId={selectedFacilityId}
            onSelectFacility={handleMapSelectFacility}
            onUserLocationChange={handleUserLocationChange}
            onReady={handleMapReady}
            filters={filters}
            onFilterToggle={handleFilterToggle}
          />

          {showSpinner && (
            <div className="absolute inset-0 z-10 flex items-center justify-center bg-[var(--bg)]">
              <span className="inline-block w-8 h-8 border-[3px] border-[var(--border)] border-t-[var(--accent)] rounded-full animate-spin" />
            </div>
          )}

          {/* Mobile-only overlays */}
          <div className="lg:hidden">
            {/* Full-screen list overlay */}
            {listVisible && (
              <div className="absolute inset-0 z-20 flex flex-col bg-[var(--bg)] overflow-hidden" style={{ willChange: "transform" }}>
                <div className="sticky top-0 z-30 flex justify-end bg-[var(--bg)] px-3 pt-3 pb-2 shrink-0">
                  <button
                    onClick={() => setListVisible(false)}
                    className="flex items-center gap-2 rounded-full bg-[var(--surface)] border border-[var(--border)] px-4 py-2 text-sm font-semibold text-[var(--text)] shadow-lg shadow-black/10 hover:shadow-xl transition-all cursor-pointer active:scale-95"
                  >
                    <Map className="size-4" />
                    Карта
                  </button>
                </div>

                <div className="shrink overflow-y-auto overscroll-contain">
                  {(hasSearched) && (
                    <button
                      onClick={() => setFiltersOpen(!filtersOpen)}
                      className="flex items-center justify-between w-full px-4 py-2 bg-[var(--bg-alt)] border-y border-[var(--border-light)] text-sm font-medium text-[var(--text-secondary)] cursor-pointer sticky top-0 z-10"
                    >
                      <span className="flex items-center gap-2">
                        <SlidersHorizontal className="size-3.5" />
                        Търсене и филтри
                        {hasFilters && (
                          <span className="inline-flex items-center justify-center min-w-[18px] h-[18px] rounded-full bg-[var(--accent)] text-[10px] font-bold text-white px-1">
                            {filters.kidFacilityTypes.length + filters.kidFacilityOwnershipTypes.length}
                          </span>
                        )}
                      </span>
                      {filtersOpen ? <ChevronUp className="size-4" /> : <ChevronDown className="size-4" />}
                    </button>
                  )}

                  {filtersOpen && (
                    <>
                      <div className="px-4 pt-3 pb-1.5">
                        <div className="flex items-stretch rounded-lg border border-[var(--border)] bg-[var(--bg)] overflow-hidden focus-within:border-[var(--accent)] focus-within:ring-1 focus-within:ring-[var(--accent)]/20 transition-shadow">
                          <button onClick={handleLocate} disabled={locating} className="flex items-center justify-center shrink-0 w-9 hover:bg-[var(--bg-alt)] disabled:opacity-50 transition-colors cursor-pointer" title="Използвай локация">
                            {locating ? <span className="inline-block w-4 h-4 border-2 border-[var(--text-muted)] border-t-transparent rounded-full animate-spin" /> : <LocateFixed className="size-4 text-[var(--accent)]" />}
                          </button>
                          <input type="text" value={address} onChange={(e) => setAddress(e.target.value)} onKeyDown={(e) => e.key === "Enter" && handleSearch()} placeholder="Квартал или улица..." className="flex-1 min-w-0 bg-transparent px-1 py-2 text-sm leading-tight text-[var(--text)] placeholder:text-[var(--text-muted)] focus:outline-none" />
                          <button onClick={handleSearch} disabled={!address.trim()} className="flex items-center justify-center shrink-0 w-9 bg-[var(--accent)] text-white hover:bg-[var(--accent-hover)] disabled:opacity-40 transition-colors cursor-pointer">
                            <Search className="size-4" />
                          </button>
                        </div>
                        {locationError && <p className="mt-1.5 text-xs text-red-500">{locationError}</p>}
                        {locationLabel && (
                          <p className="mt-1.5 text-xs text-[var(--text-muted)] truncate">Близо до <span className="font-medium text-[var(--text-secondary)]">{locationLabel}</span></p>
                        )}
                      </div>

                      <div className="px-4 pb-2">
                        <div className="flex items-center gap-1.5 overflow-x-auto hide-scrollbar">
                          {typeOptions.map((opt) => (
                            <button key={opt.value} onClick={() => handleFilterToggle(opt.key, opt.value)}
                              className={`flex items-center gap-1 shrink-0 rounded-full px-3 py-1.5 text-xs font-medium border transition-colors cursor-pointer ${
                                (filters[opt.key] as string[]).includes(opt.value)
                                  ? 'bg-[var(--accent)] text-white border-[var(--accent)]'
                                  : 'bg-[var(--bg)] text-[var(--text-secondary)] border-[var(--border)] hover:border-[var(--text-muted)]'
                              }`}>
                              <img src={opt.icon} alt="" style={{ width: 16, height: 16 }} />
                              {opt.label}
                            </button>
                          ))}
                        </div>
                        <div className="py-2">
                          <hr className="border-[var(--border)]" />
                        </div>
                        <div className="flex items-center gap-1.5 overflow-x-auto hide-scrollbar">
                          {ownershipOptions.map((opt) => (
                            <button key={opt.value} onClick={() => handleFilterToggle(opt.key, opt.value)}
                              className={`flex items-center gap-1 shrink-0 rounded-full px-3 py-1.5 text-xs font-medium border transition-colors cursor-pointer ${
                                (filters[opt.key] as string[]).includes(opt.value)
                                  ? 'border-[var(--accent)] text-[var(--accent)] bg-[var(--accent-soft)]'
                                  : 'bg-[var(--bg)] text-[var(--text-secondary)] border-[var(--border)] hover:border-[var(--text-muted)]'
                              }`}>
                              <span className="inline-block w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: opt.color }} />
                              {opt.label}
                            </button>
                          ))}
                        </div>
                        {hasFilters && (
                          <button onClick={handleClearFilters} className="mt-2 text-xs text-[var(--accent)] hover:underline cursor-pointer">
                            Изчисти всички филтри
                          </button>
                        )}
                        {hasSearched && allFacilities.length > 0 && (
                          <div className="mt-2.5">
                            <input type="text" value={textFilter} onChange={(e) => { setTextFilter(e.target.value); setDisplayCount(10); }} placeholder="Филтрирай по име или адрес..." className="w-full rounded-xl border border-[var(--border)] bg-[var(--bg)] px-3 py-2 text-xs text-[var(--text)] placeholder:text-[var(--text-muted)] focus:border-[var(--accent)] focus:outline-none focus:ring-1 focus:ring-[var(--accent)]/20" />
                          </div>
                        )}
                      </div>
                    </>
                  )}
                </div>

                <div className="flex-1 overflow-y-auto overscroll-contain" style={{ WebkitOverflowScrolling: "touch" }}>
                  {hasSearched && (
                    <div className="px-4 pt-3 pb-1 text-xs text-[var(--text-muted)]">
                      Търсене близо до{" "}
                      {hasUserLocation && locationLabel ? (
                        <span className="font-medium text-[var(--text-secondary)]">{locationLabel}</span>
                      ) : (
                        "Център"
                      )}
                    </div>
                  )}
                  <FacilityList facilities={displayedFacilities} isLoading={isLoading} error={error} selectedFacilityId={listVisible ? selectedFacilityId : null} onSelectFacility={handleSelectFacility} hasSearched={hasSearched} />
                </div>

                <div className="shrink-0">
                  {renderPaginationFooter(true)}
                </div>
              </div>
            )}

            {!listVisible && selectedFacility && (
              <FacilityDetailCard facility={selectedFacility} onClose={() => handleSelectFacility(null)} />
            )}

            {!listVisible && hasSearched && !isLoading && !error && filteredFacilities.length > 0 && (
              <button
                onClick={() => setListVisible(true)}
                className="absolute top-[calc(0.75rem+env(safe-area-inset-top,0px))] right-3 z-10 flex items-center gap-2 rounded-full bg-[var(--surface)] border border-[var(--border)] px-4 py-2 text-sm font-semibold text-[var(--text)] shadow-lg shadow-black/10 hover:shadow-xl transition-all cursor-pointer active:scale-95"
              >
                <List className="size-4" />
                Списък
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
