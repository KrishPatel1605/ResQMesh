package com.example.resqmesh.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqmesh.data.repository.MessageRepository
import com.example.resqmesh.mesh.MeshManager
import com.example.resqmesh.model.MeshMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeshViewModel(
    private val myUserId: String,
    private val repository: MessageRepository,
    private val meshManager: MeshManager
) : ViewModel() {

    val broadcastMessages = repository.getBroadcastMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val connectedDeviceCount = meshManager.connectedDeviceCount
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun startMesh() {
        meshManager.startMesh()
    }

    fun sendBroadcast(content: String) {
        val message = MeshMessage(
            senderId = myUserId,
            receiverId = null, // Null indicates broadcast
            content = content
        )

        viewModelScope.launch {
            repository.saveMessageLocally(message)
            meshManager.broadcastToMesh(message)
        }
    }

    fun sendDirectMessage(targetUserId: String, content: String) {
        val message = MeshMessage(
            senderId = myUserId,
            receiverId = targetUserId,
            content = content
        )

        viewModelScope.launch {
            repository.saveMessageLocally(message)
            meshManager.broadcastToMesh(message) // Hops through mesh until it finds targetUserId
        }
    }

    override fun onCleared() {
        super.onCleared()
        meshManager.stopMesh()
    }
}