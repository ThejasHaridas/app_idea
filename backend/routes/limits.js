const express = require('express');
const router = express.Router();
const db = require('../db');

router.get('/', (req, res) => {
  res.json(db.prepare(
    'SELECT l.*, c.name as category_name, c.color, c.icon FROM limits l JOIN categories c ON l.category_id = c.id'
  ).all());
});

router.put('/:categoryId', (req, res) => {
  const { monthly_limit } = req.body;
  const catId = req.params.categoryId;
  const existing = db.prepare('SELECT id FROM limits WHERE category_id=?').get(catId);
  if (existing) {
    db.prepare('UPDATE limits SET monthly_limit=? WHERE category_id=?').run(monthly_limit, catId);
  } else {
    db.prepare('INSERT INTO limits (category_id, monthly_limit) VALUES (?, ?)').run(catId, monthly_limit);
  }
  res.json({ success: true });
});

router.delete('/:categoryId', (req, res) => {
  db.prepare('DELETE FROM limits WHERE category_id=?').run(req.params.categoryId);
  res.json({ success: true });
});

module.exports = router;
