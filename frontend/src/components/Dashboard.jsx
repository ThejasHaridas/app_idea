import React, { useEffect, useState } from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts';
import { api } from '../api/client';

const fmt = (n) => '₹' + Number(n).toLocaleString('en-IN', { maximumFractionDigits: 2 });
const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December'];

export default function Dashboard() {
  const now = new Date();
  const [month, setMonth] = useState(String(now.getMonth() + 1));
  const [year,  setYear]  = useState(String(now.getFullYear()));
  const [summary, setSummary] = useState([]);
  const [recent,  setRecent]  = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      api.getSummary({ month, year }),
      api.getTransactions({ month, year })
    ]).then(([s, t]) => {
      setSummary(s);
      setRecent(t.slice(0, 5));
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [month, year]);

  const totalSpent = summary.reduce((a, c) => a + Number(c.spent), 0);
  const overLimit  = summary.filter(c => c.monthly_limit && c.spent > c.monthly_limit);
  const pieData    = summary.filter(c => c.spent > 0);

  return (
    <div className="p-4 space-y-4">
      {/* Month / Year picker */}
      <div className="flex gap-2">
        <select value={month} onChange={e => setMonth(e.target.value)}
          className="bg-gray-800 text-white rounded-xl px-3 py-2 text-sm flex-1">
          {MONTHS.map((m, i) => <option key={i} value={String(i + 1)}>{m}</option>)}
        </select>
        <input type="number" value={year} onChange={e => setYear(e.target.value)}
          className="bg-gray-800 text-white rounded-xl px-3 py-2 text-sm w-24"
          min="2020" max="2035" />
      </div>

      {/* Hero card */}
      <div className="bg-gradient-to-br from-indigo-600 to-purple-700 rounded-2xl p-5">
        <p className="text-indigo-200 text-sm">Total Spent This Month</p>
        <p className="text-4xl font-bold text-white mt-1">{fmt(totalSpent)}</p>
        {overLimit.length > 0 && (
          <p className="text-yellow-300 text-xs mt-2">⚠️ {overLimit.length} category{overLimit.length > 1 ? 'ies' : 'y'} over limit</p>
        )}
      </div>

      {/* Over-limit alerts */}
      {overLimit.map(c => (
        <div key={c.id} className="bg-red-900/40 border border-red-700/50 rounded-xl p-3 flex items-center gap-3">
          <span className="text-2xl">{c.icon}</span>
          <div>
            <p className="text-red-300 text-sm font-medium">{c.name} over limit!</p>
            <p className="text-red-400 text-xs">{fmt(c.spent)} spent &mdash; limit {fmt(c.monthly_limit)}</p>
          </div>
        </div>
      ))}

      {/* Pie chart */}
      {!loading && pieData.length > 0 && (
        <div className="bg-gray-900 rounded-2xl p-4">
          <p className="text-sm text-gray-400 mb-3">Spending by Category</p>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={pieData} dataKey="spent" nameKey="name" cx="50%" cy="50%" outerRadius={80} stroke="none">
                {pieData.map((c, i) => <Cell key={i} fill={c.color} />)}
              </Pie>
              <Tooltip
                formatter={(v) => [fmt(v), 'Spent']}
                contentStyle={{ background: '#111827', border: '1px solid #374151', borderRadius: 8, fontSize: 12 }}
              />
            </PieChart>
          </ResponsiveContainer>
          {/* Legend */}
          <div className="flex flex-wrap gap-2 mt-2">
            {pieData.map(c => (
              <div key={c.id} className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full inline-block" style={{ background: c.color }} />
                <span className="text-xs text-gray-400">{c.icon} {c.name}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Category breakdown with limit bars */}
      <div className="bg-gray-900 rounded-2xl p-4 space-y-4">
        <p className="text-sm text-gray-400">Category Breakdown</p>
        {loading ? (
          <p className="text-gray-500 text-sm text-center py-4">Loading...</p>
        ) : pieData.length === 0 ? (
          <p className="text-gray-500 text-sm text-center py-4">No transactions this month</p>
        ) : pieData.map(c => (
          <div key={c.id}>
            <div className="flex items-center justify-between mb-1">
              <span className="text-sm text-white">{c.icon} {c.name}</span>
              <div className="text-right">
                <span className="text-sm text-white">{fmt(c.spent)}</span>
                {c.monthly_limit && (
                  <span className="text-xs text-gray-500 ml-1">/ {fmt(c.monthly_limit)}</span>
                )}
              </div>
            </div>
            {c.monthly_limit && (
              <div className="h-1.5 bg-gray-800 rounded-full overflow-hidden">
                <div
                  className="h-full rounded-full transition-all"
                  style={{
                    width: `${Math.min(100, (c.spent / c.monthly_limit) * 100)}%`,
                    background: c.spent > c.monthly_limit ? '#ef4444' : c.color
                  }}
                />
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Recent transactions */}
      <div className="bg-gray-900 rounded-2xl p-4">
        <p className="text-sm text-gray-400 mb-3">Recent Transactions</p>
        {recent.length === 0 ? (
          <p className="text-gray-500 text-sm text-center py-4">No transactions yet</p>
        ) : (
          <div className="space-y-3">
            {recent.map(t => (
              <div key={t.id} className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-2 min-w-0">
                  <span className="text-xl shrink-0">{t.category_icon || '💳'}</span>
                  <div className="min-w-0">
                    <p className="text-sm text-white truncate">{t.description || t.vpa || 'UPI Transfer'}</p>
                    <p className="text-xs text-gray-500">{t.date}</p>
                  </div>
                </div>
                <span className={`text-sm font-semibold shrink-0 ${
                  t.type === 'debit' ? 'text-red-400' : 'text-green-400'
                }`}>
                  {t.type === 'debit' ? '-' : '+'}{fmt(t.amount)}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
