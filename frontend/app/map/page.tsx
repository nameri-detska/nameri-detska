import { Suspense } from "react";
import { KidFacilitiesMap } from "@/components/kid-facilities-map";

export default function KidFacilitiesMapPage() {
  return (
    <Suspense fallback={
      <div className="h-[calc(100dvh-3.5rem)] bg-[var(--bg)] lg:flex">
        <div className="hidden lg:flex w-[420px] shrink-0 items-center justify-center border-r border-[var(--border)] bg-[var(--surface)]">
          <span className="inline-block w-8 h-8 border-[3px] border-[var(--border)] border-t-[var(--accent)] rounded-full animate-spin" />
        </div>
        <div className="flex-1 flex items-center justify-center">
          <span className="inline-block w-8 h-8 border-[3px] border-[var(--border)] border-t-[var(--accent)] rounded-full animate-spin" />
        </div>
      </div>
    }>
      <KidFacilitiesMap />
    </Suspense>
  );
}
