package com.example.resqmesh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.resqmesh.data.local.UserPreferences
import com.example.resqmesh.data.repository.MessageRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    currentUserId: String?,
    repository: MessageRepository,
    userPrefs: UserPreferences,
    onUserIdSaved: (String) -> Unit,
    onThemeChanged: (UserPreferences.ThemeMode) -> Unit
) {
    var input by remember { mutableStateOf(currentUserId ?: "") }
    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOfflineOption by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentUserId == null) {
            OnboardingSection(
                input = input,
                isChecking = isChecking,
                errorMessage = errorMessage,
                onInputChange = { input = it; errorMessage = null },
                onSave = { attemptSave(input, currentUserId, repository, coroutineScope, { isChecking = it }, { errorMessage = it }, { showOfflineOption = it }, onUserIdSaved) }
            )
        } else {
            ProfileSettingsSection(
                input = input,
                isChecking = isChecking,
                errorMessage = errorMessage,
                userPrefs = userPrefs,
                onInputChange = { input = it; errorMessage = null },
                onSave = { attemptSave(input, currentUserId, repository, coroutineScope, { isChecking = it }, { errorMessage = it }, { showOfflineOption = it }, onUserIdSaved) },
                onThemeChanged = onThemeChanged
            )
        }
    }
}

@Composable
fun OnboardingSection(
    input: String,
    isChecking: Boolean,
    errorMessage: String?,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.Wifi,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "Welcome to ResQMesh",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Set up your unique ID to start communicating in the emergency network.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(32.dp))

    AccountCard(
        input = input,
        isChecking = isChecking,
        errorMessage = errorMessage,
        onInputChange = onInputChange,
        onSave = onSave,
        buttonText = "Get Started"
    )
}

@Composable
fun ProfileSettingsSection(
    input: String,
    isChecking: Boolean,
    errorMessage: String?,
    userPrefs: UserPreferences,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onThemeChanged: (UserPreferences.ThemeMode) -> Unit
) {
    SectionHeader(title = "Account", icon = Icons.Default.AccountCircle)
    AccountCard(
        input = input,
        isChecking = isChecking,
        errorMessage = errorMessage,
        onInputChange = onInputChange,
        onSave = onSave,
        buttonText = "Update ID"
    )

    Spacer(modifier = Modifier.height(24.dp))

    SectionHeader(title = "Appearance", icon = Icons.Default.Palette)
    AppearanceCard(userPrefs = userPrefs, onThemeChanged = onThemeChanged)

    Spacer(modifier = Modifier.height(24.dp))

    SectionHeader(title = "Information", icon = Icons.Default.Info)
    AboutCard()
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AccountCard(
    input: String,
    isChecking: Boolean,
    errorMessage: String?,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    buttonText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                label = { Text("Unique User ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null,
                supportingText = {
                    Text(text = errorMessage ?: "3-20 characters, letters/numbers only")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSave,
                enabled = !isChecking && input.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isChecking) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
fun AppearanceCard(
    userPrefs: UserPreferences,
    onThemeChanged: (UserPreferences.ThemeMode) -> Unit
) {
    val currentTheme = userPrefs.getThemeMode()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ThemeOption("System Default", currentTheme == UserPreferences.ThemeMode.SYSTEM) {
                userPrefs.saveThemeMode(UserPreferences.ThemeMode.SYSTEM)
                onThemeChanged(UserPreferences.ThemeMode.SYSTEM)
            }
            ThemeOption("Light", currentTheme == UserPreferences.ThemeMode.LIGHT) {
                userPrefs.saveThemeMode(UserPreferences.ThemeMode.LIGHT)
                onThemeChanged(UserPreferences.ThemeMode.LIGHT)
            }
            ThemeOption("Dark", currentTheme == UserPreferences.ThemeMode.DARK) {
                userPrefs.saveThemeMode(UserPreferences.ThemeMode.DARK)
                onThemeChanged(UserPreferences.ThemeMode.DARK)
            }
        }
    }
}

@Composable
fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ResQMesh v1.0.0", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = "Offline-first emergency communication network.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private val idRegex = Regex("^[a-zA-Z0-9_]{3,20}$")

private fun attemptSave(
    input: String,
    currentUserId: String?,
    repository: MessageRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    setChecking: (Boolean) -> Unit,
    setError: (String?) -> Unit,
    setShowOffline: (Boolean) -> Unit,
    onSuccess: (String) -> Unit
) {
    val trimmed = input.trim()
    setError(null)
    setShowOffline(false)

    if (!idRegex.matches(trimmed)) {
        setError("Invalid ID format.")
        return
    }
    if (trimmed == currentUserId) {
        onSuccess(trimmed)
        return
    }

    setChecking(true)
    scope.launch {
        try {
            val available = repository.isUserIdAvailable(trimmed)
            if (available) {
                if (repository.registerUser(trimmed)) {
                    onSuccess(trimmed)
                } else {
                    setError("Registration failed.")
                    setShowOffline(true)
                }
            } else {
                setError("ID already taken.")
                setShowOffline(true)
            }
        } catch (e: Exception) {
            setError("Connection error.")
            setShowOffline(true)
        } finally {
            setChecking(false)
        }
    }
}
