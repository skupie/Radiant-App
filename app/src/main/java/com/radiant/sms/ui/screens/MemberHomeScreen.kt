package com.radiant.sms.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.ui.Routes
import com.radiant.sms.ui.viewmodel.AuthViewModel

@Composable
fun MemberHomeScreen(nav: NavController, vm: AuthViewModel = viewModel()) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Member Dashboard", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = { nav.navigate(Routes.MEMBER_PROFILE) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Profile")
        }

        Button(
            onClick = { nav.navigate(Routes.MEMBER_LEDGER) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ledger")
        }

        Button(
            onClick = { nav.navigate(Routes.MEMBER_DUE_SUMMARY) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Due Summary")
        }

        Button(
            onClick = { nav.navigate(Routes.MEMBER_SHARE_DETAILS) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share Details")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                vm.logout()
                nav.navigate(Routes.LOGIN) {
                    popUpTo(Routes.MEMBER_HOME) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}
