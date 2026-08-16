package com.example.resqmesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.resqmesh.data.local.AppDatabase
import com.example.resqmesh.data.local.UserPreferences
import com.example.resqmesh.data.remote.RetrofitClient
import com.example.resqmesh.data.repository.MessageRepository
import com.example.resqmesh.mesh.MeshManager
import com.example.resqmesh.mesh.NetworkMonitor
import com.example.resqmesh.ui.screens.BroadcastScreen
import com.example.resqmesh.ui.screens.DmScreen
import com.example.resqmesh.ui.screens.ProfileScreen
import com.example.resqmesh.ui.theme.ResQMeshTheme
import com.example.resqmesh.ui.viewmodels.MeshViewModel

private enum class AppTab { BROADCAST, DM, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val userPrefs = UserPreferences(this)
        val database = AppDatabase.getDatabase(this)
        val repository = MessageRepository(database.messageDao(), RetrofitClient.apiService)
        val networkMonitor = NetworkMonitor(this)

        setContent {
            var themeMode by remember { mutableStateOf(userPrefs.getThemeMode()) }
            val darkTheme = when (themeMode) {
                UserPreferences.ThemeMode.LIGHT -> false
                UserPreferences.ThemeMode.DARK -> true
                UserPreferences.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ResQMeshTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentUserId by remember { mutableStateOf(userPrefs.getUserId()) }

                    if (currentUserId == null) {
                        ProfileScreen(
                            currentUserId = null,
                            repository = repository,
                            userPrefs = userPrefs,
                            onUserIdSaved = { newId ->
                                userPrefs.saveUserId(newId)
                                currentUserId = newId
                            },
                            onThemeChanged = { themeMode = it }
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
                        var activeTargetId by remember { mutableStateOf<String?>(null) }
                        val connectedCount by viewModel.connectedDeviceCount.collectAsState()

                        // Handle back button for DM sub-navigation
                        BackHandler(enabled = activeTargetId != null) {
                            activeTargetId = null
                        }

                        Scaffold(
                            topBar = {
                                Surface(shadowElevation = 4.dp, tonalElevation = 4.dp) {
                                    CenterAlignedTopAppBar(
                                        title = {
                                            Text(
                                                text = if (activeTargetId != null) activeTargetId!! else when (currentTab) {
                                                    AppTab.BROADCAST -> "Emergency Network"
                                                    AppTab.DM -> "Direct Messages"
                                                    AppTab.PROFILE -> "Settings"
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        navigationIcon = {
                                            if (activeTargetId != null) {
                                                IconButton(onClick = { activeTargetId = null }) {
                                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                                }
                                            }
                                        },
                                        actions = {
                                            if (currentTab == AppTab.BROADCAST && activeTargetId == null) {
                                                ConnectionStatusIndicator(connectedCount)
                                            }
                                        },
                                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            titleContentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            },
                            bottomBar = {
                                if (activeTargetId == null) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 8.dp
                                    ) {
                                        NavigationBarItem(
                                            selected = currentTab == AppTab.BROADCAST,
                                            onClick = { currentTab = AppTab.BROADCAST },
                                            icon = { Icon(Icons.Default.Wifi, contentDescription = "Broadcast") },
                                            label = { Text("Broadcast") },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                            )
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == AppTab.DM,
                                            onClick = { currentTab = AppTab.DM },
                                            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Messages") },
                                            label = { Text("Messages") },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                            )
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == AppTab.PROFILE,
                                            onClick = { currentTab = AppTab.PROFILE },
                                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                            label = { Text("Profile") },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                            )
                                        )
                                    }
                                }
                            }
                        ) { padding ->
                            Box(
                                modifier = Modifier
                                    .padding(padding)
                                    .consumeWindowInsets(padding)
                                    .fillMaxSize()
                            ) {
                                when (currentTab) {
                                    AppTab.BROADCAST -> BroadcastScreen(viewModel)
                                    AppTab.DM -> DmScreen(
                                        viewModel = viewModel,
                                        activeTargetId = activeTargetId,
                                        onTargetSelected = { activeTargetId = it }
                                    )
                                    AppTab.PROFILE -> ProfileScreen(
                                        currentUserId = userId,
                                        repository = repository,
                                        userPrefs = userPrefs,
                                        onUserIdSaved = { newId ->
                                            userPrefs.saveUserId(newId)
                                            currentUserId = newId
                                        },
                                        onThemeChanged = { themeMode = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ConnectionStatusIndicator(count: Int) {
        val isOnline = count > 0
        Surface(
            color = if (isOnline) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.padding(end = 16.dp),
            border = if (!isOnline) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)) else null
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) "$count Online" else "Offline",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
