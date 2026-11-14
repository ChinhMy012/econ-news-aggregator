const paths = {
  dashboard: "M3 3h7v7H3z M14 3h7v7h-7z M3 14h7v7H3z M14 14h7v7h-7z",
  source: "M4 11a9 9 0 0 1 9 9 M4 4a16 16 0 0 1 16 16 M4 19h1v1H4z",
  article: "M5 3h14v18H5z M8 8h8 M8 12h8 M8 16h5",
  event: "M3 17l5-5 4 3 8-9 M15 6h5v5 M3 21h18",
  document: "M6 3h8l4 4v14H6z M14 3v5h4 M9 12h6 M9 16h6",
  arrow: "M4 12h15 M13 6l6 6-6 6",
  external: "M13 5h6v6 M19 5l-9 9 M10 5H5v14h14v-5",
  logout: "M10 4H5v16h5 M9 12h12 M17 8l4 4-4 4",
};

export function WorkspaceIcon({ name, size = 19 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={paths[name]} />
    </svg>
  );
}
