package com.psi.shopnotes

import androidx.room.*

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val date: String?,
    val quantity: Int = 1
)