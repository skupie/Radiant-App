package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.TokenStore
import com.radiant.sms.ui.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    nav: NavController,
    title: String = "",
    hideTitle: Boolean = true,
    showHamburger: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    var expanded by remember { mutableStateOf(false) }

    fun logout() {
        tokenStore.clear()
        nav.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        // IMPORTANT: disable default scaffold insets (prevents extra top padding)
        contentWindowInsets = WindowInsets(0, 0, 0, 0),

        topBar = {
            TopAppBar(
                // Disable topbar's own automatic insets too
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { if (!hideTitle) Text(title) },
                navigationIcon = {
                    if (showHamburger) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ledger") },
                                onClick = {
                                    expanded = false
                                    nav.navigate(Routes.MEMBER_LEDGER) { launchSingleTop = true }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Due Summary") },
                                onClick = {
                                    expanded = false
                                    nav.navigate(Routes.MEMBER_DUE_SUMMARY) { launchSingleTop = true }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Details") },
                                onClick = {
                                    expanded = false
                                    nav.navigate(Routes.MEMBER_SHARE_DETAILS) { launchSingleTop = true }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = {
                                    expanded = false
                                    nav.navigate(Routes.MEMBER_PROFILE) { launchSingleTop = true }
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    expanded = false
                                    logout()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) {
        // Safe inset only for status bar (no camera overlap)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}
