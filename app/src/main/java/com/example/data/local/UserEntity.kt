package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val age: Int,
    val title: String = "Distinguished Delegate",
    val preferredCountryCode: String = "USA",
    val totalDebates: Int = 0,
    val averageScore: Double = 0.0,
    val highestAward: String = "Delegate Candidate",
    val createdAt: Long = System.currentTimeMillis()
)
