package com.example.resqmesh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resqmesh.ui.viewmodels.MeshViewModel
import kotlinx.coroutines.launch

@Composable
fun DmScreen(viewModel: MeshViewModel) {
    var targetIdInput by remember { mutableStateOf("") }
    var activeTargetId by remember { mutableStateOf<String?>(null) }
    var messageInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = targetIdInput,
                onValueChange = { targetIdInput = it },
                modifier = Modifier.weight(1f),
                label = { Text("User ID to message") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val trimmed = targetIdInput.trim()
                if (trimmed.isNotBlank()) activeTargetId = trimmed
            }) {
                Text("Open")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val targetId = activeTargetId
        if (targetId == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Enter a user ID above to start a conversation.")
            }
        } else {
            val messages by viewModel.getConversation(targetId).collectAsState(initial = emptyList())
            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }

            Text(
                text = "Chat with $targetId",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages) { msg ->
                    val isMine = msg.senderId == viewModel.myUserId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMine)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(text = msg.content, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    placeholder = { Text("Message...") }
                )
                Button(onClick = {
                    if (messageInput.isNotBlank()) {
                        viewModel.sendDirectMessage(targetId, messageInput)
                        messageInput = ""
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }
}