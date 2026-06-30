package com.thejas.upitracker.utils

import java.text.SimpleDateFormat
import java.util.*

data class ParsedSMS(
    val amount: Double,
    val type: String,        // "debit" | "credit"
    val bank: String,
    val account: String?,
    val vpa: String?,
    val refNo: String?,
    val date: String,        // yyyy-MM-dd
    val rawSms: String
)

object SMSParser {

    private val BANKS = listOf("HDFC", "SBI", "ICICI", "AXIS", "KOTAK", "PNB", "BOB", "CANARA", "YES", "INDUSIND")

    private val OUTPUT_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val INPUT_FMTS = listOf(
        "dd-MM-yyyy", "dd/MM/yyyy", "dd-MM-yy", "dd/MM/yy",
        "yyyy-MM-dd", "dd-MMM-yyyy", "d-MM-yyyy"
    ).map { SimpleDateFormat(it, Locale.US) }

    fun parse(sms: String): ParsedSMS? {
        if (!sms.contains("UPI", ignoreCase = true)) return null

        val amount = Regex("""(?:Rs\.?|INR)\s?([0-9,]+\.?\d*)""", RegexOption.IGNORE_CASE)
            .find(sms)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

        val typeStr = Regex("""\b(debited|credited)\b""", RegexOption.IGNORE_CASE)
            .find(sms)?.groupValues?.get(1)?.lowercase() ?: return null
        val type = if (typeStr == "debited") "debit" else "credit"

        val vpa     = Regex("""([\w.\-]+@[\w]+)""").find(sms)?.groupValues?.get(1)
        val refNo   = Regex("""(?:Ref|UPI Ref|txn)[^\d]*(\d{9,})""", RegexOption.IGNORE_CASE).find(sms)?.groupValues?.get(1)
        val account = Regex("""(?:a/c|account|acct)[^X\d]*([Xx\d]{4,10})""", RegexOption.IGNORE_CASE).find(sms)?.groupValues?.get(1)
        val bank    = BANKS.firstOrNull { sms.contains(it, ignoreCase = true) } ?: "Unknown"
        val date    = extractDate(sms) ?: today()

        return ParsedSMS(amount, type, bank, account, vpa, refNo, date, sms)
    }

    private fun extractDate(sms: String): String? {
        val patterns = listOf(
            Regex("""(\d{1,2}[-/]\d{1,2}[-/]\d{2,4})"""),
            Regex("""(\d{4}-\d{2}-\d{2})"""),
            Regex("""(\d{1,2}-[A-Za-z]{3}-\d{4})""")
        )
        for (p in patterns) {
            val raw = p.find(sms)?.groupValues?.get(1) ?: continue
            for (fmt in INPUT_FMTS) {
                try {
                    val d = fmt.parse(raw) ?: continue
                    return OUTPUT_FMT.format(d)
                } catch (_: Exception) {}
            }
        }
        return null
    }

    private fun today() = OUTPUT_FMT.format(Date())
}
