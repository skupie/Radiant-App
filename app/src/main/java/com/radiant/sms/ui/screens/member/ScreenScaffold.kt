package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
 * ScreenScaffold:
 * - No extra scaffold padding/blank space
 * - Content is kept out of status bar / camera notch (safeDrawing)
 * - Hamburger menu always available
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    nav: NavController,
    title: String = "",
    hideTitle: Boolean = true,
    showHamburger: Boolean = true,
    showBack: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
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

        // ✅ Remove automatic insets that create blank space
        contentWindowInsets = WindowInsets(0, 0, 0, 0),

        topBar = {
            TopAppBar(
                // ✅ Prevent TopAppBar from inflating due to default insets
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
    ) { innerPadding ->
        // ✅ Apply safe area padding ONLY to content (not extra top blank space)
        // safeDrawing handles camera/notch + status bar + nav bar safely
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            content(innerPadding)
        }
    }
}
