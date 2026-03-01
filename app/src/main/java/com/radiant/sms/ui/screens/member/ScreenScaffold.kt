package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ScreenScaffold(
    title: String,
    nav: NavController,
    showBack: Boolean = true,
    centerTitle: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (centerTitle) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (showBack) {
                    OutlinedButton(
                        modifier = Modifier.align(Alignment.CenterStart),
                        onClick = { nav.popBackStack() }
                    ) {
                        Text("Back")
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 56.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    OutlinedButton(onClick = { nav.popBackStack() }) {
                        Text("Back")
                    }
                    Spacer(Modifier.width(12.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        content()
    }
}
