"use client";

import DesktopNavbar from "./desktop-navbar";
import MobileNavbar from "./mobile-navbar";

export default function Navbar() {
  return (
    <header>
      <div className="desktop-navbar">
        <DesktopNavbar />
      </div>
      <div className="mobile-navbar">
        <MobileNavbar />
      </div>
    </header>
  );
}
