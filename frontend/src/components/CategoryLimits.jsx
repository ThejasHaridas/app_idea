import React, { useEffect, useState } from 'react';
import { api } from '../api/client';

const fmt = (n) => '₹' + Number(n).toLocaleString('en-IN', { maximumFractionDigits: 2 });

export default function CategoryLimits() {
  const [categories, setCategories] = useState([]);
  const [limits,     setLimits]     = useState({});
  const [drafts,     setDrafts]     = useState({});
  const [saving,     setSaving]     = useState({});
  const [showNew,    setShowNew]    = useState(false);
  const [newCat,     setNewCat]     = useState({ name: '', icon: '💳', color: '#6366f1', keywords: '' });

  const load = () => {
    Promise.all([api.getCategories(), api.getLimits()]).then(([cats, lims]) => {
      setCategories(cats);
      const m = {};
      for (const l of lims) m[l.category_id] = l.monthly_limit;
      setLimits(m);
    });
  };

  useEffect(() => { load(); }, []);

  const saveLimit = async (catId) => {
    const val = drafts[catId];
    setSaving(s => ({ ...s, [catId]: true }));
    if (val === '' || val === undefined) {
      await api.deleteLimit(catId);
    } else {
      await api.setLimit(catId, parseFloat(val));
    }
    setSaving(s => ({ ...s, [catId]: false }));
    setDrafts(d => { const n = { ...d }; delete n[catId]; return n; });
    load();
  };

  const addCategory = async (e) => {
    e.preventDefault();
    const keywords = newCat.keywords.split(',').map(k => k.trim()).filter(Boolean);
    await api.addCategory({ ...newCat, keywords });
    setNewCat({ name: '', icon: '💳', color: '#6366f1', keywords: '' });
    setShowNew(false);
    load();
  };

  const deleteCategory = async (id) => {
    if (!confirm('Delete this category? Transactions in it will become uncategorized.')) return;
    await api.deleteCategory(id);
    load();
  };

  return (
    <div className="p-4 space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-white font-semibold">Categories & Monthly Limits</h2>
        <button onClick={() => setShowNew(v => !v)}
          className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs px-3 py-2 rounded-lg transition-colors">
          + New
        </button>
      </div>

      {showNew && (
        <form onSubmit={addCategory} className="bg-gray-900 rounded-xl p-4 space-y-3">
          <p className="text-sm font-medium text-white">New Category</p>
          <div className="flex gap-2">
            <input value={newCat.icon} onChange={e => setNewCat(n => ({ ...n, icon: e.target.value }))}
              placeholder="💳" className="bg-gray-800 text-white rounded-lg px-3 py-2 text-sm w-14 text-center" />
            <input value={newCat.name} onChange={e => setNewCat(n => ({ ...n, name: e.target.value }))}
              placeholder="Category name" required
              className="bg-gray-800 text-white rounded-lg px-3 py-2 text-sm flex-1" />
            <input type="color" value={newCat.color} onChange={e => setNewCat(n => ({ ...n, color: e.target.value }))}
              className="w-10 h-10 rounded-lg cursor-pointer bg-gray-800 border-0" />
          </div>
          <div>
            <label className="text-xs text-gray-400 block mb-1">Keywords (comma-separated, used for auto-classify)</label>
            <input value={newCat.keywords} onChange={e => setNewCat(n => ({ ...n, keywords: e.target.value }))}
              placeholder="zomato, swiggy, restaurant..."
              className="w-full bg-gray-800 text-white rounded-lg px-3 py-2 text-sm" />
          </div>
          <div className="flex gap-2">
            <button type="submit" className="flex-1 bg-indigo-600 text-white text-sm rounded-lg py-2">Add</button>
            <button type="button" onClick={() => setShowNew(false)}
              className="flex-1 bg-gray-700 text-white text-sm rounded-lg py-2">Cancel</button>
          </div>
        </form>
      )}

      <div className="space-y-3">
        {categories.map(cat => {
          const lim   = limits[cat.id];
          const draft = drafts[cat.id];
          const val   = draft !== undefined ? draft : (lim !== undefined ? String(lim) : '');
          const kws   = JSON.parse(cat.keywords || '[]');
          return (
            <div key={cat.id} className="bg-gray-900 rounded-xl p-4">
              <div className="flex items-center gap-3 mb-3">
                <span className="text-2xl">{cat.icon}</span>
                <div className="flex-1 min-w-0">
                  <p className="text-white text-sm font-medium">{cat.name}</p>
                  {kws.length > 0 && (
                    <p className="text-gray-500 text-xs truncate">
                      {kws.slice(0, 4).join(', ')}{kws.length > 4 ? '...' : ''}
                    </p>
                  )}
                </div>
                <button onClick={() => deleteCategory(cat.id)}
                  className="text-gray-600 hover:text-red-400 text-sm transition-colors">🗑️</button>
              </div>

              <div className="flex items-center gap-2">
                <span className="text-gray-400 text-xs whitespace-nowrap">Monthly Limit ₹</span>
                <input
                  type="number" min="0" step="100"
                  value={val}
                  onChange={e => setDrafts(d => ({ ...d, [cat.id]: e.target.value }))}
                  onBlur={() => { if (draft !== undefined) saveLimit(cat.id); }}
                  placeholder="No limit"
                  className="flex-1 bg-gray-800 text-white rounded-lg px-3 py-1.5 text-sm"
                />
                {lim !== undefined && (
                  <button onClick={() => { setDrafts(d => ({ ...d, [cat.id]: '' })); setTimeout(() => saveLimit(cat.id), 0); }}
                    className="text-gray-500 hover:text-red-400 text-xs transition-colors">Remove</button>
                )}
                {saving[cat.id] && <span className="text-xs text-gray-500">Saving…</span>}
              </div>

              {lim && (
                <p className="text-xs text-gray-500 mt-1">Current limit: {fmt(lim)}/month</p>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
