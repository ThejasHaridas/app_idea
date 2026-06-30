package com.thejas.upitracker.data.db

import androidx.room.*
import com.thejas.upitracker.data.model.SpendingLimit
import kotlinx.coroutines.flow.Flow

@Dao
interface LimitDao {

    @Query("SELECT * FROM spending_limits")
    fun getAll(): Flow<List<SpendingLimit>>

    @Query("SELECT * FROM spending_limits WHERE categoryId = :catId")
    suspend fun getForCategory(catId: Long): SpendingLimit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(limit: SpendingLimit)

    @Query("DELETE FROM spending_limits WHERE categoryId = :catId")
    suspend fun deleteForCategory(catId: Long)
}
