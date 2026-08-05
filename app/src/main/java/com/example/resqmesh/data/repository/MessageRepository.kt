package com.example.resqmesh.data.repository

import com.example.resqmesh.data.local.MessageDao
import com.example.resqmesh.data.remote.ApiService
import com.example.resqmesh.data.remote.SupabaseMessage
import com.example.resqmesh.data.remote.SupabaseUser
import com.example.resqmesh.model.MeshMessage
import kotlinx.coroutines.flow.Flow

class MessageRepository(
    private val localDao: MessageDao,
    private val apiService: ApiService
) {
    fun getBroadcastMessages(): Flow<List<MeshMessage>> {
        return localDao.getBroadcastMessages()
    }

    fun getConversation(myId: String, targetId: String): Flow<List<MeshMessage>> {
        return localDao.getConversation(myId, targetId)
    }

    fun getContacts(myId: String): Flow<List<String>> {
        return localDao.getContacts(myId)
    }

    // Pull messages waiting on the server for me, save locally, mark delivered
    suspend fun syncInboxToLocal(myId: String) {
        val inboxMessages = fetchInboxFromServer(myId)
        for (m in inboxMessages) {
            val meshMessage = MeshMessage(
                messageId = m.messageId,
                senderId = m.senderId,
                receiverId = m.receiverId,
                content = m.content,
                timestamp = m.createdAtClient,
                ttl = m.ttl,
                isSyncedToBackend = true
            )
            saveMessageLocally(meshMessage)
            markMessageDelivered(m.messageId)
        }
    }

    suspend fun saveMessageLocally(message: MeshMessage) {
        localDao.insertMessage(message)
    }

    suspend fun offloadToServer(message: MeshMessage): Boolean {
        if (message.receiverId == null) return false // broadcasts never go to the server
        return try {
            val supabaseMessage = SupabaseMessage(
                messageId = message.messageId,
                senderId = message.senderId,
                receiverId = message.receiverId,
                content = message.content,
                ttl = message.ttl,
                createdAtClient = message.timestamp
            )
            val response = apiService.offloadMessageToServer(supabaseMessage)
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

    // ---- Custom user ID handling ----

    suspend fun isUserIdAvailable(userId: String): Boolean {
        return try {
            val response = apiService.findUser("eq.$userId")
            response.isSuccessful && response.body().isNullOrEmpty()
        } catch (e: Exception) {
            false // treat network errors as "not available" to be safe
        }
    }

    suspend fun registerUser(userId: String): Boolean {
        return try {
            apiService.registerUser(SupabaseUser(userId)).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    // ---- Inbox: pulling messages addressed to me while online ----

    suspend fun fetchInboxFromServer(userId: String): List<SupabaseMessage> {
        return try {
            val response = apiService.fetchInbox("eq.$userId")
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markMessageDelivered(messageId: String) {
        try {
            apiService.markDelivered("eq.$messageId")
        } catch (e: Exception) {
            // best-effort; will retry next fetch cycle
        }
    }
}