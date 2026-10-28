export interface NavLink {
  href: string;
  label: string;
  external?: boolean;
}

export const NAV_LINKS: NavLink[] = [
  {href: "/map", label: "Карта"},
  {href: "/about", label: "За проекта"},
];
