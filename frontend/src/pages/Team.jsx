import { useEffect, useState } from "react";
import { api } from "../api";
import { Users, Loader2 } from "lucide-react";

export default function Team({ username }) {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadTeam(); }, []);

  async function loadTeam() {
    try {
      const data = await api.get("/employees");
      setEmployees(data || []);
    } catch {}
    setLoading(false);
  }

  if (loading) return <div className="flex items-center justify-center h-64"><Loader2 className="animate-spin text-blue-600" size={32} /></div>;

  return (
    <div className="space-y-6">
      <div><h1 className="text-2xl font-bold text-gray-900">Team</h1><p className="text-sm text-gray-500">Your organization's team members.</p></div>
      {employees.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-xl border border-gray-200">
          <Users size={48} className="mx-auto text-gray-300 mb-4" />
          <p className="text-gray-500">No team members added yet</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {employees.map((emp) => (
            <div key={emp.employeeId || emp.id} className="bg-white rounded-xl border border-gray-200 p-6">
              <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                  <span className="text-blue-700 font-bold text-sm">{(emp.firstName || "U")[0].toUpperCase()}</span>
                </div>
                <div>
                  <div className="font-semibold text-gray-900 text-sm">{emp.firstName} {emp.lastName}</div>
                  <div className="text-xs text-gray-500">{emp.role || "Member"}</div>
                </div>
              </div>
              <div className="text-sm text-gray-600">{emp.email}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
