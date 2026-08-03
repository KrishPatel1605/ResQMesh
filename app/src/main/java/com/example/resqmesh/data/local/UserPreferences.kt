package com.example.resqmesh.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mesh_prefs", Context.MODE_PRIVATE)

    fun getOrGenerateUserId(): String {
        var userId = prefs.getString("USER_ID", null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            prefs.edit().putString("USER_ID", userId).apply()
        }
        return userId
    }
}