package com.radiant.sms.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
 * Requirements implemented:
 * 1) Remove back button and show Hamburger at top-left
 * 2) Hide title text beside it
 * 3) Menu options:
 *    - Ledger
 *    - Due Summary
 *    - Share Details
 *    - Profile
 * 4) Logout (clears TokenStore and navigates to Login clearing backstack)
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

    fun navigateTop(route: String) {
        menuExpanded = false
        nav.navigate(route) {
            // prevents multiple same destinations in backstack
            launchSingleTop = true
            restoreState = true

            // keeps your bottom stack clean (single instance behavior)
            val startId = nav.graph.startDestinationId
            popUpTo(startId) {
                saveState = true
            }
        }
    }

    fun logout() {
        menuExpanded = false

        // ✅ correct for your TokenStore
        tokenStore.clear()

        // clear all screens and go to login
        nav.navigate(routeLogin) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

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
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu"
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ledger") },
                                    onClick = { navigateTop(routeLedger) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Due Summary") },
                                    onClick = { navigateTop(routeDueSummary) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share Details") },
                                    onClick = { navigateTop(routeShareDetails) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    onClick = { navigateTop(routeProfile) }
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
