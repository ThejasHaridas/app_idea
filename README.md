# UPI Transaction Tracker

A full-stack web app to **track**, **classify**, and **control** your UPI spending.

## Features

- **Auto-classify** transactions into categories using keyword matching (Zomato → Food, Uber → Transport, etc.)
- **SMS Import** — paste bank SMS messages and auto-parse amount, date, VPA, ref number
- **Manual entry** — add any transaction with full control
- **Spending limits** — set monthly limits per category with progress bars and over-limit alerts
- **Dashboard** — pie chart, category breakdown, recent transactions
- **Reclassify** — change a transaction’s category at any time

## Stack

| Layer    | Tech |
|----------|------|
| Backend  | Node.js + Express + SQLite (better-sqlite3) |
| Frontend | React 18 + Vite + TailwindCSS |
| Charts   | Recharts |

## Setup

### 1. Backend

```bash
cd backend
npm install
npm start        # runs on http://localhost:3001
```

### 2. Frontend

```bash
cd frontend
npm install
npm run dev      # runs on http://localhost:5173
```

Open **http://localhost:5173** in your browser.

## UPI SMS Parsing

Supported bank SMS patterns (HDFC, SBI, ICICI, AXIS, and any generic UPI SMS):

```
Rs.500.00 has been debited from account XX1234 via UPI on 30-06-2026. UPI Ref No.123456789
INR 1000.00 debited from A/c No. XX1234 for UPI/swiggy@upi Ref 987654321
Your a/c XX1234 is debited by Rs.250 on 30/06/26 for UPI Ref 112233445
```

Parsed fields: amount, debit/credit, bank, account (masked), VPA, ref number, date.

## Default Categories

| Category | Icon | Example Keywords |
|----------|------|------------------|
| Food & Dining | 🍽️ | zomato, swiggy, restaurant |
| Groceries | 🛒 | bigbasket, blinkit, dmart |
| Transportation | 🚗 | uber, ola, rapido, metro |
| Shopping | 🛍️ | amazon, flipkart, myntra |
| Entertainment | 🎬 | netflix, hotstar, pvr |
| Utilities | ⚡ | electricity, jio, airtel, recharge |
| Healthcare | 🏥 | pharmacy, apollo, 1mg |
| Education | 📚 | udemy, coursera, school fees |
| Travel | ✈️ | makemytrip, oyo, flight |
| Transfers | 💸 | phonepe, gpay, paytm |
| Others | 💳 | everything else |

You can add custom categories with your own keywords from the **Limits** tab.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/transactions` | List transactions (filter: month, year, category) |
| POST | `/api/transactions` | Add manual transaction |
| PUT | `/api/transactions/:id` | Update transaction |
| DELETE | `/api/transactions/:id` | Delete transaction |
| POST | `/api/transactions/import-sms` | Bulk import from SMS array |
| GET | `/api/transactions/summary` | Category spending summary for a month |
| GET | `/api/categories` | List all categories |
| POST | `/api/categories` | Create category |
| PUT | `/api/categories/:id` | Update category |
| DELETE | `/api/categories/:id` | Delete category |
| GET | `/api/limits` | List all limits |
| PUT | `/api/limits/:categoryId` | Set monthly limit |
| DELETE | `/api/limits/:categoryId` | Remove limit |
