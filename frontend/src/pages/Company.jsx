import { useEffect, useState } from "react";
import { api } from "../api";
import { Building2, Loader2, Mail, Phone, MapPin, Globe } from "lucide-react";

export default function Company({ username }) {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ companyName: "", email: "", phone: "", address: "", website: "", industry: "", logoUrl: "" });

  useEffect(() => { loadCompanies(); }, []);

  async function loadCompanies() {
    try {
      const data = await api.get("/companies");
      setCompanies(data || []);
    } catch {}
    setLoading(false);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      await api.post("/companies", { ...form, organizationId: 1 });
      setForm({ companyName: "", email: "", phone: "", address: "", website: "", industry: "", logoUrl: "" });
      loadCompanies();
    } catch {}
  }

  async function handleDelete(id) {
    if (!confirm("Delete this company?")) return;
    try { await api.delete(`/companies/${id}`); loadCompanies(); } catch {}
  }

  if (loading) return <div className="flex items-center justify-center h-64"><Loader2 className="animate-spin text-blue-600" size={32} /></div>;

  return (
    <div className="space-y-6">
      <div><h1 className="text-2xl font-bold text-gray-900">Company Profile</h1><p className="text-sm text-gray-500">Manage your organization details.</p></div>
      <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
        <h3 className="font-bold text-gray-900">Add Company</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500" placeholder="Company name" value={form.companyName} onChange={(e) => setForm({ ...form, companyName: e.target.value })} required />
          <input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500" placeholder="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500" placeholder="Phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500" placeholder="Industry" value={form.industry} onChange={(e) => setForm({ ...form, industry: e.target.value })} />
          <input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500" placeholder="Website" value={form.website} onChange={(e) => setForm({ ...form, website: e.target.value })} />
          <input className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500" placeholder="Address" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
        </div>
        <button type="submit" className="bg-blue-600 text-white px-6 py-2.5 rounded-lg text-sm font-semibold">Add Company</button>
      </form>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {companies.map((c) => (
          <div key={c.companyId || c.id} className="bg-white rounded-xl border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2"><Building2 size={18} className="text-blue-600" /><h3 className="font-bold text-gray-900">{c.companyName}</h3></div>
              <button onClick={() => handleDelete(c.companyId || c.id)} className="text-red-400 hover:text-red-600">×</button>
            </div>
            <div className="space-y-2 text-sm text-gray-600">
              {c.email && <div className="flex items-center gap-2"><Mail size={14} /> {c.email}</div>}
              {c.phone && <div className="flex items-center gap-2"><Phone size={14} /> {c.phone}</div>}
              {c.address && <div className="flex items-center gap-2"><MapPin size={14} /> {c.address}</div>}
              {c.website && <div className="flex items-center gap-2"><Globe size={14} /> {c.website}</div>}
            </div>
            {c.industry && <span className="mt-3 inline-block text-xs px-2 py-1 rounded-full bg-blue-100 text-blue-700 font-medium">{c.industry}</span>}
          </div>
        ))}
      </div>
    </div>
  );
}
