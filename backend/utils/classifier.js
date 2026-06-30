const db = require('../db');

function classifyTransaction(description, vpa) {
  const categories = db.prepare('SELECT * FROM categories ORDER BY name').all();
  const text = `${description || ''} ${vpa || ''}`.toLowerCase();

  for (const cat of categories) {
    if (cat.name === 'Others') continue;
    const keywords = JSON.parse(cat.keywords || '[]');
    for (const kw of keywords) {
      if (kw && text.includes(kw.toLowerCase())) return cat.id;
    }
  }

  const others = db.prepare("SELECT id FROM categories WHERE name = 'Others'").get();
  return others ? others.id : null;
}

module.exports = { classifyTransaction };
