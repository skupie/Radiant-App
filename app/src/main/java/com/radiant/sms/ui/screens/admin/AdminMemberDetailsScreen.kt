package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AdminMemberDetailsScreen(
    nav: NavController,
    id: Long,
    name: String,
    email: String,
    nid: String,
    share: Int,
    deposits: Int,
    total: Double
) {
    AdminScaffold(nav = nav, title = "Member Details", hideTitle = false, showHamburger = true) {
        Spacer(Modifier.height(12.dp))

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(6.dp))
                Text(text = "Member ID: $id", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Email: $email", style = MaterialTheme.typography.bodyMedium)
                Text(text = "NID: $nid", style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(12.dp))
                Text(text = "Share: $share", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(text = "Deposits count: $deposits", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(text = "Total deposited: ৳ $total", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
    }
}
