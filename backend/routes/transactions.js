const express = require('express');
const router = express.Router();
const db = require('../db');
const { classifyTransaction } = require('../utils/classifier');
const { parseUPISMS } = require('../utils/smsParser');

router.get('/summary', (req, res) => {
  const m = String(req.query.month || new Date().getMonth() + 1).padStart(2, '0');
  const y = req.query.year || String(new Date().getFullYear());
  const data = db.prepare(`
    SELECT c.id, c.name, c.color, c.icon,
      COALESCE(SUM(CASE WHEN t.type='debit' THEN t.amount ELSE 0 END), 0) as spent,
      l.monthly_limit
    FROM categories c
    LEFT JOIN transactions t ON t.category_id = c.id
      AND strftime('%m', t.date) = ? AND strftime('%Y', t.date) = ?
    LEFT JOIN limits l ON l.category_id = c.id
    GROUP BY c.id ORDER BY spent DESC
  `).all(m, y);
  res.json(data);
});

router.get('/', (req, res) => {
  const { month, year, category } = req.query;
  let q = `SELECT t.*, c.name as category_name, c.color as category_color, c.icon as category_icon
    FROM transactions t LEFT JOIN categories c ON t.category_id = c.id`;
  const cond = [], params = [];
  if (month && year) {
    cond.push(`strftime('%m', t.date) = ? AND strftime('%Y', t.date) = ?`);
    params.push(String(month).padStart(2, '0'), String(year));
  }
  if (category) { cond.push('t.category_id = ?'); params.push(category); }
  if (cond.length) q += ' WHERE ' + cond.join(' AND ');
  q += ' ORDER BY t.date DESC, t.created_at DESC';
  res.json(db.prepare(q).all(...params));
});

router.post('/', (req, res) => {
  const { amount, type, description, vpa, date, category_id } = req.body;
  if (!amount || !type || !date) return res.status(400).json({ error: 'amount, type, date required' });
  const catId = category_id || classifyTransaction(description, vpa);
  const r = db.prepare(
    'INSERT INTO transactions (amount, type, description, vpa, date, category_id, source) VALUES (?, ?, ?, ?, ?, ?, \'manual\')'
  ).run(amount, type, description || null, vpa || null, date, catId);
  res.status(201).json({ id: r.lastInsertRowid });
});

router.post('/import-sms', (req, res) => {
  const { messages } = req.body;
  if (!Array.isArray(messages)) return res.status(400).json({ error: 'messages array required' });
  const results = [];
  for (const sms of messages) {
    const parsed = parseUPISMS(sms);
    if (!parsed) { results.push({ status: 'skipped', reason: 'not a UPI message' }); continue; }
    const catId = classifyTransaction(parsed.vpa, parsed.vpa);
    try {
      const r = db.prepare(
        'INSERT INTO transactions (amount, type, vpa, bank, ref_no, date, category_id, source, raw_sms) VALUES (?, ?, ?, ?, ?, ?, ?, \'sms\', ?)'
      ).run(parsed.amount, parsed.type, parsed.vpa, parsed.bank, parsed.ref_no, parsed.date, catId, parsed.raw_sms);
      results.push({ status: 'imported', id: r.lastInsertRowid });
    } catch (e) {
      results.push({ status: 'error', reason: e.message });
    }
  }
  res.json(results);
});

router.put('/:id', (req, res) => {
  const { amount, type, description, vpa, date, category_id } = req.body;
  db.prepare(
    'UPDATE transactions SET amount=?, type=?, description=?, vpa=?, date=?, category_id=? WHERE id=?'
  ).run(amount, type, description, vpa, date, category_id, req.params.id);
  res.json({ success: true });
});

router.delete('/:id', (req, res) => {
  db.prepare('DELETE FROM transactions WHERE id=?').run(req.params.id);
  res.json({ success: true });
});

module.exports = router;
