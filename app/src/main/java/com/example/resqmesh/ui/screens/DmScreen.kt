package com.example.resqmesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.resqmesh.ui.viewmodels.MeshViewModel
import kotlinx.coroutines.launch

@Composable
fun DmScreen(
    viewModel: MeshViewModel,
    activeTargetId: String?,
    onTargetSelected: (String?) -> Unit
) {
    var targetIdInput by remember { mutableStateOf("") }
    val contacts by viewModel.contacts.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (activeTargetId == null) {
            DmListHeader(
                inputValue = targetIdInput,
                onValueChange = { targetIdInput = it },
                onOpen = {
                    val trimmed = targetIdInput.trim()
                    if (trimmed.isNotBlank()) onTargetSelected(trimmed)
                }
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Active Conversations",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(contacts) { contactId ->
                    ContactListItem(contactId = contactId) {
                        onTargetSelected(contactId)
                    }
                }
            }
        } else {
            ChatConversation(
                targetId = activeTargetId,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun DmListHeader(inputValue: String, onValueChange: (String) -> Unit, onOpen: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by User ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true,
                trailingIcon = {
                    if (inputValue.isNotBlank()) {
                        TextButton(onClick = onOpen) { Text("Chat") }
                    }
                }
            )
        }
    }
}

@Composable
fun ContactListItem(contactId: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = contactId.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(contactId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Tap to message", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ChatConversation(targetId: String, viewModel: MeshViewModel) {
    val messages by viewModel.getConversation(targetId).collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var messageInput by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            reverseLayout = true,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp) // Tighter spacing for same-sender logic
        ) {
            items(messages.size) { index ->
                val msg = messages[index]
                val prevMsg = if (index < messages.size - 1) messages[index + 1] else null
                
                val isMine = msg.senderId == viewModel.myUserId
                val isLastInGroup = prevMsg == null || prevMsg.senderId != msg.senderId
                
                // Extra spacing between different senders
                if (isLastInGroup && index < messages.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                ChatBubble(
                    content = msg.content,
                    isMine = isMine,
                    timestamp = msg.timestamp,
                    showTail = isLastInGroup
                )
            }
        }

        ChatInputArea(
            text = messageInput,
            onTextChange = { messageInput = it },
            onSend = {
                viewModel.sendDirectMessage(targetId, messageInput)
                messageInput = ""
            },
            enabled = messageInput.isNotBlank()
        )
    }
}

@Composable
fun ChatBubble(
    content: String,
    isMine: Boolean,
    timestamp: Long,
    showTail: Boolean
) {
    val timeFormat = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
    val timeString = timeFormat.format(java.util.Date(timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else if (showTail) 2.dp else 16.dp,
                bottomEnd = if (isMine) (if (showTail) 2.dp else 16.dp) else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp),
            tonalElevation = if (isMine) 2.dp else 0.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isMine) 
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ChatInputArea(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit, enabled: Boolean) {
    Surface(
        tonalElevation = 8.dp, // Increased elevation for shadow separation
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message...") },
                shape = MaterialTheme.shapes.extraLarge,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                trailingIcon = {
                    IconButton(
                        onClick = onSend, 
                        enabled = enabled,
                        modifier = Modifier.background(
                            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    }
}
