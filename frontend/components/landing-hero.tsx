"use client";

import { useState } from "react";
import type { LucideIcon } from "lucide-react";

interface LandingHeroProps {
  onUseMyLocation: () => void;
  onSearchWithAddress: (address: string) => void;
  isLoading: boolean;
  locationError: string | null;
  LocateIcon: LucideIcon;
  MapIcon: LucideIcon;
}

export function LandingHero({
                              onUseMyLocation,
                              onSearchWithAddress,
                              isLoading,
                              locationError,
                              LocateIcon,
                              MapIcon,
                            }: LandingHeroProps) {
  const [address, setAddress] = useState("");

  const handleSearch = () => {
    if (!address.trim()) return;
    onSearchWithAddress(address.trim());
  };

  return (
    <section className="relative min-h-[90vh] flex items-center justify-center overflow-hidden">

      <div className="relative z-10 w-full max-w-xl mx-auto px-4 text-center">
        <h1
          className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight text-slate-900 dark:text-slate-100 leading-tight">
          Намерете най-близката детска ясла или градина в София
        </h1>

        <p className="mt-5 text-base sm:text-lg text-slate-500 dark:text-slate-400 leading-relaxed max-w-lg mx-auto">
          Всички общински заведения и частни ясли и градини, <span
          className="font-semibold text-slate-700 dark:text-slate-300">одобрени от СРЗИ и МОН</span>, на една
          карта — <span
          className="font-semibold text-slate-700 dark:text-slate-300">подредени по близост до Вас</span>.
        </p>

        <div className="mt-10">
          <div
            className="rounded-2xl border border-slate-200/60 dark:border-slate-700/60 bg-white/80 dark:bg-slate-800/80 backdrop-blur-xl shadow-xl shadow-slate-900/5 dark:shadow-black/20 p-5 sm:p-7">
            <button
              onClick={onUseMyLocation}
              disabled={isLoading}
              className="w-full rounded-xl bg-gradient-to-r from-blue-600 to-blue-500 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-500/25 hover:from-blue-700 hover:to-blue-600 disabled:opacity-50 transition-all duration-200 cursor-pointer"
            >
              {isLoading ? (
                <span className="flex items-center justify-center gap-2">
                  <span
                    className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"/>
                  Определяне на местоположение...
                </span>
              ) : (
                <span className="flex items-center justify-center gap-2">
                  <LocateIcon className="size-4"/>
                  Използвай локация
                </span>
              )}
            </button>

            {locationError && (
              <p className="mt-2 text-xs text-red-500 text-center">{locationError}</p>
            )}

            <div className="relative flex items-center gap-3 my-4">
              <div className="flex-1 h-px bg-slate-200 dark:bg-slate-700"/>
              <span className="text-xs font-medium text-slate-400 dark:text-slate-500">или въведи адрес</span>
              <div className="flex-1 h-px bg-slate-200 dark:bg-slate-700"/>
            </div>

            <div className="flex flex-col sm:flex-row gap-2">
              <input
                type="text"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                placeholder="Квартал или улица в София..."
                className="flex-1 rounded-xl border border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-900 px-4 py-3 text-base text-slate-900 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20 transition-all"
              />
              <button
                onClick={handleSearch}
                disabled={!address.trim()}
                className="rounded-xl bg-slate-900 dark:bg-white dark:text-slate-900 px-5 py-3 text-sm font-medium text-white hover:bg-slate-800 dark:hover:bg-slate-100 disabled:opacity-40 transition-all cursor-pointer"
              >
                Търси
              </button>
            </div>
          </div>

          <div className="relative flex items-center gap-3 my-5">
            <div className="flex-1 h-px bg-slate-200/60 dark:bg-slate-700/60"/>
            <span className="text-xs font-medium text-slate-400 dark:text-slate-500">или просто</span>
            <div className="flex-1 h-px bg-slate-200/60 dark:bg-slate-700/60"/>
          </div>

          <a
            href="/map?browse=1"
            className="flex items-center justify-center gap-2 w-full rounded-2xl border border-slate-200/60 dark:border-slate-700/60 bg-white/80 dark:bg-slate-800/80 backdrop-blur-xl shadow-lg shadow-slate-900/5 dark:shadow-black/20 px-5 py-4 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-white dark:hover:bg-slate-800 hover:shadow-xl transition-all cursor-pointer"
          >
            <MapIcon className="size-4"/>
            Разгледай картата с всички ясли и градини
          </a>
        </div>
      </div>
    </section>
  )
    ;
}
