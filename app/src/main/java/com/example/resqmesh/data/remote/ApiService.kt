package com.example.resqmesh.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class SupabaseUser(
    @SerializedName("user_id") val userId: String
)

data class SupabaseMessage(
    @SerializedName("message_id") val messageId: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("receiver_id") val receiverId: String?,
    @SerializedName("content") val content: String,
    @SerializedName("ttl") val ttl: Int,
    @SerializedName("created_at_client") val createdAtClient: Long,
    @SerializedName("delivered") val delivered: Boolean = false
)

data class DeliveredUpdate(
    @SerializedName("delivered") val delivered: Boolean = true
)

interface ApiService {

    // ---- Users ----

    @GET("rest/v1/users")
    suspend fun findUser(
        @Query("user_id") userIdFilter: String, // pass as "eq.myCustomId"
        @Query("select") select: String = "user_id"
    ): Response<List<SupabaseUser>>

    @Headers("Prefer: resolution=ignore-duplicates")
    @POST("rest/v1/users")
    suspend fun registerUser(@Body user: SupabaseUser): Response<Unit>

    // ---- Messages ----

    @POST("rest/v1/messages")
    suspend fun offloadMessageToServer(@Body message: SupabaseMessage): Response<Unit>

    @GET("rest/v1/messages")
    suspend fun fetchInbox(
        @Query("receiver_id") receiverIdFilter: String, // "eq.myCustomId"
        @Query("delivered") deliveredFilter: String = "eq.false",
        @Query("order") order: String = "inserted_at.asc"
    ): Response<List<SupabaseMessage>>

    @PATCH("rest/v1/messages")
    suspend fun markDelivered(
        @Query("message_id") messageIdFilter: String, // "eq.<id>"
        @Body update: DeliveredUpdate = DeliveredUpdate()
    ): Response<Unit>
}