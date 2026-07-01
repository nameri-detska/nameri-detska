import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatMinutes(seconds: number): string {
  const mins = Math.round(seconds / 60);
  if (mins < 1) return "<1 мин";
  if (mins === 1) return "1 мин";
  return `${mins} мин`;
}

export const OWNERSHIP_COLORS: Record<string, string> = {
  MUNICIPAL: "#2563eb",
  PRIVATE_SRZI: "#059669",
  PRIVATE_MON: "#fc9003",
};

export function getOwnershipColor(ownershipType: string): string {
  return OWNERSHIP_COLORS[ownershipType] ?? "#fc9003";
}
