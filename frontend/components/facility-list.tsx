"use client";

import type { Facility } from "@/types/facility";
import { formatMinutes, getOwnershipColor } from "@/lib/utils";
import { useEffect, useRef } from "react";

interface FacilityListProps {
  facilities: Facility[];
  isLoading: boolean;
  error: Error | null;
  selectedFacilityId: string | null;
  onSelectFacility: (facilityId: string | null) => void;
  hasSearched?: boolean;
}

export function FacilityList({ facilities, isLoading, error, selectedFacilityId, onSelectFacility, hasSearched }: FacilityListProps) {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <span className="inline-block w-5 h-5 border-2 border-[var(--border)] border-t-[var(--accent)] rounded-full animate-spin" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="px-4 py-8 text-center text-sm text-red-500">
        <p>Заведенията не могат да бъдат заредени.</p>
        <p className="text-xs text-[var(--text-muted)] mt-1">Работи ли сървърът?</p>
      </div>
    );
  }

  if (facilities.length === 0) {
    if (!hasSearched) {
      return null;
    }
    return (
      <div className="px-4 py-12 text-center">
        <svg className="mx-auto mb-3 text-[var(--text-muted)]" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <p className="text-sm text-[var(--text-muted)]">Няма намерени заведения.</p>
        <p className="text-xs text-[var(--text-muted)] mt-0.5">Опитайте с други филтри.</p>
      </div>
    );
  }

  return (
    <div>
      {facilities.map((f, i) => (
        <FacilityListItem
          key={f.id}
          facility={f}
          index={i}
          isSelected={f.id === selectedFacilityId}
          onSelect={onSelectFacility}
          isLast={i === facilities.length - 1}
        />
      ))}
    </div>
  );
}

function FacilityListItem({ facility, index, isSelected, onSelect, isLast }: { facility: Facility; index: number; isSelected: boolean; onSelect: (facilityId: string | null) => void; isLast?: boolean }) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isSelected && ref.current) {
      ref.current.scrollIntoView({ behavior: "smooth", block: "nearest" });
    }
  }, [isSelected]);

  return (
    <div
      ref={ref}
      onClick={() => onSelect(facility.id)}
      className={`px-4 py-3 transition-colors cursor-pointer animate-in ${
        isSelected
          ? "bg-[var(--accent)]/10 ring-1 ring-[var(--accent)]/30"
          : "hover:bg-[var(--bg-alt)]"
      } ${!isLast ? "border-b border-[var(--border)]" : ""}`}
      style={{ animationDelay: `${index * 40}ms` }}
    >
      <div className="flex items-start gap-3">
        <div className="min-w-0 flex-1">
          <h3 className="text-sm font-semibold text-[var(--text)] leading-snug">
            <span className="inline-flex items-center gap-1.5">
              <span className="inline-flex items-center justify-center w-8 h-8 rounded-md shrink-0" style={{ borderWidth: "1.5px", borderStyle: "solid", borderColor: getOwnershipColor(facility.kidFacilityOwnershipType) }}>
                <img src={facility.kidFacilityType === "NURSERY" ? "/pacifier-baby.svg" : "/teddy-bear.svg"} alt="" style={{ width: 18, height: 18 }} />
              </span>
              {facility.name}
            </span>
          </h3>
          <p className="text-xs text-[var(--text-muted)] truncate mt-0.5">{facility.address}</p>
        </div>
        <div className="shrink-0 text-right">
          {facility.distanceMeters > 0 && (
            <p className="text-xs font-semibold text-[var(--text-secondary)]">{(facility.distanceMeters / 1000).toFixed(1)} км</p>
          )}
        </div>
      </div>

      <div className="mt-2 flex gap-3 text-xs">
        {facility.walkingTimeSeconds > 0 && (
          <span className="inline-flex items-center gap-1 text-emerald-600 dark:text-emerald-400">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="5" r="2" /><path d="M10 22v-5l-2-3 2-3" /><path d="M14 22v-4l2-4-2-3" /><path d="M8 13h4l2 4" /></svg>
            {formatMinutes(facility.walkingTimeSeconds)}
          </span>
        )}
        {facility.transitTimeSeconds != null && facility.transitTimeSeconds > 0 && (
          <span className="inline-flex items-center gap-1 text-blue-600 dark:text-blue-400">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="5" width="20" height="13" rx="2" /><line x1="6" y1="18" x2="6" y2="20" /><line x1="18" y1="18" x2="18" y2="20" /><line x1="7" y1="9" x2="7" y2="13" /><line x1="17" y1="9" x2="17" y2="13" /></svg>
            {formatMinutes(facility.transitTimeSeconds)}
          </span>
        )}
      </div>
    </div>
  );
}
