import { Link, NavLink } from "react-router-dom";
import "./editorial.css";

export function PublicHeader() {
  return (
    <header className="editorial-header">
      <Link className="editorial-brand" to="/events">
        <span className="editorial-mark" aria-hidden="true">
          E
        </span>{" "}
        EconNews
      </Link>
      <nav className="editorial-nav" aria-label="Điều hướng công khai">
        <NavLink to="/events">Sự kiện</NavLink>
        <NavLink to="/articles">Bài viết</NavLink>
        <Link className="editorial-admin" to="/admin">
          Quản trị
        </Link>
      </nav>
    </header>
  );
}
