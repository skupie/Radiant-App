package com.radiant.sms.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.radiant.sms.data.TokenStore

/**
 * ScreenScaffold
 *
 * - showBack: shows back arrow (disabled for hamburger usage)
 * - showHamburger: shows hamburger icon
 * - hideTitle: hides the title text completely (requirement #2)
 *
 * Menu Items:
 *  - Ledger
 *  - Due Summary
 *  - Share Details
 *  - Profile
 *  - Logout (clears TokenStore and navigates to login clearing backstack)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String = "",
    nav: NavController,
    showBack: Boolean = false,
    showHamburger: Boolean = true,
    hideTitle: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    var menuExpanded by remember { mutableStateOf(false) }

    // ---- CHANGE THESE ROUTES IF YOUR NAV GRAPH USES DIFFERENT ONES ----
    val routeLedger = "ledger"
    val routeDueSummary = "due_summary"
    val routeShareDetails = "share_details"
    val routeProfile = "profile"
    val routeLogin = "login"
    // ------------------------------------------------------------------

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (!hideTitle) {
                        Text(text = title)
                    }
                },
                navigationIcon = {
                    when {
                        showHamburger -> {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ledger") },
                                    onClick = {
                                        menuExpanded = false
                                        nav.navigate(routeLedger) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Due Summary") },
                                    onClick = {
                                        menuExpanded = false
                                        nav.navigate(routeDueSummary) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share Details") },
                                    onClick = {
                                        menuExpanded = false
                                        nav.navigate(routeShareDetails) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    onClick = {
                                        menuExpanded = false
                                        nav.navigate(routeProfile) {
                                            launchSingleTop = true
                                        }
                                    }
                                )

                                Divider()

                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        menuExpanded = false

                                        // Clear token/session
                                        tokenStore.clearTokenSync()

                                        // Navigate to login and clear back stack
                                        nav.navigate(routeLogin) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }

                        showBack -> {
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        content(padding)
    }
}
