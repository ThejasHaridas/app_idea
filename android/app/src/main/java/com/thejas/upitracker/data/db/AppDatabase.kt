package com.thejas.upitracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thejas.upitracker.data.model.Category
import com.thejas.upitracker.data.model.SpendingLimit
import com.thejas.upitracker.data.model.Transaction

@Database(
    entities = [Transaction::class, Category::class, SpendingLimit::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun limitDao(): LimitDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "upi_tracker.db")
                    .addCallback(SeedCallback)
                    .build()
                    .also { INSTANCE = it }
            }

        private val SeedCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val cats = listOf(
                    listOf("Food & Dining",  "🍽️", "#F97316", "[\"zomato\",\"swiggy\",\"restaurant\",\"cafe\",\"food\",\"hotel\",\"kitchen\",\"biryani\",\"pizza\",\"burger\"]"),
                    listOf("Groceries",      "🛒", "#22C55E", "[\"bigbasket\",\"blinkit\",\"grofer\",\"grocery\",\"vegetables\",\"supermarket\",\"dmart\",\"reliance\"]"),
                    listOf("Transportation", "🚗", "#3B82F6", "[\"uber\",\"ola\",\"rapido\",\"metro\",\"bus\",\"auto\",\"cab\",\"taxi\",\"petrol\",\"fuel\",\"irctc\"]"),
                    listOf("Shopping",       "🛍️", "#A855F7", "[\"amazon\",\"flipkart\",\"myntra\",\"meesho\",\"shop\",\"store\",\"mall\",\"nykaa\",\"ajio\"]"),
                    listOf("Entertainment",  "🎬", "#EC4899", "[\"netflix\",\"hotstar\",\"prime\",\"spotify\",\"pvr\",\"inox\",\"bookmyshow\",\"youtube\",\"zee5\"]"),
                    listOf("Utilities",      "⚡", "#14B8A6", "[\"electricity\",\"water\",\"gas\",\"internet\",\"wifi\",\"airtel\",\"jio\",\"bsnl\",\"recharge\",\"bill\"]"),
                    listOf("Healthcare",     "🏥", "#EF4444", "[\"pharmacy\",\"hospital\",\"clinic\",\"doctor\",\"medicine\",\"apollo\",\"medplus\",\"1mg\",\"netmeds\"]"),
                    listOf("Education",      "📚", "#EAB308", "[\"school\",\"college\",\"university\",\"course\",\"tuition\",\"fees\",\"udemy\",\"coursera\",\"books\"]"),
                    listOf("Travel",         "✈️", "#06B6D4", "[\"flight\",\"makemytrip\",\"goibibo\",\"oyo\",\"booking\",\"airbnb\",\"holiday\",\"tour\",\"cleartrip\"]"),
                    listOf("Transfers",      "💸", "#8B5CF6", "[\"transfer\",\"send\",\"paytm\",\"phonepe\",\"gpay\",\"bhim\",\"neft\",\"imps\"]"),
                    listOf("Others",         "💳", "#6B7280", "[]")
                )
                cats.forEach { (name, icon, color, kw) ->
                    db.execSQL("INSERT INTO categories (name, icon, colorHex, keywords) VALUES (?, ?, ?, ?)",
                        arrayOf(name, icon, color, kw))
                }
            }
        }
    }
}
