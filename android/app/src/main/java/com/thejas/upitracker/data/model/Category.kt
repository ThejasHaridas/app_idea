package com.thejas.upitracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "💳",
    val colorHex: String = "#6366F1",
    val keywords: String = "[]"  // stored as JSON array string
)
