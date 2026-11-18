import { useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import { WorkspaceIcon } from "./WorkspaceIcon";
import "./workspace.css";

const sections = [
  { href: "/admin", label: "Tổng quan", icon: "dashboard" },
  { href: "/admin/sources", label: "Nguồn tin & RSS", icon: "source" },
  { href: "/admin/articles", label: "Bài viết", icon: "article" },
  { href: "/admin/events", label: "Sự kiện", icon: "event" },
  { href: "/admin/documents", label: "Tài liệu", icon: "document" },
];

export function AdminLayout() {
  const auth = useAuth();
  const { pathname } = useLocation();
  const [leaving, setLeaving] = useState(false);
  const section = sections.find(
    ({ href }) =>
      pathname === href ||
      (pathname.startsWith(href + "/") && href !== "/admin"),
  );
  const username = auth.user.username;

  async function logout() {
    setLeaving(true);
    try {
      await auth.logout();
    } catch {
      // Phiên đăng nhập hiển thị thông báo khi đăng xuất thất bại.
    } finally {
      setLeaving(false);
    }
  }

  return (
    <div className="workspace">
      <aside className="workspace-sidebar">
        <Link to="/admin" className="workspace-brand">
          <span className="workspace-logo">
            <WorkspaceIcon name="event" size={21} />
          </span>
          <span>
            EconNews<small>KHÔNG GIAN BIÊN TẬP</small>
          </span>
        </Link>
        <p className="workspace-nav-label">QUẢN LÝ NỘI DUNG</p>
        <nav aria-label="Điều hướng quản trị" className="workspace-nav">
          {sections.map(({ href, label, icon }) => (
            <NavLink key={href} to={href} end={href === "/admin"}>
              <WorkspaceIcon name={icon} /> <span>{label}</span>
            </NavLink>
          ))}
        </nav>
        <Link to="/events" className="workspace-public-link">
          <WorkspaceIcon name="external" /> Trang công khai
        </Link>
      </aside>
      <div className="workspace-body">
        <header className="workspace-topbar">
          <div className="workspace-breadcrumb">
            <Link to="/admin">Quản trị</Link>
            <span aria-hidden="true">/</span>
            {section && <Link to={section.href}>{section.label}</Link>}
          </div>
          <div className="workspace-account">
            <span className="workspace-avatar" aria-hidden="true">
              {username.slice(0, 1).toUpperCase()}
            </span>
            <span className="workspace-account-name">
              {username}
              <small>Quản trị viên</small>
            </span>
            <button
              type="button"
              onClick={logout}
              disabled={leaving}
              aria-label="Đăng xuất"
              title="Đăng xuất"
            >
              <WorkspaceIcon name="logout" />
            </button>
          </div>
        </header>
        <Outlet />
      </div>
    </div>
  );
}
