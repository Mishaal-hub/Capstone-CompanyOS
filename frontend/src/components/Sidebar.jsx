import { NavLink, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  Brain,
  CheckSquare,
  FolderKanban,
  Target,
  Users,
  TrendingUp,
  Calendar,
  Activity,
  Settings,
  LogOut,
  Menu,
  X,
} from "lucide-react";
import { useState } from "react";
import companyLogo from "../assets/companyos-logo.png";

const navItems = [
  { section: "MAIN", items: [
    { to: "/", icon: LayoutDashboard, label: "Dashboard" },
    { to: "/mate", icon: Brain, label: "MATE" },
    { to: "/tasks", icon: CheckSquare, label: "Tasks" },
    { to: "/projects", icon: FolderKanban, label: "Projects" },
    { to: "/goals", icon: Target, label: "Goals" },
    { to: "/team", icon: Users, label: "Team" },
    { to: "/growth", icon: TrendingUp, label: "Growth" },
  ]},
  { section: "WORKSPACE", items: [
    { to: "/calendar", icon: Calendar, label: "Calendar" },
    { to: "/activity", icon: Activity, label: "Activity" },
  ]},
  { section: "SYSTEM", items: [
    { to: "/settings", icon: Settings, label: "Settings" },
  ]},
];

export default function Sidebar({ username, onLogout }) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("companyos_token");
    localStorage.removeItem("companyos_current_user");
    localStorage.removeItem("companyos_user");
    onLogout();
    navigate("/");
  };

  return (
    <>
      <button
        className="sidebar-mobile-toggle"
        onClick={() => setMobileOpen(!mobileOpen)}
        aria-label="Toggle navigation"
      >
        {mobileOpen ? <X size={21} /> : <Menu size={21} />}
      </button>

      {mobileOpen && <div className="sidebar-overlay" onClick={() => setMobileOpen(false)} />}

      <aside className={`app-sidebar ${mobileOpen ? "is-open" : ""}`}>
        <div className="sidebar-brand">
          <img src={companyLogo} alt="CompanyOS with MATE Intelligence" className="sidebar-logo" />
        </div>

        <nav className="sidebar-nav">
          {navItems.map((group) => (
            <div className="sidebar-section" key={group.section}>
              <p className="sidebar-section-title">{group.section}</p>
              {group.items.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === "/"}
                  onClick={() => setMobileOpen(false)}
                  className={({ isActive }) => `sidebar-link ${isActive ? "active" : ""}`}
                >
                  <item.icon size={19} strokeWidth={1.9} />
                  <span>{item.label}</span>
                </NavLink>
              ))}
            </div>
          ))}
        </nav>

        <div className="sidebar-user-area">
          <div className="sidebar-user">
            <div className="sidebar-avatar">{(username || "U")[0].toUpperCase()}</div>
            <span title={username || "User"}>{username || "User"}</span>
          </div>
          <button className="sidebar-logout" onClick={handleLogout}>
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </aside>
    </>
  );
}
