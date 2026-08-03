package com.example.resqmesh.data.remote

import com.example.resqmesh.model.MeshMessage
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    // This endpoint connects to your backend (Node.js/Python),
    // which then inserts the message into PostgreSQL.
    @POST("/api/messages/offload")
    suspend fun offloadMessageToServer(@Body message: MeshMessage): Response<Unit>
}