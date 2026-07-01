"use client";

import type { Facility } from "@/types/facility";
import { formatMinutes, getOwnershipColor } from "@/lib/utils";
import { X } from "lucide-react";

interface FacilityDetailCardProps {
  facility: Facility;
  onClose: () => void;
}

export function FacilityDetailCard({ facility, onClose }: FacilityDetailCardProps) {

  return (
    <div className="absolute bottom-[calc(0.75rem+env(safe-area-inset-bottom,0px))] left-3 right-3 z-30 animate-slide-up" style={{ willChange: "transform" }}>
      <div className="rounded-2xl bg-[var(--surface)] border border-[var(--border)] shadow-xl overflow-hidden">
        <div className="p-4">
          <div className="flex items-start gap-3">
            <div className="min-w-0 flex-1">
              <h3 className="text-sm font-semibold text-[var(--text)] leading-snug pr-6">
                <span className="inline-flex items-center gap-1.5">
                  <span className="inline-flex items-center justify-center w-8 h-8 rounded-md shrink-0" style={{ borderWidth: "1.5px", borderStyle: "solid", borderColor: getOwnershipColor(facility.kidFacilityOwnershipType) }}>
                    <img src={facility.kidFacilityType === "NURSERY" ? "/pacifier-baby.svg" : "/teddy-bear.svg"} alt="" style={{ width: 18, height: 18 }} />
                  </span>
                  {facility.name}
                </span>
              </h3>
              <p className="text-xs text-[var(--text-muted)] mt-0.5">
                {facility.address}
              </p>
            </div>
            <button
              onClick={(e) => { e.stopPropagation(); onClose(); }}
              className="absolute top-3 right-3 flex items-center justify-center w-7 h-7 rounded-full bg-[var(--bg-alt)] hover:bg-[var(--border)] text-[var(--text-muted)] transition-colors cursor-pointer"
            >
              <X className="size-3.5" />
            </button>
          </div>

          <div className="flex items-center gap-4 mt-3">
            {facility.distanceMeters > 0 && (
              <span className="text-xs font-semibold text-[var(--text-secondary)]">
                {(facility.distanceMeters / 1000).toFixed(1)} км
              </span>
            )}
            {facility.walkingTimeSeconds > 0 && (
              <span className="inline-flex items-center gap-1 text-xs text-emerald-600 dark:text-emerald-400">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="5" r="2"/><path d="M10 22v-5l-2-3 2-3"/><path d="M14 22v-4l2-4-2-3"/><path d="M8 13h4l2 4"/></svg>
                {formatMinutes(facility.walkingTimeSeconds)}
              </span>
            )}
            {facility.transitTimeSeconds != null && facility.transitTimeSeconds > 0 && (
              <span className="inline-flex items-center gap-1 text-xs text-blue-600 dark:text-blue-400">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="5" width="20" height="13" rx="2"/><line x1="6" y1="18" x2="6" y2="20"/><line x1="18" y1="18" x2="18" y2="20"/><line x1="7" y1="9" x2="7" y2="13"/><line x1="17" y1="9" x2="17" y2="13"/></svg>
                {formatMinutes(facility.transitTimeSeconds)}
              </span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
