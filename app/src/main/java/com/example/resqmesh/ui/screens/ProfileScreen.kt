package com.example.resqmesh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resqmesh.data.repository.MessageRepository
import kotlinx.coroutines.launch

private val idRegex = Regex("^[a-zA-Z0-9_]{3,20}$")

@Composable
fun ProfileScreen(
    currentUserId: String?,
    repository: MessageRepository,
    onUserIdSaved: (String) -> Unit
) {
    var input by remember { mutableStateOf(currentUserId ?: "") }
    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOfflineOption by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun attemptSave(forceOffline: Boolean = false) {
        val trimmed = input.trim()
        errorMessage = null
        showOfflineOption = false

        if (!idRegex.matches(trimmed)) {
            errorMessage = "ID must be 3-20 characters: letters, numbers, underscore only."
            return
        }
        if (trimmed == currentUserId) {
            onUserIdSaved(trimmed)
            return
        }
        if (forceOffline) {
            onUserIdSaved(trimmed)
            return
        }

        isChecking = true
        coroutineScope.launch {
            val available = repository.isUserIdAvailable(trimmed)
            if (available) {
                val registered = repository.registerUser(trimmed)
                isChecking = false
                if (registered) {
                    onUserIdSaved(trimmed)
                } else {
                    errorMessage = "Couldn't reach the server to register this ID."
                    showOfflineOption = true
                }
            } else {
                isChecking = false
                errorMessage = "That ID is taken, or we couldn't verify it (check your connection)."
                showOfflineOption = true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (currentUserId == null) "Choose your ID" else "Edit your ID",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This is how other people will find and message you. Choose something memorable.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it; errorMessage = null },
            label = { Text("User ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { attemptSave() },
            enabled = !isChecking,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isChecking) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(if (currentUserId == null) "Get Started" else "Save")
            }
        }

        if (showOfflineOption) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { attemptSave(forceOffline = true) }, modifier = Modifier.fillMaxWidth()) {
                Text("Continue offline (sync later)")
            }
        }
    }
}