import React, { useState } from 'react';
import Dashboard from './components/Dashboard';
import TransactionList from './components/TransactionList';
import AddTransaction from './components/AddTransaction';
import CategoryLimits from './components/CategoryLimits';
import SMSImport from './components/SMSImport';

const TABS = [
  { id: 'dashboard',    label: 'Dashboard',  icon: '📊' },
  { id: 'transactions', label: 'History',    icon: '📋' },
  { id: 'add',          label: 'Add',        icon: '➕' },
  { id: 'sms',          label: 'SMS Import', icon: '📱' },
  { id: 'limits',       label: 'Limits',     icon: '🎯' },
];

export default function App() {
  const [tab, setTab] = useState('dashboard');
  const [refreshKey, setRefreshKey] = useState(0);
  const refresh = () => setRefreshKey(k => k + 1);

  return (
    <div className="min-h-screen bg-gray-950 flex flex-col max-w-lg mx-auto">
      <header className="bg-gray-900 border-b border-gray-800 px-4 py-3 flex items-center gap-3 sticky top-0 z-10">
        <span className="text-2xl">💳</span>
        <div>
          <h1 className="text-lg font-bold text-white leading-none">UPI Tracker</h1>
          <p className="text-xs text-gray-400">Track · Classify · Control</p>
        </div>
      </header>

      <main className="flex-1 overflow-auto pb-20">
        {tab === 'dashboard'    && <Dashboard    key={refreshKey} />}
        {tab === 'transactions' && <TransactionList key={refreshKey} onUpdate={refresh} />}
        {tab === 'add'          && <AddTransaction onAdded={() => { refresh(); setTab('transactions'); }} />}
        {tab === 'sms'          && <SMSImport onImported={() => { refresh(); setTab('transactions'); }} />}
        {tab === 'limits'       && <CategoryLimits key={refreshKey} />}
      </main>

      <nav className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-lg bg-gray-900 border-t border-gray-800 flex z-10">
        {TABS.map(t => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={`flex-1 flex flex-col items-center py-2 gap-0.5 text-xs transition-colors ${
              tab === t.id ? 'text-indigo-400' : 'text-gray-500 hover:text-gray-300'
            }`}
          >
            <span className="text-xl">{t.icon}</span>
            <span>{t.label}</span>
          </button>
        ))}
      </nav>
    </div>
  );
}
