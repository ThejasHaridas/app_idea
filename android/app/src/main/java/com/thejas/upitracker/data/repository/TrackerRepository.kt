package com.thejas.upitracker.data.repository

import com.thejas.upitracker.data.db.AppDatabase
import com.thejas.upitracker.data.model.Category
import com.thejas.upitracker.data.model.SpendingLimit
import com.thejas.upitracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class TrackerRepository(private val db: AppDatabase) {

    fun getTransactionsByMonth(month: String, year: String): Flow<List<Transaction>> =
        db.transactionDao().getByMonth(month, year)

    suspend fun addTransaction(tx: Transaction): Long = db.transactionDao().insert(tx)
    suspend fun updateTransaction(tx: Transaction) = db.transactionDao().update(tx)
    suspend fun deleteTransaction(tx: Transaction) = db.transactionDao().delete(tx)

    fun getCategories(): Flow<List<Category>> = db.categoryDao().getAll()
    suspend fun getCategoriesOnce(): List<Category> = db.categoryDao().getAllOnce()
    suspend fun addCategory(cat: Category): Long = db.categoryDao().insert(cat)
    suspend fun updateCategory(cat: Category) = db.categoryDao().update(cat)
    suspend fun deleteCategory(cat: Category) = db.categoryDao().delete(cat)

    fun getLimits(): Flow<List<SpendingLimit>> = db.limitDao().getAll()
    suspend fun setLimit(catId: Long, limit: Double) =
        db.limitDao().upsert(SpendingLimit(categoryId = catId, monthlyLimit = limit))
    suspend fun removeLimit(catId: Long) = db.limitDao().deleteForCategory(catId)
}
