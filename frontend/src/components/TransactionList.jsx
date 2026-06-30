import React, { useEffect, useState } from 'react';
import { api } from '../api/client';

const fmt = (n) => '₹' + Number(n).toLocaleString('en-IN', { maximumFractionDigits: 2 });
const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

export default function TransactionList({ onUpdate }) {
  const now = new Date();
  const [transactions, setTransactions] = useState([]);
  const [categories,   setCategories]   = useState([]);
  const [month,  setMonth]  = useState(String(now.getMonth() + 1));
  const [year,   setYear]   = useState(String(now.getFullYear()));
  const [catFilter, setCatFilter] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    const params = { month, year };
    if (catFilter) params.category = catFilter;
    Promise.all([api.getTransactions(params), api.getCategories()])
      .then(([t, c]) => { setTransactions(t); setCategories(c); setLoading(false); })
      .catch(() => setLoading(false));
  };

  useEffect(() => { load(); }, [month, year, catFilter]);

  const handleDelete = async (id) => {
    if (!confirm('Delete this transaction?')) return;
    await api.deleteTransaction(id);
    load(); onUpdate();
  };

  const handleCategoryChange = async (tx, catId) => {
    await api.updateTransaction(tx.id, { ...tx, category_id: catId || null });
    load(); onUpdate();
  };

  const totalDebit  = transactions.filter(t => t.type === 'debit').reduce((a, t) => a + t.amount, 0);
  const totalCredit = transactions.filter(t => t.type === 'credit').reduce((a, t) => a + t.amount, 0);

  return (
    <div className="p-4 space-y-3">
      {/* Filters */}
      <div className="flex gap-2 flex-wrap">
        <select value={month} onChange={e => setMonth(e.target.value)}
          className="bg-gray-800 text-white rounded-xl px-3 py-2 text-sm">
          {MONTHS.map((m, i) => <option key={i} value={String(i + 1)}>{m}</option>)}
        </select>
        <input type="number" value={year} onChange={e => setYear(e.target.value)}
          className="bg-gray-800 text-white rounded-xl px-3 py-2 text-sm w-20" />
        <select value={catFilter} onChange={e => setCatFilter(e.target.value)}
          className="bg-gray-800 text-white rounded-xl px-3 py-2 text-sm flex-1">
          <option value="">All Categories</option>
          {categories.map(c => <option key={c.id} value={c.id}>{c.icon} {c.name}</option>)}
        </select>
      </div>

      {/* Summary row */}
      {!loading && transactions.length > 0 && (
        <div className="flex gap-2">
          <div className="flex-1 bg-red-900/30 rounded-xl p-3 text-center">
            <p className="text-xs text-red-400">Debited</p>
            <p className="text-sm font-semibold text-red-300">{fmt(totalDebit)}</p>
          </div>
          <div className="flex-1 bg-green-900/30 rounded-xl p-3 text-center">
            <p className="text-xs text-green-400">Credited</p>
            <p className="text-sm font-semibold text-green-300">{fmt(totalCredit)}</p>
          </div>
        </div>
      )}

      {loading ? (
        <p className="text-center text-gray-400 py-12">Loading...</p>
      ) : transactions.length === 0 ? (
        <p className="text-center text-gray-500 py-12">No transactions found</p>
      ) : (
        <div className="space-y-2">
          {transactions.map(t => (
            <div key={t.id} className="bg-gray-900 rounded-xl p-3">
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-center gap-2 flex-1 min-w-0">
                  <span className="text-2xl shrink-0">{t.category_icon || '💳'}</span>
                  <div className="min-w-0">
                    <p className="text-sm text-white truncate">{t.description || t.vpa || 'UPI Transfer'}</p>
                    <p className="text-xs text-gray-500">{t.date} · {t.bank || t.source}</p>
                    {t.ref_no && <p className="text-xs text-gray-600">Ref: {t.ref_no}</p>}
                  </div>
                </div>
                <div className="shrink-0 text-right">
                  <span className={`text-sm font-semibold ${
                    t.type === 'debit' ? 'text-red-400' : 'text-green-400'
                  }`}>
                    {t.type === 'debit' ? '-' : '+'}{fmt(t.amount)}
                  </span>
                </div>
              </div>
              <div className="flex items-center gap-2 mt-2">
                <select
                  value={t.category_id || ''}
                  onChange={e => handleCategoryChange(t, e.target.value)}
                  className="bg-gray-800 text-gray-300 rounded-lg px-2 py-1 text-xs flex-1"
                >
                  <option value="">Uncategorized</option>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.icon} {c.name}</option>)}
                </select>
                <button onClick={() => handleDelete(t.id)}
                  className="text-gray-600 hover:text-red-400 px-2 py-1 text-sm transition-colors">
                  🗑️
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
