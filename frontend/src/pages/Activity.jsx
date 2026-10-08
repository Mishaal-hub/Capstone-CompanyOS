import { useEffect, useState } from "react";
import { api } from "../api";
import { Activity as ActivityIcon, Loader2, Clock } from "lucide-react";

export default function Activity({ username }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadActivity(); }, []);

  async function loadActivity() {
    try {
      const data = await api.get("/dashboard/overview");
      setItems(data?.recentActivity || []);
    } catch {}
    setLoading(false);
  }

  if (loading) return <div className="flex items-center justify-center h-64"><Loader2 className="animate-spin text-blue-600" size={32} /></div>;

  return (
    <div className="space-y-6">
      <div><h1 className="text-2xl font-bold text-gray-900">Activity</h1><p className="text-sm text-gray-500">Recent activity across your organization.</p></div>
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        {items.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <ActivityIcon size={48} className="mx-auto mb-4 opacity-50" />
            <p>No recent activity</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {items.map((a, i) => (
              <div key={i} className="flex items-center gap-4 p-4 hover:bg-gray-50 transition-colors">
                <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
                  <ActivityIcon size={18} className="text-blue-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="text-sm font-medium text-gray-900">{a.action || "Activity"}</div>
                  <div className="text-xs text-gray-500">{a.entityType || "System"}</div>
                </div>
                <div className="text-xs text-gray-400 shrink-0 flex items-center gap-1"><Clock size={12} /> {a.timestamp ? new Date(a.timestamp).toLocaleString() : "Just now"}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
