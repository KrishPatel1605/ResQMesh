package com.example.resqmesh.mesh

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.gson.Gson
import com.example.resqmesh.data.repository.MessageRepository
import com.example.resqmesh.model.MeshMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeshManager(
    private val context: Context,
    private val myUserId: String,
    private val repository: MessageRepository,
    private val networkMonitor: NetworkMonitor
) {
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val connectedEndpoints = mutableSetOf<String>()
    private val gson = Gson()

    // P2P_CLUSTER allows a true mesh network (N-to-N connections)
    private val strategy = Strategy.P2P_CLUSTER

    fun startMesh() {
        startAdvertising()
        startDiscovery()
    }

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startAdvertising(myUserId, "DISASTER_MESH_APP", connectionLifecycleCallback, options)
    }

    private fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery("DISASTER_MESH_APP", endpointDiscoveryCallback, options)
    }

    // Handles incoming connections from other devices
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) connectedEndpoints.add(endpointId)
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
        }
    }

    // Handles discovering other devices
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(myUserId, endpointId, connectionLifecycleCallback)
        }
        override fun onEndpointLost(endpointId: String) {}
    }

    // Handles receiving data payloads (messages)
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            val messageString = String(bytes)
            val message = gson.fromJson(messageString, MeshMessage::class.java)

            processIncomingMessage(message, endpointId)
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private fun processIncomingMessage(message: MeshMessage, senderEndpoint: String) {
        // Prevent infinite loops
        if (message.ttl <= 0) return

        CoroutineScope(Dispatchers.IO).launch {
            // Save to local database
            repository.saveMessageLocally(message)

            // If I am the destination, notify the UI
            if (message.receiverId == myUserId) {
                Log.d("Mesh", "Message reached destination!")
                return@launch
            }

            // The Bridge logic: Do I have internet?
            if (networkMonitor.hasInternetConnection()) {
                val success = repository.offloadToServer(message)
                if (success) {
                    Log.d("Mesh", "Message offloaded to PostgreSQL server.")
                    return@launch // Stop hopping, server will handle it
                }
            }

            // If no internet, decrement TTL and hop the message to other devices
            message.ttl -= 1
            broadcastToMesh(message, excludeEndpoint = senderEndpoint)
        }
    }

    fun broadcastToMesh(message: MeshMessage, excludeEndpoint: String? = null) {
        val payload = Payload.fromBytes(gson.toJson(message).toByteArray())
        val targets = connectedEndpoints.filter { it != excludeEndpoint }

        if (targets.isNotEmpty()) {
            connectionsClient.sendPayload(targets.toList(), payload)
        }
    }
}