const Database = require('better-sqlite3');
const path = require('path');

const db = new Database(path.join(__dirname, 'upi_tracker.db'));

db.exec(`
  CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    color TEXT NOT NULL DEFAULT '#6366f1',
    icon TEXT NOT NULL DEFAULT '💳',
    keywords TEXT NOT NULL DEFAULT '[]',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS limits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL UNIQUE,
    monthly_limit REAL NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount REAL NOT NULL,
    type TEXT NOT NULL CHECK(type IN ('debit', 'credit')),
    description TEXT,
    vpa TEXT,
    bank TEXT,
    ref_no TEXT,
    category_id INTEGER,
    date TEXT NOT NULL,
    source TEXT NOT NULL DEFAULT 'manual',
    raw_sms TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
  );
`);

const count = db.prepare('SELECT COUNT(*) as c FROM categories').get();
if (count.c === 0) {
  const insert = db.prepare('INSERT INTO categories (name, color, icon, keywords) VALUES (?, ?, ?, ?)');
  const defaults = [
    ['Food & Dining',   '#f97316', '🍽️', JSON.stringify(['zomato','swiggy','restaurant','cafe','food','hotel','kitchen','biryani','pizza','burger'])],
    ['Groceries',       '#22c55e', '🛒', JSON.stringify(['bigbasket','blinkit','grofer','grocery','vegetables','supermarket','dmart','reliance'])],
    ['Transportation',  '#3b82f6', '🚗', JSON.stringify(['uber','ola','rapido','metro','bus','auto','cab','taxi','petrol','fuel','irctc'])],
    ['Shopping',        '#a855f7', '🛍️', JSON.stringify(['amazon','flipkart','myntra','meesho','shop','store','mall','nykaa','ajio'])],
    ['Entertainment',   '#ec4899', '🎬', JSON.stringify(['netflix','hotstar','prime','spotify','pvr','inox','bookmyshow','youtube','zee5'])],
    ['Utilities',       '#14b8a6', '⚡', JSON.stringify(['electricity','water','gas','internet','wifi','broadband','airtel','jio','bsnl','recharge','bill'])],
    ['Healthcare',      '#ef4444', '🏥', JSON.stringify(['pharmacy','hospital','clinic','doctor','medicine','apollo','medplus','1mg','netmeds'])],
    ['Education',       '#eab308', '📚', JSON.stringify(['school','college','university','course','tuition','fees','udemy','coursera','books'])],
    ['Travel',          '#06b6d4', '✈️', JSON.stringify(['flight','makemytrip','goibibo','oyo','booking','airbnb','holiday','tour','cleartrip'])],
    ['Transfers',       '#8b5cf6', '💸', JSON.stringify(['transfer','send','paytm','phonepe','gpay','bhim','neft','imps'])],
    ['Others',          '#6b7280', '💳', JSON.stringify([])]
  ];
  for (const row of defaults) insert.run(...row);
}

module.exports = db;
