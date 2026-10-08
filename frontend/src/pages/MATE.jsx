import { useEffect, useState } from "react";
import { api } from "../api";
import {
  Brain,
  Zap,
  AlertTriangle,
  CheckCircle,
  Clock,
  TrendingUp,
  Target,
  ArrowRight,
  Loader2,
} from "lucide-react";

export default function MATE({ username }) {
  const [stats, setStats] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    try {
      const data = await api.get("/dashboard/overview");
      setStats(data);
    } catch {}
    try {
      const t = await api.get("/tasks");
      setTasks(t || []);
    } catch {}
    try {
      const p = await api.get("/projects");
      setProjects(p || []);
    } catch {}
    setLoading(false);
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="animate-spin text-blue-600" size={32} />
      </div>
    );
  }

  const overdueTasks = tasks.filter((t) => t.overdue && t.status !== "COMPLETED");
  const blockedTasks = tasks.filter((t) => t.status === "BLOCKED");
  const highPriority = tasks.filter((t) => (t.priority === "HIGH" || t.priority === "CRITICAL") && t.status !== "COMPLETED");
  const incompleteProjects = projects.filter((p) => p.status !== "COMPLETED" && p.progress < 100);

  const recommendations = [];
  if (overdueTasks.length > 0) {
    recommendations.push({
      icon: AlertTriangle,
      color: "red",
      title: "Overdue Tasks",
      text: `${overdueTasks.length} task(s) are overdue. Addressing these first will unblock dependent work and improve your health score.`
    });
  }
  if (blockedTasks.length > 0) {
    recommendations.push({
      icon: AlertTriangle,
      color: "orange",
      title: "Blocked Tasks",
      text: `${blockedTasks.length} task(s) are blocked by dependencies. Check prerequisites and clear blockers to restore flow.`
    });
  }
  if (highPriority.length > 0) {
    recommendations.push({
      icon: Target,
      color: "blue",
      title: "High Priority Tasks",
      text: `${highPriority.length} high-priority task(s) need attention. Completing these first will maximize impact.`
    });
  }
  if (incompleteProjects.length > 0) {
    const slowest = incompleteProjects.sort((a, b) => (a.progress || 0) - (b.progress || 0))[0];
    recommendations.push({
      icon: TrendingUp,
      color: "purple",
      title: "Project Insight",
      text: `"${slowest?.projectName}" is at ${slowest?.progress || 0}% progress. Focus here to accelerate delivery.`
    });
  }
  if (recommendations.length === 0) {
    recommendations.push({
      icon: CheckCircle,
      color: "green",
      title: "All Systems Green",
      text: "Everything is on track! Keep up the great work and maintain momentum."
    });
  }

  const priorityColors = { red: "bg-red-50 border-red-200", orange: "bg-orange-50 border-orange-200", blue: "bg-blue-50 border-blue-200", purple: "bg-purple-50 border-purple-200", green: "bg-green-50 border-green-200" };
  const iconColors = { red: "text-red-500", orange: "text-orange-500", blue: "text-blue-500", purple: "text-purple-500", green: "text-green-500" };

  return (
    <div className="space-y-6">
      <div>
        <div className="flex items-center gap-2 mb-2">
          <Brain className="text-blue-600" size={24} />
          <h1 className="text-2xl font-bold text-gray-900">MATE Intelligence</h1>
        </div>
        <p className="text-sm text-gray-500">Your AI-powered business companion. MATE analyzes your data to provide actionable insights.</p>
      </div>

      <div className="bg-gradient-to-br from-blue-600 via-indigo-600 to-purple-700 rounded-2xl p-8 text-white">
        <div className="flex items-center gap-3 mb-4">
          <Zap size={24} />
          <span className="text-sm font-bold uppercase tracking-wider text-blue-200">Today's Recommendation</span>
        </div>
        <h2 className="text-xl font-bold mb-2">{recommendations[0]?.title || "No insights"}</h2>
        <p className="text-blue-100 leading-relaxed mb-4">{recommendations[0]?.text || "Complete tasks to unlock MATE insights."}</p>
        <button className="bg-white/20 hover:bg-white/30 text-white px-6 py-2.5 rounded-xl text-sm font-semibold transition-colors flex items-center gap-2">
          View all recommendations <ArrowRight size={16} />
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {recommendations.map((rec, i) => (
          <div key={i} className={`${priorityColors[rec.color]} rounded-xl border p-5`}>
            <div className="flex items-center gap-2 mb-3">
              <rec.icon size={18} className={iconColors[rec.color]} />
              <h3 className="font-bold text-gray-900 text-sm">{rec.title}</h3>
            </div>
            <p className="text-sm text-gray-600 leading-relaxed">{rec.text}</p>
            {i !== 0 && (
              <button className="mt-3 text-xs font-semibold text-blue-600 hover:text-blue-700 flex items-center gap-1">
                View details <ArrowRight size={12} />
              </button>
            )}
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="font-bold text-gray-900 mb-4 flex items-center gap-2"><AlertTriangle size={18} /> Risks & Blockers</h3>
          {overdueTasks.length === 0 && blockedTasks.length === 0 ? (
            <div className="flex items-center gap-2 text-green-600"><CheckCircle size={18} /> <span className="text-sm">No active risks or blockers detected.</span></div>
          ) : (
            <div className="space-y-3">
              {overdueTasks.map((t) => (
                <div key={t.taskId} className="flex items-center gap-3 p-3 rounded-lg bg-red-50 border border-red-100">
                  <AlertTriangle size={16} className="text-red-500 shrink-0" />
                  <div>
                    <div className="text-sm font-medium text-gray-900">{t.title}</div>
                    <div className="text-xs text-red-500">Overdue · Due {new Date(t.dueDate).toLocaleDateString()}</div>
                  </div>
                </div>
              ))}
              {blockedTasks.map((t) => (
                <div key={t.taskId} className="flex items-center gap-3 p-3 rounded-lg bg-orange-50 border border-orange-100">
                  <AlertTriangle size={16} className="text-orange-500 shrink-0" />
                  <div>
                    <div className="text-sm font-medium text-gray-900">{t.title}</div>
                    <div className="text-xs text-orange-500">Blocked · {t.projectName || "No project"}</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="font-bold text-gray-900 mb-4 flex items-center gap-2"><TrendingUp size={18} /> Daily Summary</h3>
          <div className="space-y-4">
            <div className="flex items-center justify-between p-3 rounded-lg bg-gray-50">
              <div className="flex items-center gap-2"><CheckCircle size={18} className="text-green-500" /><span className="text-sm text-gray-700">Tasks Completed</span></div>
              <span className="font-bold text-gray-900">{stats?.completedTasks || 0}</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-lg bg-gray-50">
              <div className="flex items-center gap-2"><Clock size={18} className="text-amber-500" /><span className="text-sm text-gray-700">Pending</span></div>
              <span className="font-bold text-gray-900">{stats?.pendingTasks || 0}</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-lg bg-gray-50">
              <div className="flex items-center gap-2"><FolderKanban size={18} className="text-blue-500" /><span className="text-sm text-gray-700">Active Projects</span></div>
              <span className="font-bold text-gray-900">{stats?.activeProjects || 0}</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-lg bg-gray-50">
              <div className="flex items-center gap-2"><Target size={18} className="text-purple-500" /><span className="text-sm text-gray-700">Health Score</span></div>
              <span className="font-bold text-gray-900">{stats?.companyHealthScore || 0}/100</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
