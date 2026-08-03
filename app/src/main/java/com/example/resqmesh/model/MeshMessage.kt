package com.example.resqmesh.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "messages")
data class MeshMessage(
    @PrimaryKey val messageId: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String?, // Null means it's a Broadcast
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    var ttl: Int = 10, // Max hops allowed
    var isSyncedToBackend: Boolean = false // True if uploaded via Internet
)