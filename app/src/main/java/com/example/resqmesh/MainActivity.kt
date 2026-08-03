package com.example.resqmesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.resqmesh.data.local.AppDatabase
import com.example.resqmesh.data.local.UserPreferences
import com.example.resqmesh.data.remote.RetrofitClient
import com.example.resqmesh.data.repository.MessageRepository
import com.example.resqmesh.mesh.MeshManager
import com.example.resqmesh.mesh.NetworkMonitor
import com.example.resqmesh.ui.screens.BroadcastScreen
import com.example.resqmesh.ui.viewmodels.MeshViewModel

class MainActivity : ComponentActivity() {

    private lateinit var meshManager: MeshManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize core dependencies
        val userPrefs = UserPreferences(this)
        val myUserId = userPrefs.getOrGenerateUserId()

        val database = AppDatabase.getDatabase(this)
        val repository = MessageRepository(database.messageDao(), RetrofitClient.apiService)
        val networkMonitor = NetworkMonitor(this)

        // 2. Initialize Mesh Manager
        meshManager = MeshManager(this, myUserId, repository, networkMonitor)

        // Note: In a production app, you MUST request runtime permissions
        // (Bluetooth, Location, etc.) here before starting the mesh.
        meshManager.startMesh()

        // 3. Initialize ViewModel
        val viewModel = MeshViewModel(myUserId, repository, meshManager)

        // 4. Set UI
        setContent {
            MaterialTheme {
                Surface {
                    // Displaying Broadcast Screen as default for this template
                    BroadcastScreen(viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ideally, gracefully stop the mesh connections here
    }
}