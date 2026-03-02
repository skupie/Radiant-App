package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AdminPanelScreen(nav: NavController) {
    AdminScaffold(nav = nav) {
        Column(Modifier.padding(16.dp)) {
            Text("Admin Panel", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text(
                "This is the admin panel page. You can add actions like manage members, deposits, due amounts, etc.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
