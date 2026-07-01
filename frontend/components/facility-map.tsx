"use client";

import { useEffect, useMemo, useRef } from "react";
import { createRoot } from "react-dom/client";
import { Move } from "lucide-react";
import maplibregl, { type StyleSpecification } from "maplibre-gl";
import type { Facility, SearchFilters } from "@/types/facility";
import { getOwnershipColor } from "@/lib/utils";

interface FacilityMapProps {
  facilities: Facility[];
  centerLatitude: number;
  centerLongitude: number;
  locationLabel?: string;
  selectedFacilityId: string | null;
  onSelectFacility: (facilityId: string | null) => void;
  onUserLocationChange?: (lat: number, lng: number) => void;
  onReady?: () => void;
  filters?: SearchFilters;
  onFilterToggle?: (key: keyof SearchFilters, value: string) => void;
}

const MAP_STYLE: StyleSpecification = {
  version: 8,
  sources: {
    osm: {
      type: "raster",
      tiles: ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      tileSize: 256,
      maxzoom: 18,
      attribution: "&copy; OpenStreetMap contributors",
    },
  },
  layers: [
    {
      id: "osm-tiles",
      type: "raster",
      source: "osm",
      minzoom: 0,
      maxzoom: 19,
    },
  ],
};

const getPinColor = getOwnershipColor;

function PinMarker({color, children, highlighted}: {
  color: string;
  children: React.ReactNode;
  highlighted?: boolean
}) {
  return (
    <div style={{
      display: "flex", flexDirection: "column", alignItems: "center",
      filter: highlighted
        ? `drop-shadow(0 4px 6px rgba(0,0,0,0.35)) drop-shadow(0 0 14px ${color})`
        : "drop-shadow(0 2px 4px rgba(0,0,0,0.25))",
      transform: highlighted ? "scale(1.18)" : undefined,
      transition: "transform 0.18s ease-out, filter 0.18s ease-out",
    }}>
      <div style={{
        width: 44, height: 44, borderRadius: 12,
        background: "white",
        display: "flex", alignItems: "center", justifyContent: "center",
        border: highlighted ? `3.5px solid ${color}` : `2px solid ${color}`,
        boxShadow: highlighted ? "0 2px 8px rgba(0,0,0,0.2)" : undefined,
        transition: "border 0.18s ease-out, box-shadow 0.18s ease-out",
      }}>
        {children}
      </div>
      <div style={{
        width: 0, height: 0,
        borderLeft: "7px solid transparent",
        borderRight: "7px solid transparent",
        borderTop: `9px solid ${color}`,
        marginTop: -1,
      }}/>
    </div>
  );
}

function createPinElement(f: Facility) {
  const el = document.createElement("div");
  el.style.cursor = "pointer";
  el.style.touchAction = "manipulation";
  const color = getPinColor(f.kidFacilityOwnershipType);
  const isNursery = f.kidFacilityType === "NURSERY";
  const iconSrc = isNursery ? "/pacifier-baby.svg" : "/teddy-bear.svg";
  const root = createRoot(el);
  const render = (highlighted: boolean) => {
    root.render(
      <PinMarker color={color} highlighted={highlighted}>
        <img src={iconSrc} alt="" style={{width: 32, height: 32}}/>
      </PinMarker>
    );
  };
  render(false);
  return {el, render};
}

export function FacilityMap({
                              facilities,
                              centerLatitude,
                              centerLongitude,
                              locationLabel,
                              selectedFacilityId,
                              onSelectFacility,
                              onUserLocationChange,
                              onReady,
                              filters,
                              onFilterToggle
                            }: FacilityMapProps) {
  const mapContainer = useRef<HTMLDivElement>(null);
  const mapRef = useRef<maplibregl.Map | null>(null);
  const markersRef = useRef<Map<string, maplibregl.Marker>>(new Map());
  const userMarkerRef = useRef<maplibregl.Marker | null>(null);
  const resizeObserverRef = useRef<ResizeObserver | null>(null);
  const prevCenterRef = useRef<{ lat: number; lng: number } | null>(null);
  const prevUserMarkerKeyRef = useRef<string>("");
  const skipFlyToRef = useRef(false);
  const pinRenderersRef = useRef<Map<string, (highlighted: boolean) => void>>(new Map());
  const prevSelectedRef = useRef<string | null>(null);
  const legendRootRef = useRef<ReturnType<typeof createRoot> | null>(null);
  const renderLegendRef = useRef<() => void>(() => {});

  // Keep renderLegend up to date with latest filters
  renderLegendRef.current = () => {
    const root = legendRootRef.current;
    if (!root) return;
    const activeTypes = filters?.kidFacilityTypes ?? [];
    const activeOwnership = filters?.kidFacilityOwnershipTypes ?? [];
    const toggle = onFilterToggle;
    const hasAnyFilter = activeTypes.length > 0 || activeOwnership.length > 0;
    const isActive = (filterKey: keyof SearchFilters, value: string) => {
      const arr: string[] = filterKey === "kidFacilityTypes" ? activeTypes : activeOwnership;
      return arr.includes(value);
    };
    const chipStyle = (key: keyof SearchFilters, value: string): React.CSSProperties => ({
      display:"flex",alignItems:"center",gap:"4px",cursor:"pointer",padding:"3px 7px",borderRadius:"6px",
      background: isActive(key, value) ? "#eff6ff" : "transparent",
      boxShadow: isActive(key, value) ? "0 0 0 1px #93c5fd" : undefined,
    });
    root.render(
      <>
        <style>{`.legend-chip{transition:background .15s,box-shadow .15s}.legend-chip:hover{background:#f1f5f9!important}.legend-chip:active{background:#e2e8f0!important;transform:scale(.96)}`}</style>
        <div style={{background:"white",border:"1px solid #c4c4c4",borderRadius:"8px",padding:"4px 6px",fontSize:"11px",fontFamily:"system-ui,-apple-system,sans-serif",display:"flex",flexDirection:"column",gap:"4px",boxShadow:"0 1px 4px rgba(0,0,0,0.1)"}}>
          <div style={{display:"flex",gap:"6px",alignItems:"center"}}>
            <span className="legend-chip" onClick={() => toggle?.("kidFacilityTypes", "KINDERGARTEN")} style={chipStyle("kidFacilityTypes", "KINDERGARTEN")}>
              <img src="/teddy-bear.svg" width="14" height="14" />
              <span style={{color:"#475569",fontWeight:500}}>Градина</span>
            </span>
            <span className="legend-chip" onClick={() => toggle?.("kidFacilityTypes", "NURSERY")} style={chipStyle("kidFacilityTypes", "NURSERY")}>
              <img src="/pacifier-baby.svg" width="14" height="14" />
              <span style={{color:"#475569",fontWeight:500}}>Ясла</span>
            </span>
          </div>
          <div style={{display:"flex",gap:"6px",alignItems:"center"}}>
            <span className="legend-chip" onClick={() => toggle?.("kidFacilityOwnershipTypes", "MUNICIPAL")} style={chipStyle("kidFacilityOwnershipTypes", "MUNICIPAL")}>
              <span style={{display:"inline-block",width:"10px",height:"10px",borderRadius:"50%",background:"#2563eb",flexShrink:0}} />
              <span style={{color:"#475569",fontWeight:500}}>Общинска</span>
            </span>
            <span className="legend-chip" onClick={() => toggle?.("kidFacilityOwnershipTypes", "PRIVATE_SRZI")} style={chipStyle("kidFacilityOwnershipTypes", "PRIVATE_SRZI")}>
              <span style={{display:"inline-block",width:"10px",height:"10px",borderRadius:"50%",background:"#059669",flexShrink:0}} />
              <span style={{color:"#475569",fontWeight:500}}>Частна СРЗИ</span>
            </span>
            <span className="legend-chip" onClick={() => toggle?.("kidFacilityOwnershipTypes", "PRIVATE_MON")} style={chipStyle("kidFacilityOwnershipTypes", "PRIVATE_MON")}>
              <span style={{display:"inline-block",width:"10px",height:"10px",borderRadius:"50%",background:"#fc9003",flexShrink:0}} />
              <span style={{color:"#475569",fontWeight:500}}>Частна МОН</span>
            </span>
          </div>
        </div>
      </>
    );
  };
  const facilityIdsKey = useMemo(() => facilities.map((f) => f.id).sort().join(","), [facilities]);

  useEffect(() => {
    if (!mapContainer.current || mapRef.current) return;

    const map = new maplibregl.Map({
      container: mapContainer.current,
      style: MAP_STYLE,
      center: [centerLongitude, centerLatitude],
      zoom: 13,
      maxZoom: 18,
      attributionControl: false,
    });

    // Legend
    const legendEl = document.createElement("div");
    legendEl.className = "maplibregl-ctrl";
    Object.assign(legendEl.style, {marginBottom: "2px", marginRight: "8px"});

    legendRootRef.current = createRoot(legendEl);
    renderLegendRef.current();

    map.addControl({
      onAdd: () => legendEl, onRemove: () => {
        legendEl.remove();
      }
    }, "bottom-right");
    map.addControl({
      onAdd: () => legendEl, onRemove: () => {
        legendEl.remove();
      }
    }, "bottom-right");

    if (window.innerWidth >= 1024) {
      map.addControl(new maplibregl.NavigationControl({showCompass: false, visualizePitch: false}), "bottom-right");
    }

    // Position the bottom-right panel and item gaps via JS
    map.on("idle", () => {
      const container = mapContainer.current;
      if (!container) return;
      const panel = container.querySelector(".maplibregl-ctrl-bottom-right") as HTMLElement;
      if (panel) panel.style.bottom = "10px";
      const controls = container.querySelectorAll(".maplibregl-ctrl-bottom-right > .maplibregl-ctrl");
      controls.forEach((ctrl, i) => {
        const el = ctrl as HTMLElement;
        el.style.marginRight = "8px";
        el.style.marginBottom = i === controls.length - 1 ? "0" : "2px";
      });
    });

    mapRef.current = map;

    map.on("click", () => onSelectFacility(null));

    const readyRef = {fired: false};
    map.on("idle", () => {
      if (!readyRef.fired) {
        readyRef.fired = true;
        onReady?.();
      }
    });

    resizeObserverRef.current = new ResizeObserver(() => {
      map.resize();
    });
    resizeObserverRef.current.observe(mapContainer.current);

    return () => {
      resizeObserverRef.current?.disconnect();
      markersRef.current.forEach((m) => m.remove());
      markersRef.current.clear();
      userMarkerRef.current?.remove();
      userMarkerRef.current = null;
      map.remove();
      mapRef.current = null;
    };
  }, []);

  // Re-render legend when filters change
  useEffect(() => {
    renderLegendRef.current();
  }, [filters]);

  useEffect(() => {
    if (!mapRef.current) return;

    const centerChanged = !prevCenterRef.current
      || Math.abs(prevCenterRef.current.lat - centerLatitude) > 0.001
      || Math.abs(prevCenterRef.current.lng - centerLongitude) > 0.001;
    if (centerChanged) {
      mapRef.current.flyTo({
        center: [centerLongitude, centerLatitude],
        zoom: mapRef.current.getZoom(),
        duration: 1000,
      });
      prevCenterRef.current = {lat: centerLatitude, lng: centerLongitude};
    }

    const existing = markersRef.current;
    const newIds = new Set(facilities.map((f) => f.id));

    for (const [id, marker] of existing) {
      if (!newIds.has(id)) {
        marker.remove();
        existing.delete(id);
        pinRenderersRef.current.delete(id);
      }
    }

    const coordLookup = new Map<string, number>();
    facilities.forEach((f) => {
      if (existing.has(f.id)) return;

      const coordKey = `${f.latitude.toFixed(6)},${f.longitude.toFixed(6)}`;
      const count = coordLookup.get(coordKey) ?? 0;
      coordLookup.set(coordKey, count + 1);
      const jitter = count * 0.00015;
      const lat = f.latitude + jitter;
      const lng = f.longitude + jitter;

      const {el, render} = createPinElement(f);

      const marker = new maplibregl.Marker({element: el, anchor: "bottom"})
        .setLngLat([lng, lat])
        .addTo(mapRef.current!);

      pinRenderersRef.current.set(f.id, render);

      el.addEventListener("click", (e) => {
        e.stopPropagation();
        skipFlyToRef.current = true;
        onSelectFacility(f.id);
      });

      existing.set(f.id, marker);
    });
  }, [facilityIdsKey, centerLatitude, centerLongitude, onSelectFacility]);

  useEffect(() => {
    if (!mapRef.current) return;

    const userMarkerKey = `${centerLatitude.toFixed(6)},${centerLongitude.toFixed(6)}|${locationLabel ?? ""}`;
    const keyChanged = userMarkerKey !== prevUserMarkerKeyRef.current;
    prevUserMarkerKeyRef.current = userMarkerKey;

    if (!keyChanged) return;

    userMarkerRef.current?.remove();
    userMarkerRef.current = null;
    if (locationLabel) {
      const userEl = document.createElement("div");
      userEl.style.cursor = "grab";
      const inner = document.createElement("div");
      userEl.appendChild(inner);
      createRoot(inner).render(
        <div style={{position: "relative"}}>
          <PinMarker color="#dc2626">
            <img src="/home.svg" alt="" style={{width: 28, height: 28}}/>
          </PinMarker>
          <div style={{
            position: "absolute",
            top: -2, left: -4,
            width: 18, height: 18, borderRadius: "50%",
            background: "#dc2626",
            display: "flex", alignItems: "center", justifyContent: "center",
            boxShadow: "0 1px 3px rgba(0,0,0,0.3)",
          }}>
            <Move size={10} color="white"/>
          </div>
        </div>
      );
      const userMarker = new maplibregl.Marker({element: userEl, anchor: "bottom", draggable: true})
        .setLngLat([centerLongitude, centerLatitude])
        .addTo(mapRef.current);
      userMarker.on("dragend", () => {
        const lngLat = userMarker.getLngLat();
        onUserLocationChange?.(lngLat.lat, lngLat.lng);
      });
      userMarkerRef.current = userMarker;
    }
  }, [centerLatitude, centerLongitude, locationLabel, onUserLocationChange]);

  useEffect(() => {
    const prevId = prevSelectedRef.current;
    if (prevId && prevId !== selectedFacilityId) {
      pinRenderersRef.current.get(prevId)?.(false);
    }
    prevSelectedRef.current = selectedFacilityId;

    if (selectedFacilityId) {
      pinRenderersRef.current.get(selectedFacilityId)?.(true);
      const marker = markersRef.current.get(selectedFacilityId);
      if (marker) {
        const el = marker.getElement();
        el.parentNode?.appendChild(el);
      }
    }

    if (!selectedFacilityId) return;

    if (skipFlyToRef.current) {
      skipFlyToRef.current = false;
      return;
    }
    const facility = facilities.find((f) => f.id === selectedFacilityId);
    if (!facility) return;
    const currentZoom = mapRef.current?.getZoom() ?? 15;
    mapRef.current?.flyTo({center: [facility.longitude, facility.latitude], zoom: currentZoom, duration: 800});
  }, [selectedFacilityId, facilities]);

  return (
    <div ref={mapContainer} className="w-full h-full map-container"/>
  );
}
