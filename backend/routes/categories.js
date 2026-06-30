const express = require('express');
const router = express.Router();
const db = require('../db');

router.get('/', (req, res) => {
  res.json(db.prepare('SELECT * FROM categories ORDER BY name').all());
});

router.post('/', (req, res) => {
  const { name, color, icon, keywords } = req.body;
  if (!name) return res.status(400).json({ error: 'name required' });
  const r = db.prepare(
    'INSERT INTO categories (name, color, icon, keywords) VALUES (?, ?, ?, ?)'
  ).run(name, color || '#6366f1', icon || '💳', JSON.stringify(keywords || []));
  res.status(201).json({ id: r.lastInsertRowid });
});

router.put('/:id', (req, res) => {
  const { name, color, icon, keywords } = req.body;
  db.prepare(
    'UPDATE categories SET name=?, color=?, icon=?, keywords=? WHERE id=?'
  ).run(name, color, icon, JSON.stringify(keywords || []), req.params.id);
  res.json({ success: true });
});

router.delete('/:id', (req, res) => {
  db.prepare('DELETE FROM categories WHERE id=?').run(req.params.id);
  res.json({ success: true });
});

module.exports = router;
