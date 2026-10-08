import { useState } from "react";
import { Settings2, User, Bell, Shield, Loader2 } from "lucide-react";

export default function Settings({ username }) {
  const [saved, setSaved] = useState(false);

  return (
    <div className="space-y-6">
      <div><h1 className="text-2xl font-bold text-gray-900">Settings</h1><p className="text-sm text-gray-500">Manage your account and preferences.</p></div>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <div className="flex items-center gap-2 mb-4"><User size={18} className="text-blue-600" /><h2 className="font-bold text-gray-900">Profile</h2></div>
            <div className="space-y-4">
              <div><label className="text-sm font-medium text-gray-700 mb-1 block">Name</label><input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500" defaultValue={username || ""} /></div>
              <div><label className="text-sm font-medium text-gray-700 mb-1 block">Email</label><input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500" type="email" defaultValue={localStorage.getItem("companyos_current_user") || ""} /></div>
              <button onClick={() => { setSaved(true); setTimeout(() => setSaved(false), 2000); }} className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-lg text-sm font-semibold">Save Changes</button>
              {saved && <span className="text-sm text-green-600">Saved!</span>}
            </div>
          </div>
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <div className="flex items-center gap-2 mb-4"><Bell size={18} className="text-blue-600" /><h2 className="font-bold text-gray-900">Notifications</h2></div>
            <div className="space-y-3">
              {["Task reminders", "Project updates", "MATE insights", "Weekly digest"].map((item) => (
                <label key={item} className="flex items-center justify-between p-3 rounded-lg bg-gray-50 cursor-pointer">
                  <span className="text-sm text-gray-700">{item}</span>
                  <input type="checkbox" defaultChecked className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                </label>
              ))}
            </div>
          </div>
        </div>
        <div>
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <div className="flex items-center gap-2 mb-4"><Shield size={18} className="text-blue-600" /><h2 className="font-bold text-gray-900">Security</h2></div>
            <div className="space-y-3 text-sm">
              <div className="flex items-center gap-2 text-green-600"><span className="w-2 h-2 rounded-full bg-green-500" /> Email verified</div>
              <div className="flex items-center gap-2 text-gray-600"><span className="w-2 h-2 rounded-full bg-blue-500" /> JWT session active</div>
              <button className="mt-3 text-sm text-red-600 hover:text-red-700 font-medium">Change Password</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
