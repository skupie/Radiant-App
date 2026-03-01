package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.radiant.sms.data.TokenStore
import com.radiant.sms.ui.Routes

/**
 * Full-screen scaffold:
 * - NO automatic insets/padding (status bar, navigation bar, etc.)
 * - Hamburger always visible
 * - Title hidden by default
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    nav: NavController,
    title: String = "",
    hideTitle: Boolean = true,
    showHamburger: Boolean = true,
    showBack: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    var menuExpanded by remember { mutableStateOf(false) }

    fun navigateTop(route: String) {
        menuExpanded = false
        nav.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(nav.graph.startDestinationId) { saveState = true }
        }
    }

    fun logout() {
        menuExpanded = false
        tokenStore.clear()
        nav.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        // ✅ remove ALL scaffold padding (top/bottom/gesture/status/nav)
        contentWindowInsets = WindowInsets(0, 0, 0, 0),

        topBar = {
            TopAppBar(
                // ✅ remove TopAppBar insets too
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { if (!hideTitle) Text(title) },
                navigationIcon = {
                    when {
                        showHamburger -> {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ledger") },
                                    onClick = { navigateTop(Routes.MEMBER_LEDGER) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Due Summary") },
                                    onClick = { navigateTop(Routes.MEMBER_DUE_SUMMARY) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share Details") },
                                    onClick = { navigateTop(Routes.MEMBER_SHARE_DETAILS) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    onClick = { navigateTop(Routes.MEMBER_PROFILE) }
                                )

                                Divider()

                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = { logout() }
                                )
                            }
                        }

                        showBack -> {
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }
                }
            )
        }
    ) {
        // ✅ ignore padding completely
        content()
    }
}
