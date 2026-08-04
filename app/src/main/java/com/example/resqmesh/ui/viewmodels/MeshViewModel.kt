package com.example.resqmesh.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqmesh.data.repository.MessageRepository
import com.example.resqmesh.mesh.MeshManager
import com.example.resqmesh.mesh.NetworkMonitor
import com.example.resqmesh.model.MeshMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class MeshViewModel(
    val myUserId: String,
    private val repository: MessageRepository,
    private val meshManager: MeshManager,
    private val networkMonitor: NetworkMonitor   // <-- newly injected
) : ViewModel() {

    val broadcastMessages = repository.getBroadcastMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val connectedDeviceCount = meshManager.connectedDeviceCount
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val contacts = repository.getContacts(myUserId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Poll the server inbox periodically whenever we have internet
        viewModelScope.launch {
            while (true) {
                if (networkMonitor.hasInternetConnection()) {
                    repository.syncInboxToLocal(myUserId)
                }
                kotlinx.coroutines.delay(10_000) // every 10s, tune as needed
            }
        }
    }

    fun getConversation(targetUserId: String): Flow<List<MeshMessage>> {
        return repository.getConversation(myUserId, targetUserId)
    }

    fun startMesh() = meshManager.startMesh()

    fun sendBroadcast(content: String) {
        val message = MeshMessage(senderId = myUserId, receiverId = null, content = content)
        viewModelScope.launch {
            repository.saveMessageLocally(message)
            if (networkMonitor.hasInternetConnection()) {
                repository.offloadToServer(message)
            }
            // still hop over mesh too — broadcasts benefit from both paths
            meshManager.broadcastToMesh(message)
        }
    }

    fun sendDirectMessage(targetUserId: String, content: String) {
        val message = MeshMessage(senderId = myUserId, receiverId = targetUserId, content = content)
        viewModelScope.launch {
            repository.saveMessageLocally(message)
            if (networkMonitor.hasInternetConnection()) {
                val delivered = repository.offloadToServer(message)
                if (delivered) return@launch   // exit node succeeded, no need to hop
            }
            meshManager.broadcastToMesh(message)
        }
    }
}