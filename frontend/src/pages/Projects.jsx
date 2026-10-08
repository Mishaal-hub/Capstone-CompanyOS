import { useEffect, useState } from "react";
import { api } from "../api";
import {
  FolderKanban,
  Plus,
  Edit3,
  Trash2,
  Loader2,
  BarChart3,
  Clock,
} from "lucide-react";

const STATUS_LABELS = {
  PLANNED: "Planned",
  IN_PROGRESS: "In Progress",
  ON_HOLD: "On Hold",
  COMPLETED: "Completed",
  CANCELLED: "Cancelled",
};

export default function Projects({ username }) {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ projectName: "", description: "", status: "PLANNED", priority: "MEDIUM", budget: "", endDate: "" });

  useEffect(() => { loadProjects(); }, []);

  async function loadProjects() {
    try {
      const data = await api.get("/projects");
      setProjects(data || []);
    } catch (err) {
      setError(err.message);
    }
    setLoading(false);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    try {
      const payload = { ...form, organizationId: 1, progress: 0 };
      if (editing) {
        await api.put(`/projects/${editing}`, payload);
      } else {
        await api.post("/projects", payload);
      }
      setShowForm(false);
      setEditing(null);
      setForm({ projectName: "", description: "", status: "PLANNED", priority: "MEDIUM", budget: "", endDate: "" });
      loadProjects();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(id) {
    if (!confirm("Delete this project?")) return;
    try {
      await api.delete(`/projects/${id}`);
      loadProjects();
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="animate-spin text-blue-600" size={32} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Projects</h1>
          <p className="text-sm text-gray-500">Manage your projects and track progress.</p>
        </div>
        <button
          onClick={() => { setEditing(null); setForm({ projectName: "", description: "", status: "PLANNED", priority: "MEDIUM", budget: "", endDate: "" }); setShowForm(true); }}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold flex items-center gap-2 transition-colors"
        >
          <Plus size={16} /> New Project
        </button>
      </div>

      {error && <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700">{error}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
          <h3 className="font-bold text-gray-900">{editing ? "Edit Project" : "Create New Project"}</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Project name" value={form.projectName} onChange={(e) => setForm({ ...form, projectName: e.target.value })} required />
            <input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            <select className="px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
              {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
            <select className="px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none" value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
              {["LOW", "MEDIUM", "HIGH", "CRITICAL"].map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
            <input type="number" className="px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none" placeholder="Budget" value={form.budget} onChange={(e) => setForm({ ...form, budget: e.target.value })} />
            <input type="date" className="px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} />
          </div>
          <div className="flex gap-3">
            <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-lg text-sm font-semibold">{editing ? "Update" : "Create"}</button>
            <button type="button" onClick={() => setShowForm(false)} className="px-6 py-2.5 rounded-lg text-sm font-medium border border-gray-200 hover:bg-gray-50">Cancel</button>
          </div>
        </form>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {projects.map((proj) => (
          <div key={proj.projectId} className="bg-white rounded-xl border border-gray-200 p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <FolderKanban size={18} className="text-blue-600" />
                <span className="font-bold text-gray-900">{proj.projectName}</span>
              </div>
              <span className="text-xs px-2 py-1 rounded-full bg-blue-100 text-blue-700 font-medium">
                {STATUS_LABELS[proj.status] || proj.status}
              </span>
            </div>
            <p className="text-sm text-gray-500 mb-4">{proj.description || "No description"}</p>
            <div className="mb-2">
              <div className="flex justify-between text-xs text-gray-500 mb-1">
                <span>Progress</span>
                <span>{proj.progress || 0}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div className="bg-blue-600 h-2 rounded-full transition-all" style={{ width: `${proj.progress || 0}%` }} />
              </div>
            </div>
            <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-100">
              <div className="flex items-center gap-3 text-xs text-gray-500">
                <span className="flex items-center gap-1"><BarChart3 size={12} /> {proj.totalTasks || 0} tasks</span>
                <span className="flex items-center gap-1"><Clock size={12} /> {proj.completedTasks || 0} done</span>
              </div>
              <div className="flex gap-1">
                <button onClick={() => { setEditing(proj.projectId); setForm({ projectName: proj.projectName, description: proj.description || "", status: proj.status, priority: proj.priority, budget: proj.budget || "", endDate: proj.endDate || "" }); setShowForm(true); }} className="p-1.5 rounded hover:bg-gray-100 text-blue-600">
                  <Edit3 size={14} />
                </button>
                <button onClick={() => handleDelete(proj.projectId)} className="p-1.5 rounded hover:bg-red-50 text-red-500">
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {projects.length === 0 && !showForm && (
        <div className="text-center py-16 bg-white rounded-xl border border-gray-200">
          <FolderKanban size={48} className="mx-auto text-gray-300 mb-4" />
          <p className="text-gray-500 font-medium">No projects yet</p>
          <p className="text-gray-400 text-sm mt-1">Create your first project to get started!</p>
        </div>
      )}
    </div>
  );
}
