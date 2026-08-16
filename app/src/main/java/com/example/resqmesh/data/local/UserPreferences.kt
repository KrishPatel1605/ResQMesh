package com.example.resqmesh.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mesh_prefs", Context.MODE_PRIVATE)

    enum class ThemeMode { SYSTEM, LIGHT, DARK }

    fun getUserId(): String? {
        return prefs.getString("USER_ID", null)
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString("USER_ID", userId).apply()
    }

    fun getThemeMode(): ThemeMode {
        val mode = prefs.getString("THEME_MODE", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(mode)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putString("THEME_MODE", mode.name).apply()
    }
}
