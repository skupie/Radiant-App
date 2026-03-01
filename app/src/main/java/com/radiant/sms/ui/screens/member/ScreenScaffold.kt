package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        // 🔥 Disable automatic insets completely
        contentWindowInsets = WindowInsets(0, 0, 0, 0),

        topBar = {
            TopAppBar(
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
                                    nav.navigate(Routes.MEMBER_LEDGER)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Due Summary") },
                                onClick = {
                                    expanded = false
                                    nav.navigate(Routes.MEMBER_DUE_SUMMARY)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Details") },
                                onClick = {
                                    expanded = false
                                    nav.navigate(Routes.MEMBER_SHARE_DETAILS)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = {
                                    expanded = false
                                    nav.navigate(Routes.MEMBER_PROFILE)
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
        // 🔥 THIS is the clean layout rule:
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()  // safe for notch
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}
