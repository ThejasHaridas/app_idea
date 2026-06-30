import React, { useEffect, useState } from 'react';
import { api } from '../api/client';

export default function AddTransaction({ onAdded }) {
  const [form, setForm] = useState({
    amount: '',
    type: 'debit',
    description: '',
    vpa: '',
    date: new Date().toISOString().split('T')[0],
    category_id: ''
  });
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { api.getCategories().then(setCategories); }, []);

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async (e) => {
    e.preventDefault();
    if (!form.amount || !form.date) { setError('Amount and date are required'); return; }
    setLoading(true); setError('');
    try {
      await api.addTransaction({ ...form, amount: parseFloat(form.amount), category_id: form.category_id || null });
      onAdded();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-4">
      <h2 className="text-white font-semibold mb-4">Add Transaction</h2>
      <form onSubmit={submit} className="space-y-4">

        {/* Debit / Credit toggle */}
        <div className="flex bg-gray-800 rounded-xl p-1 gap-1">
          {['debit', 'credit'].map(tp => (
            <button type="button" key={tp} onClick={() => set('type', tp)}
              className={`flex-1 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                form.type === tp
                  ? tp === 'debit' ? 'bg-red-600 text-white' : 'bg-green-600 text-white'
                  : 'text-gray-400 hover:text-white'
              }`}>
              {tp === 'debit' ? '↑ Debit (Sent)' : '↓ Credit (Received)'}
            </button>
          ))}
        </div>

        <div>
          <label className="text-xs text-gray-400 block mb-1">Amount (₹) *</label>
          <input type="number" step="0.01" min="0" value={form.amount}
            onChange={e => set('amount', e.target.value)} placeholder="0.00" required
            className="w-full bg-gray-800 text-white text-2xl font-bold rounded-xl px-4 py-3 placeholder-gray-600" />
        </div>

        <div>
          <label className="text-xs text-gray-400 block mb-1">Date *</label>
          <input type="date" value={form.date} onChange={e => set('date', e.target.value)} required
            className="w-full bg-gray-800 text-white rounded-xl px-4 py-3" />
        </div>

        <div>
          <label className="text-xs text-gray-400 block mb-1">Description</label>
          <input type="text" value={form.description} onChange={e => set('description', e.target.value)}
            placeholder="What was this for?"
            className="w-full bg-gray-800 text-white rounded-xl px-4 py-3 placeholder-gray-600" />
        </div>

        <div>
          <label className="text-xs text-gray-400 block mb-1">UPI VPA / Merchant</label>
          <input type="text" value={form.vpa} onChange={e => set('vpa', e.target.value)}
            placeholder="merchant@upi"
            className="w-full bg-gray-800 text-white rounded-xl px-4 py-3 placeholder-gray-600" />
        </div>

        <div>
          <label className="text-xs text-gray-400 block mb-1">Category</label>
          <select value={form.category_id} onChange={e => set('category_id', e.target.value)}
            className="w-full bg-gray-800 text-white rounded-xl px-4 py-3">
            <option value="">Auto-detect from description</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.icon} {c.name}</option>)}
          </select>
        </div>

        {error && <p className="text-red-400 text-sm">{error}</p>}

        <button type="submit" disabled={loading}
          className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-semibold rounded-xl py-3 transition-colors">
          {loading ? 'Adding...' : 'Add Transaction'}
        </button>
      </form>
    </div>
  );
}
