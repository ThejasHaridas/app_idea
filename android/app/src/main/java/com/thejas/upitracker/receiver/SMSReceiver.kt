package com.thejas.upitracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.thejas.upitracker.data.db.AppDatabase
import com.thejas.upitracker.data.model.Transaction
import com.thejas.upitracker.utils.Classifier
import com.thejas.upitracker.utils.SMSParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val body = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            .joinToString("") { it.messageBody }

        val parsed = SMSParser.parse(body) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val categories = db.categoryDao().getAllOnce()
            val catId = Classifier.classify(parsed.vpa, parsed.vpa, categories)

            db.transactionDao().insert(
                Transaction(
                    amount = parsed.amount,
                    type = parsed.type,
                    vpa = parsed.vpa,
                    bank = parsed.bank,
                    refNo = parsed.refNo,
                    categoryId = catId,
                    date = parsed.date,
                    source = "sms",
                    rawSms = parsed.rawSms
                )
            )
        }
    }
}
