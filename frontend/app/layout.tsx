import type { Metadata, Viewport } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { dehydrate, HydrationBoundary, QueryClient } from "@tanstack/react-query";
import { searchFacilities } from "@/lib/api-client";
import { ReactQueryProvider } from "@/components/react-query-provider";
import { MyThemeProvider } from "@/components/theme-provider";
import Navbar from "@/components/navbar/navbar";

const inter = Inter({
  subsets: ["cyrillic", "latin"],
  variable: "--font-inter",
});

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  viewportFit: "cover",
};

export const metadata: Metadata = {
  title: "Намери Детска — Детски градини и ясли в София",
  description: "Всички общински и частни ясли и градини, одобрени от СРЗИ и МОН, на една карта — подредени по близост до Вас.",
};

export default async function RootLayout({children}: { children: React.ReactNode }) {
  const queryClient = new QueryClient();

  try {
    await queryClient.prefetchQuery({
      queryKey: ["facilities"],
      queryFn: searchFacilities,
      staleTime: 5 * 60 * 1000,
    });
  } catch {}

  return (
    <html lang="bg" suppressHydrationWarning className={inter.variable}>
      <body className={`${inter.className} bg-[var(--bg)] text-[var(--text)]`}>
        <ReactQueryProvider>
          <HydrationBoundary state={dehydrate(queryClient)}>
            <MyThemeProvider>
              <Navbar/>
              {children}
            </MyThemeProvider>
          </HydrationBoundary>
        </ReactQueryProvider>
      </body>
    </html>
  );
}
