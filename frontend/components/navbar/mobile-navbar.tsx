"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import { Menu, X } from "lucide-react";
import { NAV_LINKS } from "./nav-links";
import { ThemeSwitcher } from "@/components/theme-switcher";
import { KofiWidget } from "@/components/kofi-widget";

export default function MobileNavbar() {
  const [isOpen, setIsOpen] = useState(false);
  const pathname = usePathname();
  const navRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    setIsOpen(false);
  }, [pathname]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (navRef.current && !navRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setIsOpen(false);
    };

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      document.addEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "hidden";
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "unset";
    };
  }, [isOpen]);

  return (
    <nav ref={navRef} className="relative z-50 border-b border-[var(--border)] bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl">
      <div className="flex items-center justify-between px-4 h-14">
        <Link href="/" className="flex items-center gap-2 shrink-0">
          <Image src="/logo.svg" alt="Намери Детска" width={28} height={28} className="h-7 w-auto" priority />
          <span className="text-sm font-extrabold tracking-tight text-slate-900 dark:text-white">Намери Детска</span>
        </Link>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setIsOpen(!isOpen)}
            className="flex size-10 items-center justify-center rounded-xl border border-[var(--border)] bg-white/80 dark:bg-slate-800/80 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:shadow-sm active:scale-95 transition-all duration-200 cursor-pointer"
            aria-label="Меню"
            aria-expanded={isOpen}
          >
            {isOpen ? (
              <X className="size-[18px]" />
            ) : (
              <Menu className="size-[18px]" />
            )}
          </button>
          <ThemeSwitcher />
          <a
            href="https://github.com/nameri-detska"
            target="_blank"
            rel="noopener noreferrer"
            className="flex size-10 items-center justify-center rounded-xl border border-[var(--border)] bg-white/80 dark:bg-slate-800/80 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:shadow-sm active:scale-95 transition-all duration-200"
            aria-label="GitHub"
          >
            <svg
              width="calc(1rem * 1.7)"
              height="calc(1rem * 1.7)"
              viewBox="0 0 100 100"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                fillRule="evenodd"
                clipRule="evenodd"
                d="M48.854 0C21.839 0 0 22 0 49.217c0 21.756 13.993 40.172 33.405 46.69 2.427.49 3.316-1.059 3.316-2.362 0-1.141-.08-5.052-.08-9.127-13.59 2.934-16.42-5.867-16.42-5.867-2.184-5.704-5.42-7.17-5.42-7.17-4.448-3.015.324-3.015.324-3.015 4.934.326 7.523 5.052 7.523 5.052 4.367 7.496 11.404 5.378 14.235 4.074.404-3.178 1.699-5.378 3.074-6.6-10.839-1.141-22.243-5.378-22.243-24.283 0-5.378 1.94-9.778 5.014-13.2-.485-1.222-2.184-6.275.486-13.038 0 0 4.125-1.304 13.426 5.052a46.97 46.97 0 0 1 12.214-1.63c4.125 0 8.33.571 12.213 1.63 9.302-6.356 13.427-5.052 13.427-5.052 2.67 6.763.97 11.816.485 13.038 3.155 3.422 5.015 7.822 5.015 13.2 0 18.905-11.404 23.06-22.324 24.283 1.78 1.548 3.316 4.481 3.316 9.126 0 6.6-.08 11.897-.08 13.526 0 1.304.89 2.853 3.316 2.364 19.412-6.52 33.405-24.935 33.405-46.691C97.707 22 75.788 0 48.854 0z"
                className="fill-slate-600 dark:fill-slate-400"
              />
            </svg>
          </a>
          <KofiWidget />
        </div>
      </div>

      {/* Mobile menu dropdown */}
      <div
        className={`absolute left-0 top-full w-full border-b border-[var(--border)] bg-white dark:bg-slate-900 shadow-xl transition-all duration-200 ease-out ${
          isOpen ? "opacity-100 translate-y-0 visible" : "opacity-0 -translate-y-2 invisible"
        }`}
      >
        <div className="flex flex-col gap-1 p-4">
          {NAV_LINKS.map(({ href, label, external }) => {
            const isActive = !external && (pathname === href || (href !== "/" && pathname.startsWith(href)));
            const className = `rounded-xl px-4 py-3 text-sm font-semibold transition-colors ${
              isActive
                ? "bg-slate-100 text-slate-900 dark:bg-slate-800 dark:text-white"
                : "text-slate-600 hover:bg-slate-50 dark:text-slate-400 dark:hover:bg-slate-800"
            }`;
            if (external) {
              return (
                <a key={label} href={href} target="_blank" rel="noopener noreferrer" onClick={() => setIsOpen(false)} className={className}>
                  {label}
                </a>
              );
            }
            return (
              <Link key={label} href={href} onClick={() => setIsOpen(false)} className={className}>
                {label}
              </Link>
            );
          })}
        </div>
      </div>
    </nav>
  );
}
