import { useEffect, useState } from "react";
import { api } from "../api";
import {
  FolderKanban,
  Clock3,
  CircleCheck,
  AlertTriangle,
  ArrowUpRight,
  Zap,
  TrendingUp,
  Bell,
  Sun,
  CalendarDays,
  Target,
  FolderOpen,
  Users,
} from "lucide-react";

function StatCard({ icon: Icon, iconClass, title, value, footer, footerClass, footerIcon: FooterIcon }) {
  return (
    <div className="dashboard-stat-card">
      <div className="stat-card-top">
        <div className={`stat-icon ${iconClass}`}><Icon size={20} strokeWidth={2} /></div>
        <span className="stat-title">{title}</span>
        <div className={`stat-mini-icon ${iconClass}`}><Icon size={16} strokeWidth={2} /></div>
      </div>
      <div className="stat-value">{value}</div>
      <div className={`stat-footer ${footerClass}`}>
        {FooterIcon && <FooterIcon size={15} strokeWidth={2} />}
        <span>{footer}</span>
      </div>
    </div>
  );
}

export default function Dashboard({ username }) {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);

  useEffect(() => { loadDashboard(); }, []);

  async function loadDashboard() {
    try {
      setLoading(true);
      const data = await api.get("/dashboard/overview");
      setStats(data);
    } catch (err) {
      setError(err.message);
    }
    try { setTasks((await api.get("/tasks")) || []); } catch {}
    try { setProjects((await api.get("/projects")) || []); } catch {}
    setLoading(false);
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return "No date";
    return new Date(dateStr).toLocaleDateString("en-US", { month: "short", day: "numeric" });
  };

  const greeting = new Date().getHours() < 12 ? "Good morning" : new Date().getHours() < 18 ? "Good afternoon" : "Good evening";
  const activeTasks = tasks.filter((t) => t.status !== "COMPLETED").slice(0, 5);
  const health = stats?.companyHealthScore ?? 0;

  if (loading) {
    return <div className="dashboard-loading"><div className="dashboard-spinner" /></div>;
  }

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>{greeting}, {username || "User"}!</h1>
          <p>Here's what needs your attention today.</p>
        </div>
        <div className="dashboard-header-actions">
          <button className="header-icon-button" aria-label="Notifications"><Bell size={19} /><span className="notification-dot" /></button>
          <button className="header-icon-button" aria-label="Theme"><Sun size={19} /></button>
          <div className="header-profile">{(username || "U")[0].toUpperCase()}</div>
        </div>
      </header>

      {error && <div className="dashboard-error">{error}</div>}

      <section className="dashboard-stats-grid">
        <StatCard icon={Users} iconClass="stat-blue" title="Active Projects" value={stats?.activeProjects || 0} footer="All teams active" footerClass="footer-green" footerIcon={ArrowUpRight} />
        <StatCard icon={Clock3} iconClass="stat-orange" title="Pending Tasks" value={stats?.pendingTasks || 0} footer={`${stats?.overdueTasks || 0} overdue`} footerClass="footer-red" footerIcon={AlertTriangle} />
        <StatCard icon={CircleCheck} iconClass="stat-green" title="Completed" value={stats?.completedTasks || 0} footer={`Of ${stats?.totalTasks || 0} total`} footerClass="footer-green" footerIcon={TrendingUp} />
        <StatCard icon={Zap} iconClass="stat-purple" title="Health Score" value={health} footer="Out of 100" footerClass="footer-muted" />
      </section>

      <section className="dashboard-content-grid">
        <div className="dashboard-main-column">
          <div className="dashboard-panel focus-panel">
            <div className="panel-heading">
              <div className="panel-title"><span className="panel-title-icon"><Target size={19} /></span><h2>Today's Focus</h2></div>
              <button className="panel-link"><CalendarDays size={16} /> View Calendar</button>
            </div>
            {activeTasks.length === 0 ? (
              <div className="dashboard-empty">
                <div className="empty-illustration target-empty"><Target size={43} /></div>
                <strong>No active tasks. All clear!</strong>
                <span>Keep up the great work!</span>
              </div>
            ) : (
              <div className="task-list">
                {activeTasks.map((task) => (
                  <div className="dashboard-task" key={task.taskId}>
                    <span className={`task-dot ${task.overdue ? "overdue" : ""}`} />
                    <div className="task-info"><strong>{task.title}</strong><span>{task.projectName || "No project"} · Due {formatDate(task.dueDate)}</span></div>
                    <span className="task-priority">{task.priority}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="dashboard-panel projects-panel">
            <div className="panel-heading">
              <div className="panel-title"><span className="panel-title-icon"><FolderOpen size={19} /></span><h2>Projects Overview</h2></div>
              <button className="panel-link">View All Projects <ArrowUpRight size={16} /></button>
            </div>
            {projects.length === 0 ? (
              <div className="dashboard-empty projects-empty">
                <div className="empty-illustration folder-empty"><FolderOpen size={42} /></div>
                <strong>No projects yet. Create one to get started!</strong>
              </div>
            ) : (
              <div className="project-grid">
                {projects.map((project) => (
                  <div className="dashboard-project-card" key={project.projectId}>
                    <div className="project-card-row"><strong>{project.projectName}</strong><span>{project.status?.replace("_", " ")}</span></div>
                    <div className="project-progress"><span style={{ width: `${project.progress || 0}%` }} /></div>
                    <div className="project-meta"><span>{project.completedTasks || 0}/{project.totalTasks || 0} tasks</span><span>{project.progress || 0}%</span></div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <aside className="mate-insight-card">
          <div className="mate-card-label"><Zap size={18} /> MATE INTELLIGENCE</div>
          <h2>Smart Insights</h2>
          <p>{stats?.criticalAlerts?.length ? stats.criticalAlerts[0] : "Operations running smoothly. No blockers detected."}</p>
          <button>View Recommendation <ArrowUpRight size={17} /></button>
        </aside>
      </section>
    </div>
  );
}
