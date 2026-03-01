package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

/**
 * Member Screen Scaffold (used by ALL member pages)
 *
 * Changes:
 * 1) Back button removed and replaced with Hamburger
 * 2) Title hidden (no "Member Name" text beside hamburger)
 * 3) Menu:
 *    - Ledger
 *    - Due Summary
 *    - Share Details
 *    - Profile
 * 4) Logout:
 *    - tokenStore.clear()
 *    - navigate to login + clear backstack
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
            popUpTo(nav.graph.startDestinationId) {
                saveState = true
            }
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
        topBar = {
            TopAppBar(
                title = {
                    if (!hideTitle) Text(title)
                },
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
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        // give all screens the same nice spacing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            content(padding)
        }
    }
}
