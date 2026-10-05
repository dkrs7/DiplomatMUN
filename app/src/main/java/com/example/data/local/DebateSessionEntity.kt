package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debate_sessions")
data class DebateSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Int = 1,
    val committeeAcronym: String,
    val topic: String,
    val countryCode: String,
    val dateEpoch: Long = System.currentTimeMillis(),
    val overallScore: Int,
    val awardTier: String,
    val evaluationJson: String,
    val speechesCount: Int
)
