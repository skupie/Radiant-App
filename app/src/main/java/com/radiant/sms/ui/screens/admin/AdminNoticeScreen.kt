package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AdminNoticeScreen(nav: NavController) {

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AdminScaffold(
        nav = nav,
        title = "Send Notice",
        hideTitle = false
    ) {

        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notice Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Notice Message") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // send notice to backend
                }
            ) {
                Text("Send Notice")
            }

        }
    }
}
