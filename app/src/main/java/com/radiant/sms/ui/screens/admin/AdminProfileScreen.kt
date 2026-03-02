package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.network.NetworkModule

@Composable
fun AdminProfileScreen(nav: NavController) {
    val context = LocalContext.current
    val api = NetworkModule.api(context)

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Admin") }

    LaunchedEffect(Unit) {
        try {
            // Assumes your existing ApiService has getMe()
            val me = api.getMe()
            name = me.name ?: ""
            email = me.email ?: ""
            // If your backend returns role, keep it. Otherwise, this stays "Admin".
            role = me.role ?: "Admin"
        } catch (_: Exception) {
            // keep empty
        }
    }

    AdminScaffold(nav = nav) {
        Column(Modifier.padding(16.dp)) {
            Text("Profile", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            Text("Name: ${if (name.isBlank()) "-" else name}")
            Spacer(Modifier.height(8.dp))

            Text("Email: ${if (email.isBlank()) "-" else email}")
            Spacer(Modifier.height(8.dp))

            Text("Role: ${if (role.isBlank()) "Admin" else role}")
        }
    }
}
