package com.example.resqmesh.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.resqmesh.model.MeshMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MeshMessage)

    // Get all broadcast messages (receiverId is null)
    @Query("SELECT * FROM messages WHERE receiverId IS NULL ORDER BY timestamp DESC")
    fun getBroadcastMessages(): Flow<List<MeshMessage>>

    // Get DMs for a specific user
    @Query("SELECT * FROM messages WHERE receiverId = :userId OR senderId = :userId ORDER BY timestamp DESC")
    fun getDirectMessages(userId: String): Flow<List<MeshMessage>>
}