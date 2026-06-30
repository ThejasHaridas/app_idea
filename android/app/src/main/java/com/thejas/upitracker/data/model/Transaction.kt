package com.thejas.upitracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("categoryId"), Index("date")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,            // "debit" | "credit"
    val description: String? = null,
    val vpa: String? = null,
    val bank: String? = null,
    val refNo: String? = null,
    val categoryId: Long? = null,
    val date: String,            // yyyy-MM-dd
    val source: String = "manual", // "manual" | "sms"
    val rawSms: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
