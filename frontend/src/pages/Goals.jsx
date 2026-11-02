import { useEffect, useState } from "react";
import { api } from "../api";
import { Target, Plus, Loader2, Edit3, Trash2 } from "lucide-react";

export default function Goals({ username }) {
  const [goals, setGoals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({
    title: "",
    description: "",
    targetValue: 100,
    currentValue: 0,
    status: "IN_PROGRESS",
    deadline: "",
  });

  useEffect(() => { loadGoals(); }, []);

  async function loadGoals() {
    try {
      const data = await api.get("/goals");
      setGoals(data || []);
    } catch (err) {
      setError(err.message);
    }
    setLoading(false);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    try {
      const payload = { ...form, level: "ORGANIZATION" };
      if (editing) {
        await api.put(`/goals/${editing}`, payload);
      } else {
        await api.post("/goals", payload);
      }
      setShowForm(false);
      setEditing(null);
      setForm({ title: "", description: "", targetValue: 100, currentValue: 0, status: "IN_PROGRESS", deadline: "" });
      loadGoals();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(id) {
    if (!confirm("Delete this goal?")) return;
    setError("");
    try {
      await api.delete(`/goals/${id}`);
      loadGoals();
    } catch (err) {
      setError(err.message);
    }
  }

  function startEdit(goal) {
    setEditing(goal.goalId);
    setForm({
      title: goal.title,
      description: goal.description || "",
      targetValue: goal.targetValue || 100,
      currentValue: goal.currentValue || 0,
      status: goal.status,
      deadline: goal.deadline || "",
    });
    setShowForm(true);
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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Goals</h1>
          <p className="text-sm text-gray-500">Track and manage organizational goals.</p>
        </div>
        <button
          onClick={() => {
            setEditing(null);
            setForm({ title: "", description: "", targetValue: 100, currentValue: 0, status: "IN_PROGRESS", deadline: "" });
            setShowForm(true);
          }}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold flex items-center gap-2"
        >
          <Plus size={16} /> New Goal
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700">{error}</div>
      )}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
          <h3 className="font-bold text-gray-900">{editing ? "Edit Goal" : "Create Goal"}</h3>
          <input
            className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Goal title"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
          <textarea
            className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Description"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            rows={3}
          />
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-xs text-gray-500 mb-1 block">Target Value</label>
              <input
                type="number"
                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
                value={form.targetValue}
                onChange={(e) => setForm({ ...form, targetValue: Number(e.target.value) })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 mb-1 block">Current Value</label>
              <input
                type="number"
                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
                value={form.currentValue}
                onChange={(e) => setForm({ ...form, currentValue: Number(e.target.value) })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 mb-1 block">Status</label>
              <select
                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
                value={form.status}
                onChange={(e) => setForm({ ...form, status: e.target.value })}
              >
                {["IN_PROGRESS", "ACHIEVED", "MISSED", "PAUSED"].map((s) => (
                  <option key={s} value={s}>{s.replace("_", " ")}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-xs text-gray-500 mb-1 block">Deadline</label>
              <input
                type="date"
                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
                value={form.deadline}
                onChange={(e) => setForm({ ...form, deadline: e.target.value })}
              />
            </div>
          </div>
          <div className="flex gap-3">
            <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-lg text-sm font-semibold">
              {editing ? "Update" : "Create"}
            </button>
            <button
              type="button"
              onClick={() => { setShowForm(false); setEditing(null); }}
              className="px-6 py-2.5 rounded-lg text-sm border border-gray-200 hover:bg-gray-50"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {goals.length === 0 && !showForm ? (
        <div className="text-center py-16 bg-white rounded-xl border border-gray-200">
          <Target size={48} className="mx-auto text-gray-300 mb-4" />
          <p className="text-gray-500">No goals set yet</p>
          <p className="text-gray-400 text-sm mt-1">Create a goal to start tracking progress.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {goals.map((g) => (
            <div key={g.goalId} className="bg-white rounded-xl border border-gray-200 p-6">
              <div className="flex items-start justify-between mb-1">
                <h3 className="font-bold text-gray-900">{g.title}</h3>
                <div className="flex gap-1 shrink-0 ml-2">
                  <button onClick={() => startEdit(g)} className="p-1.5 rounded hover:bg-gray-100 text-blue-600">
                    <Edit3 size={14} />
                  </button>
                  <button onClick={() => handleDelete(g.goalId)} className="p-1.5 rounded hover:bg-red-50 text-red-500">
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
              <p className="text-sm text-gray-500 mb-4">{g.description || ""}</p>
              <div className="w-full bg-gray-200 rounded-full h-3 mb-2">
                <div
                  className="bg-blue-600 h-3 rounded-full transition-all"
                  style={{ width: `${Math.min(g.progress || 0, 100)}%` }}
                />
              </div>
              <div className="flex justify-between text-xs text-gray-500">
                <span>{g.currentValue || 0} / {g.targetValue || 100}</span>
                <span className="font-semibold text-gray-700">{g.progress || 0}%</span>
              </div>
              <div className="mt-3 flex items-center justify-between">
                <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                  g.status === "ACHIEVED" ? "bg-green-100 text-green-700"
                  : g.status === "MISSED" ? "bg-red-100 text-red-700"
                  : g.status === "PAUSED" ? "bg-gray-100 text-gray-600"
                  : "bg-blue-100 text-blue-700"
                }`}>
                  {g.status?.replace("_", " ")}
                </span>
                {g.deadline && (
                  <span className="text-xs text-gray-400">
                    Due {new Date(g.deadline).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
