package com.example.resqmesh.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqmesh.data.repository.MessageRepository
import com.example.resqmesh.mesh.MeshManager
import com.example.resqmesh.model.MeshMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeshViewModel(
    val myUserId: String,
    private val repository: MessageRepository,
    private val meshManager: MeshManager
) : ViewModel() {

    val broadcastMessages = repository.getBroadcastMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val connectedDeviceCount = meshManager.connectedDeviceCount
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun getConversation(targetUserId: String): Flow<List<MeshMessage>> {
        return repository.getConversation(myUserId, targetUserId)
    }

    fun startMesh() {
        meshManager.startMesh()
    }

    fun sendBroadcast(content: String) {
        val message = MeshMessage(senderId = myUserId, receiverId = null, content = content)
        viewModelScope.launch {
            repository.saveMessageLocally(message)
            meshManager.broadcastToMesh(message)
        }
    }

    fun sendDirectMessage(targetUserId: String, content: String) {
        val message = MeshMessage(senderId = myUserId, receiverId = targetUserId, content = content)
        viewModelScope.launch {
            repository.saveMessageLocally(message)
            meshManager.broadcastToMesh(message)
        }
    }
}