import { useEffect, useState } from "react";
import { api } from "../api";
import { Users, Loader2, Plus, Edit3, Trash2, Mail, Phone } from "lucide-react";

const STATUS_OPTIONS = ["ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"];

export default function Team({ username }) {
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    designation: "",
    jobTitle: "",
    departmentId: "",
    status: "ACTIVE",
    hireDate: "",
  });

  useEffect(() => {
    loadTeam();
    loadDepartments();
  }, []);

  async function loadTeam() {
    try {
      const data = await api.get("/employees");
      setEmployees(data || []);
    } catch (err) {
      setError(err.message);
    }
    setLoading(false);
  }

  async function loadDepartments() {
    try {
      const data = await api.get("/departments");
      setDepartments(data || []);
    } catch {}
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    try {
      const payload = {
        ...form,
        departmentId: form.departmentId ? Number(form.departmentId) : null,
        hireDate: form.hireDate || null,
      };
      if (editing) {
        await api.put(`/employees/${editing}`, payload);
      } else {
        await api.post("/employees", payload);
      }
      setShowForm(false);
      setEditing(null);
      setForm({ firstName: "", lastName: "", email: "", phone: "", designation: "", jobTitle: "", departmentId: "", status: "ACTIVE", hireDate: "" });
      loadTeam();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(id) {
    if (!confirm("Remove this team member?")) return;
    setError("");
    try {
      await api.delete(`/employees/${id}`);
      loadTeam();
    } catch (err) {
      setError(err.message);
    }
  }

  function startEdit(emp) {
    setEditing(emp.employeeId);
    setForm({
      firstName: emp.firstName || "",
      lastName: emp.lastName || "",
      email: emp.email || "",
      phone: emp.phone || "",
      designation: emp.designation || "",
      jobTitle: emp.jobTitle || "",
      departmentId: emp.departmentId || "",
      status: emp.status || "ACTIVE",
      hireDate: emp.hireDate || "",
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
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Team</h1>
          <p className="text-sm text-gray-500">Manage your organization's team members.</p>
        </div>
        <button
          onClick={() => {
            setEditing(null);
            setForm({ firstName: "", lastName: "", email: "", phone: "", designation: "", jobTitle: "", departmentId: "", status: "ACTIVE", hireDate: "" });
            setShowForm(true);
          }}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold flex items-center gap-2 transition-colors"
        >
          <Plus size={16} /> Add Member
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700">{error}</div>
      )}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
          <h3 className="font-bold text-gray-900">{editing ? "Edit Team Member" : "Add Team Member"}</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input
              className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="First name *"
              value={form.firstName}
              onChange={(e) => setForm({ ...form, firstName: e.target.value })}
              required
            />
            <input
              className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Last name"
              value={form.lastName}
              onChange={(e) => setForm({ ...form, lastName: e.target.value })}
            />
            <input
              type="email"
              className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Email address *"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
            <input
              className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Phone"
              value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value })}
            />
            <input
              className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Designation (e.g. Senior Developer)"
              value={form.designation}
              onChange={(e) => setForm({ ...form, designation: e.target.value })}
            />
            <input
              className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Job title"
              value={form.jobTitle}
              onChange={(e) => setForm({ ...form, jobTitle: e.target.value })}
            />
            <select
              className="px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              value={form.departmentId}
              onChange={(e) => setForm({ ...form, departmentId: e.target.value })}
            >
              <option value="">No department</option>
              {departments.map((d) => (
                <option key={d.departmentId} value={d.departmentId}>{d.departmentName}</option>
              ))}
            </select>
            <select
              className="px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              value={form.status}
              onChange={(e) => setForm({ ...form, status: e.target.value })}
            >
              {STATUS_OPTIONS.map((s) => (
                <option key={s} value={s}>{s.replace("_", " ")}</option>
              ))}
            </select>
            <input
              type="date"
              className="px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              value={form.hireDate}
              onChange={(e) => setForm({ ...form, hireDate: e.target.value })}
            />
          </div>
          <div className="flex gap-3">
            <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-lg text-sm font-semibold">
              {editing ? "Update" : "Add Member"}
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

      {employees.length === 0 && !showForm ? (
        <div className="text-center py-16 bg-white rounded-xl border border-gray-200">
          <Users size={48} className="mx-auto text-gray-300 mb-4" />
          <p className="text-gray-500 font-medium">No team members yet</p>
          <p className="text-gray-400 text-sm mt-1">Add your first team member to get started.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {employees.map((emp) => (
            <div key={emp.employeeId} className="bg-white rounded-xl border border-gray-200 p-6 hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
                    <span className="text-blue-700 font-bold text-sm">
                      {(emp.firstName || "U")[0].toUpperCase()}
                    </span>
                  </div>
                  <div>
                    <div className="font-semibold text-gray-900 text-sm">
                      {emp.fullName || `${emp.firstName || ""} ${emp.lastName || ""}`.trim()}
                    </div>
                    <div className="text-xs text-gray-500">{emp.designation || emp.jobTitle || "Team Member"}</div>
                  </div>
                </div>
                <div className="flex gap-1 shrink-0">
                  <button onClick={() => startEdit(emp)} className="p-1.5 rounded hover:bg-gray-100 text-blue-600">
                    <Edit3 size={14} />
                  </button>
                  <button onClick={() => handleDelete(emp.employeeId)} className="p-1.5 rounded hover:bg-red-50 text-red-500">
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>

              <div className="space-y-1.5 text-sm text-gray-600">
                {emp.email && (
                  <div className="flex items-center gap-2">
                    <Mail size={13} className="text-gray-400" />
                    <span className="truncate">{emp.email}</span>
                  </div>
                )}
                {emp.phone && (
                  <div className="flex items-center gap-2">
                    <Phone size={13} className="text-gray-400" />
                    <span>{emp.phone}</span>
                  </div>
                )}
              </div>

              <div className="mt-3 flex items-center justify-between">
                <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                  emp.status === "ACTIVE" ? "bg-green-100 text-green-700"
                  : emp.status === "INACTIVE" ? "bg-gray-100 text-gray-500"
                  : emp.status === "ON_LEAVE" ? "bg-amber-100 text-amber-700"
                  : "bg-red-100 text-red-600"
                }`}>
                  {(emp.status || "ACTIVE").replace("_", " ")}
                </span>
                {emp.hireDate && (
                  <span className="text-xs text-gray-400">
                    Joined {new Date(emp.hireDate).toLocaleDateString("en-US", { month: "short", year: "numeric" })}
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
