package com.example.resqmesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.resqmesh.data.local.AppDatabase
import com.example.resqmesh.data.local.UserPreferences
import com.example.resqmesh.data.remote.RetrofitClient
import com.example.resqmesh.data.repository.MessageRepository
import com.example.resqmesh.mesh.MeshManager
import com.example.resqmesh.mesh.NetworkMonitor
import com.example.resqmesh.ui.screens.BroadcastScreen
import com.example.resqmesh.ui.screens.DmScreen
import com.example.resqmesh.ui.screens.ProfileScreen
import com.example.resqmesh.ui.viewmodels.MeshViewModel

private enum class AppTab { BROADCAST, DM, PROFILE }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPrefs = UserPreferences(this)
        val database = AppDatabase.getDatabase(this)
        val repository = MessageRepository(database.messageDao(), RetrofitClient.apiService)
        val networkMonitor = NetworkMonitor(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentUserId by remember { mutableStateOf(userPrefs.getUserId()) }

                    if (currentUserId == null) {
                        ProfileScreen(
                            currentUserId = null,
                            repository = repository,
                            onUserIdSaved = { newId ->
                                userPrefs.saveUserId(newId)
                                currentUserId = newId
                            }
                        )
                    } else {
                        val userId = currentUserId!!

                        val meshManager = remember(userId) {
                            MeshManager(this@MainActivity, userId, repository, networkMonitor)
                        }
                        val viewModel = remember(userId) {
                            MeshViewModel(userId, repository, meshManager, networkMonitor)
                        }

                        DisposableEffect(userId) {
                            onDispose { meshManager.stopMesh() }
                        }

                        var currentTab by remember { mutableStateOf(AppTab.BROADCAST) }

                        Scaffold(
                            bottomBar = {
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.BROADCAST,
                                        onClick = { currentTab = AppTab.BROADCAST },
                                        icon = {},
                                        label = { Text("Broadcast") }
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.DM,
                                        onClick = { currentTab = AppTab.DM },
                                        icon = {},
                                        label = { Text("Messages") }
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.PROFILE,
                                        onClick = { currentTab = AppTab.PROFILE },
                                        icon = {},
                                        label = { Text("Profile") }
                                    )
                                }
                            }
                        ) { padding ->
                            Box(modifier = Modifier.padding(padding)) {
                                when (currentTab) {
                                    AppTab.BROADCAST -> BroadcastScreen(viewModel)
                                    AppTab.DM -> DmScreen(viewModel)
                                    AppTab.PROFILE -> ProfileScreen(
                                        currentUserId = userId,
                                        repository = repository,
                                        onUserIdSaved = { newId ->
                                            userPrefs.saveUserId(newId)
                                            currentUserId = newId // rebuilds MeshManager/ViewModel with new ID
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}