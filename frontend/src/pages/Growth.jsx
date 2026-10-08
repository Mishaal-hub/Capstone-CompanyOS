import { useEffect, useState } from "react";
import { api } from "../api";
import { TrendingUp, BarChart3, ArrowUpRight, Loader2 } from "lucide-react";

export default function Growth({ username }) {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadData(); }, []);

  async function loadData() {
    try {
      const data = await api.get("/dashboard/overview");
      setStats(data);
    } catch {}
    setLoading(false);
  }

  if (loading) return <div className="flex items-center justify-center h-64"><Loader2 className="animate-spin text-blue-600" size={32} /></div>;

  const d = stats || {};
  const completionRate = d.totalTasks > 0 ? Math.round((d.completedTasks / d.totalTasks) * 100) : 0;

  return (
    <div className="space-y-6">
      <div><h1 className="text-2xl font-bold text-gray-900">Growth</h1><p className="text-sm text-gray-500">Business momentum and performance metrics.</p></div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: "Completion Rate", value: `${completionRate}%`, color: "text-green-600" },
          { label: "Active Projects", value: d.activeProjects || 0, color: "text-blue-600" },
          { label: "Health Score", value: d.companyHealthScore || 0, color: "text-purple-600" },
          { label: "Pipeline Value", value: `$${(d.totalPipelineValue || 0).toLocaleString()}`, color: "text-amber-600" },
        ].map((m) => (
          <div key={m.label} className="bg-white rounded-xl border border-gray-200 p-5">
            <div className="text-sm text-gray-500 mb-2">{m.label}</div>
            <div className={`text-2xl font-bold ${m.color}`}>{m.value}</div>
            <div className="flex items-center gap-1 mt-2 text-xs text-green-600"><ArrowUpRight size={14} /> Trending up</div>
          </div>
        ))}
      </div>
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4"><BarChart3 size={18} className="text-blue-600" /><h2 className="font-bold text-gray-900">Performance Breakdown</h2></div>
        <div className="space-y-4">
          {[
            { label: "Task Health", value: d.healthBreakdown?.taskHealth || 100, color: "bg-blue-500" },
            { label: "Project Health", value: d.healthBreakdown?.projectHealth || 100, color: "bg-green-500" },
            { label: "Goal Health", value: d.healthBreakdown?.goalHealth || 100, color: "bg-purple-500" },
          ].map((item) => (
            <div key={item.label}>
              <div className="flex justify-between text-sm mb-1"><span className="text-gray-700">{item.label}</span><span className="font-semibold">{item.value}%</span></div>
              <div className="w-full bg-gray-200 rounded-full h-3">
                <div className={`${item.color} h-3 rounded-full transition-all`} style={{ width: `${item.value}%` }} />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
