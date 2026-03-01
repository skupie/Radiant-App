package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawelaunch
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.TokenStore
import com.radiant.sms.ui.Routes
import kotlinx.coroutines.launch

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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun logout() {
        tokenStore.clear()
        nav.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun closeDrawerThen(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showHamburger,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    label = { Text("Ledger") },
                    selected = false,
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    onClick = {
                        closeDrawerThen {
                            nav.navigate(Routes.MEMBER_LEDGER) { launchSingleTop = true }
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Due Summary") },
                    selected = false,
                    icon = { Icon(Icons.Filled.Assignment, contentDescription = null) },
                    onClick = {
                        closeDrawerThen {
                            nav.navigate(Routes.MEMBER_DUE_SUMMARY) { launchSingleTop = true }
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Share Details") },
                    selected = false,
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    onClick = {
                        closeDrawerThen {
                            nav.navigate(Routes.MEMBER_SHARE_DETAILS) { launchSingleTop = true }
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    onClick = {
                        closeDrawerThen {
                            nav.navigate(Routes.MEMBER_PROFILE) { launchSingleTop = true }
                        }
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            logout()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { if (!hideTitle) Text(title) },
                    navigationIcon = {
                        if (showHamburger) {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    }
                )
            }
        ) {
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
}
