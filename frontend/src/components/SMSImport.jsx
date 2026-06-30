import React, { useState } from 'react';
import { api } from '../api/client';

export default function SMSImport({ onImported }) {
  const [text,    setText]    = useState('');
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleImport = async () => {
    if (!text.trim()) return;
    setLoading(true);
    const messages = text.split(/\n{2,}|---+/).map(s => s.trim()).filter(Boolean);
    try {
      const r = await api.importSMS(messages);
      setResults(r);
      if (r.some(x => x.status === 'imported')) onImported();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const imported = results?.filter(r => r.status === 'imported').length ?? 0;
  const skipped  = results?.filter(r => r.status === 'skipped').length ?? 0;
  const errors   = results?.filter(r => r.status === 'error').length ?? 0;

  return (
    <div className="p-4 space-y-4">
      <div>
        <h2 className="text-white font-semibold mb-1">Import from SMS</h2>
        <p className="text-gray-400 text-xs">Paste your bank UPI SMS messages below. Separate multiple SMS with a blank line or ---.</p>
      </div>

      <div className="bg-gray-900 rounded-xl p-3 text-xs text-gray-500 space-y-1">
        <p className="text-gray-400 font-medium mb-1">Supported formats:</p>
        <p>• Rs.500.00 has been <strong>debited</strong> from account XX1234 via <strong>UPI</strong> on 30-06-2026. UPI Ref No.123456789</p>
        <p>• INR 1000.00 <strong>debited</strong> from A/c No. XX1234 for <strong>UPI</strong>/swiggy@upi Ref 987654321</p>
        <p>• Your a/c XX1234 is <strong>debited</strong> by Rs.250 on 30/06/26 for <strong>UPI</strong> Ref 112233445</p>
      </div>

      <textarea
        value={text} onChange={e => setText(e.target.value)}
        placeholder="Paste SMS messages here..."
        rows={8}
        className="w-full bg-gray-800 text-white rounded-xl px-4 py-3 text-sm placeholder-gray-600 resize-none focus:outline-none focus:ring-2 focus:ring-indigo-500"
      />

      <button onClick={handleImport} disabled={loading || !text.trim()}
        className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-semibold rounded-xl py-3 transition-colors">
        {loading ? 'Processing...' : 'Import SMS'}
      </button>

      {results && (
        <div className="bg-gray-900 rounded-xl p-4 space-y-2">
          <p className="text-sm font-medium text-white">
            {imported} imported · {skipped} skipped · {errors} errors
          </p>
          <div className="space-y-1 max-h-48 overflow-auto">
            {results.map((r, i) => (
              <div key={i} className={`text-xs p-2 rounded-lg ${
                r.status === 'imported' ? 'bg-green-900/40 text-green-400'
                : r.status === 'error'  ? 'bg-red-900/40 text-red-400'
                : 'bg-gray-800 text-gray-500'
              }`}>
                {r.status === 'imported' ? '✅' : r.status === 'error' ? '❌' : '⏭️'} {r.status}
                {r.reason && ` — ${r.reason}`}
                {r.id && ` (ID: ${r.id})`}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
