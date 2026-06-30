package com.thejas.upitracker.data.db

import androidx.room.*
import com.thejas.upitracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("""
        SELECT * FROM transactions
        WHERE strftime('%m', date) = :month AND strftime('%Y', date) = :year
        ORDER BY date DESC, createdAt DESC
    """)
    fun getByMonth(month: String, year: String): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: Transaction): Long

    @Update
    suspend fun update(tx: Transaction)

    @Delete
    suspend fun delete(tx: Transaction)
}
