import { useEffect, useState } from "react";
import { api } from "../api";
import { Bell, Check, CheckCheck, Loader2, Info, AlertTriangle, FolderKanban, Target } from "lucide-react";

const TYPE_CONFIG = {
  TASK_ASSIGNED:       { icon: Check,         color: "text-blue-500",   bg: "bg-blue-50"   },
  DEADLINE_RISK:       { icon: AlertTriangle,  color: "text-red-500",    bg: "bg-red-50"    },
  PROJECT_UPDATE:      { icon: FolderKanban,   color: "text-purple-500", bg: "bg-purple-50" },
  MATE_RECOMMENDATION: { icon: Target,         color: "text-indigo-500", bg: "bg-indigo-50" },
  APPROVAL_REQUIRED:   { icon: Info,           color: "text-amber-500",  bg: "bg-amber-50"  },
};

export default function Notifications({ username }) {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => { loadNotifications(); }, []);

  async function loadNotifications() {
    try {
      const data = await api.get("/notifications");
      setNotifications(data || []);
    } catch (err) {
      setError(err.message);
    }
    setLoading(false);
  }

  async function markRead(id) {
    try {
      await api.patch(`/notifications/${id}/read`);
      setNotifications((prev) =>
        prev.map((n) => (n.notificationId === id ? { ...n, read: true } : n))
      );
    } catch (err) {
      setError(err.message);
    }
  }

  async function markAllRead() {
    try {
      await api.patch("/notifications/read-all");
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch (err) {
      setError(err.message);
    }
  }

  const unreadCount = notifications.filter((n) => !n.read).length;

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
          <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
          <p className="text-sm text-gray-500">
            {unreadCount > 0 ? `${unreadCount} unread notification${unreadCount !== 1 ? "s" : ""}` : "All notifications read"}
          </p>
        </div>
        {unreadCount > 0 && (
          <button
            onClick={markAllRead}
            className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 transition-colors"
          >
            <CheckCheck size={16} /> Mark all as read
          </button>
        )}
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700">{error}</div>
      )}

      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        {notifications.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <Bell size={48} className="mx-auto mb-4 opacity-40" />
            <p className="font-medium">No notifications</p>
            <p className="text-sm mt-1">You're all caught up!</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {notifications.map((n) => {
              const config = TYPE_CONFIG[n.type] || { icon: Info, color: "text-gray-500", bg: "bg-gray-50" };
              const Icon = config.icon;
              return (
                <div
                  key={n.notificationId}
                  className={`flex items-start gap-4 p-4 transition-colors ${n.read ? "bg-white" : "bg-blue-50/40"}`}
                >
                  <div className={`w-9 h-9 rounded-full ${config.bg} flex items-center justify-center shrink-0 mt-0.5`}>
                    <Icon size={16} className={config.color} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <p className={`text-sm font-semibold ${n.read ? "text-gray-700" : "text-gray-900"}`}>
                          {n.title}
                        </p>
                        <p className="text-sm text-gray-600 mt-0.5">{n.message}</p>
                      </div>
                      {!n.read && (
                        <button
                          onClick={() => markRead(n.notificationId)}
                          className="shrink-0 p-1.5 rounded hover:bg-gray-100 text-blue-600"
                          title="Mark as read"
                        >
                          <Check size={14} />
                        </button>
                      )}
                    </div>
                    <div className="flex items-center gap-3 mt-1">
                      <span className="text-xs text-gray-400">
                        {n.createdAt ? new Date(n.createdAt).toLocaleString() : ""}
                      </span>
                      {!n.read && (
                        <span className="w-2 h-2 rounded-full bg-blue-500" />
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
