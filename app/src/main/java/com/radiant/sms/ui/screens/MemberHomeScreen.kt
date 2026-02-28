package com.radiant.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.storage.TokenStore
import com.radiant.sms.ui.Routes

@Composable
fun MemberHomeScreen(nav: NavController) {
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Member Dashboard", style = MaterialTheme.typography.headlineSmall)

        Button(onClick = { nav.navigate(Routes.MEMBER_PROFILE) }, modifier = Modifier.fillMaxWidth()) {
            Text("Profile")
        }
        Button(onClick = { nav.navigate(Routes.MEMBER_LEDGER) }, modifier = Modifier.fillMaxWidth()) {
            Text("Ledger")
        }
        Button(onClick = { nav.navigate(Routes.MEMBER_DUE_SUMMARY) }, modifier = Modifier.fillMaxWidth()) {
            Text("Due Summary")
        }
        Button(onClick = { nav.navigate(Routes.MEMBER_SHARE_DETAILS) }, modifier = Modifier.fillMaxWidth()) {
            Text("Share Details")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                TokenStore(ctx).clear()
                nav.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}
