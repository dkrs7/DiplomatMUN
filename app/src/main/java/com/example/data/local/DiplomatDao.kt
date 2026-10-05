package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiplomatDao {
    @Query("SELECT * FROM users ORDER BY id ASC LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM debate_sessions ORDER BY dateEpoch DESC")
    fun getAllSessions(): Flow<List<DebateSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: DebateSessionEntity): Long

    @Query("SELECT * FROM debate_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): DebateSessionEntity?

    @Query("DELETE FROM debate_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}
