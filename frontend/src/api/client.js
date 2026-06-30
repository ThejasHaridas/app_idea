const BASE = '/api';

async function req(path, opts = {}) {
  const r = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...opts
  });
  if (!r.ok) throw new Error(await r.text());
  return r.json();
}

export const api = {
  getTransactions: (params) => req('/transactions?' + new URLSearchParams(params)),
  addTransaction:  (data)   => req('/transactions', { method: 'POST', body: JSON.stringify(data) }),
  updateTransaction: (id, data) => req(`/transactions/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteTransaction: (id)   => req(`/transactions/${id}`, { method: 'DELETE' }),
  importSMS:       (messages) => req('/transactions/import-sms', { method: 'POST', body: JSON.stringify({ messages }) }),
  getSummary:      (params) => req('/transactions/summary?' + new URLSearchParams(params)),

  getCategories:   ()       => req('/categories'),
  addCategory:     (data)   => req('/categories', { method: 'POST', body: JSON.stringify(data) }),
  updateCategory:  (id, data) => req(`/categories/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteCategory:  (id)     => req(`/categories/${id}`, { method: 'DELETE' }),

  getLimits:  ()                    => req('/limits'),
  setLimit:   (catId, monthly_limit) => req(`/limits/${catId}`, { method: 'PUT', body: JSON.stringify({ monthly_limit }) }),
  deleteLimit: (catId)              => req(`/limits/${catId}`, { method: 'DELETE' })
};
