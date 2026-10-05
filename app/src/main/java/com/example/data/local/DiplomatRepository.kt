package com.example.data.local

import kotlinx.coroutines.flow.Flow

class DiplomatRepository(private val dao: DiplomatDao) {
    val activeUser: Flow<UserEntity?> = dao.getActiveUser()
    val allSessions: Flow<List<DebateSessionEntity>> = dao.getAllSessions()

    suspend fun saveUser(user: UserEntity): Long = dao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    suspend fun saveSession(session: DebateSessionEntity): Long = dao.insertSession(session)

    suspend fun getSessionById(id: Long): DebateSessionEntity? = dao.getSessionById(id)

    suspend fun deleteSession(id: Long) = dao.deleteSessionById(id)

    suspend fun clearSession() = dao.clearUsers()
}
