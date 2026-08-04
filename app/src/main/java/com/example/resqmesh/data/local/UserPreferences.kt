package com.example.resqmesh.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mesh_prefs", Context.MODE_PRIVATE)

    fun getUserId(): String? {
        return prefs.getString("USER_ID", null)
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString("USER_ID", userId).apply()
    }
}