package com.example.resqmesh.data.repository

import com.example.resqmesh.data.local.MessageDao
import com.example.resqmesh.data.remote.ApiService
import com.example.resqmesh.model.MeshMessage
import kotlinx.coroutines.flow.Flow

class MessageRepository(
    private val localDao: MessageDao,
    private val apiService: ApiService
) {
    fun getBroadcastMessages(): Flow<List<MeshMessage>> {
        return localDao.getBroadcastMessages()
    }

    suspend fun saveMessageLocally(message: MeshMessage) {
        localDao.insertMessage(message)
    }

    suspend fun offloadToServer(message: MeshMessage): Boolean {
        return try {
            val response = apiService.offloadMessageToServer(message)
            if (response.isSuccessful) {
                message.isSyncedToBackend = true
                localDao.insertMessage(message)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}