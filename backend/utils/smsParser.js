function parseAmount(str) {
  return parseFloat(String(str).replace(/,/g, ''));
}

function extractDate(sms) {
  const patterns = [
    /([0-3]\d[-\/][01]\d[-\/]\d{2,4})/,
    /(\d{4}-\d{2}-\d{2})/,
    /([0-3]\d-[A-Za-z]{3}-\d{4})/
  ];
  for (const p of patterns) {
    const m = sms.match(p);
    if (m) {
      const d = new Date(m[1]);
      if (!isNaN(d)) return d.toISOString().split('T')[0];
    }
  }
  return new Date().toISOString().split('T')[0];
}

function extractVPA(sms) {
  const m = sms.match(/([\w.\-]+@[\w]+)/i);
  return m ? m[1] : null;
}

function extractRef(sms) {
  const m = sms.match(/(?:Ref|UPI Ref|txn|transaction)[^\d]*(\d{9,})/i);
  return m ? m[1] : null;
}

function extractAccount(sms) {
  const m = sms.match(/(?:a\/c|account|acct)[^X\d]*([Xx\d]{4,10})/i);
  return m ? m[1] : null;
}

function extractBank(sms) {
  const banks = ['HDFC','SBI','ICICI','AXIS','KOTAK','PNB','BOB','CANARA','IDBI','YES'];
  for (const b of banks) {
    if (sms.toUpperCase().includes(b)) return b;
  }
  return 'Unknown';
}

function parseUPISMS(sms) {
  const text = sms.trim();
  if (!/UPI/i.test(text)) return null;

  const amountMatch = text.match(/(?:Rs\.?|INR)\s?([\d,]+\.?\d*)/i);
  if (!amountMatch) return null;

  const typeMatch = text.match(/\b(debited|credited)\b/i);
  if (!typeMatch) return null;

  return {
    amount: parseAmount(amountMatch[1]),
    type: typeMatch[1].toLowerCase() === 'debited' ? 'debit' : 'credit',
    bank: extractBank(text),
    account: extractAccount(text),
    vpa: extractVPA(text),
    ref_no: extractRef(text),
    date: extractDate(text),
    raw_sms: sms,
    source: 'sms'
  };
}

module.exports = { parseUPISMS };
