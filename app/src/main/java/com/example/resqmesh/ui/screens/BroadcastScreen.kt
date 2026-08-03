package com.example.resqmesh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resqmesh.ui.viewmodels.MeshViewModel

@Composable
fun BroadcastScreen(viewModel: MeshViewModel) {
    var inputText by remember { mutableStateOf("") }
    val messages by viewModel.broadcastMessages.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { msg ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "From: ${msg.senderId.take(5)}...", style = MaterialTheme.typography.labelSmall)
                        Text(text = msg.content, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                placeholder = { Text("Emergency Broadcast...") }
            )
            Button(onClick = {
                if (inputText.isNotBlank()) {
                    viewModel.sendBroadcast(inputText)
                    inputText = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}