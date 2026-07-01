export interface NavLink {
  href: string;
  label: string;
  external?: boolean;
}

export const NAV_LINKS: NavLink[] = [
  {href: "/about", label: "За проекта"},
];
