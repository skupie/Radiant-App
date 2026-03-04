package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
fun AdminScaffold(
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
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {

                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = false,
                    icon = { Icon(Icons.Filled.Dashboard, null) },
                    onClick = { closeDrawerThen { nav.navigate(Routes.ADMIN_DASHBOARD) { launchSingleTop = true } } }
                )

                NavigationDrawerItem(
                    label = { Text("Deposits") },
                    selected = false,
                    icon = { Icon(Icons.Filled.AttachMoney, null) },
                    onClick = { closeDrawerThen { nav.navigate(Routes.ADMIN_DEPOSITS) { launchSingleTop = true } } }
                )

                // ✅ Drawer name updated
                NavigationDrawerItem(
                    label = { Text("Due Summary") },
                    selected = false,
                    icon = { Icon(Icons.Filled.Warning, null) },
                    onClick = { closeDrawerThen { nav.navigate(Routes.ADMIN_DUE_AMOUNTS) { launchSingleTop = true } } }
                )

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    icon = { Icon(Icons.Filled.Person, null) },
                    onClick = { closeDrawerThen { nav.navigate(Routes.ADMIN_PROFILE) { launchSingleTop = true } } }
                )

                NavigationDrawerItem(
                    label = { Text("Admin Panel") },
                    selected = false,
                    icon = { Icon(Icons.Filled.AdminPanelSettings, null) },
                    onClick = { closeDrawerThen { nav.navigate(Routes.ADMIN_PANEL) { launchSingleTop = true } } }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    icon = { Icon(Icons.Filled.Logout, null) },
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            logout()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),

            // ✅ IMPORTANT: respect safe area (status/navigation bars) for ALL pages
            contentWindowInsets = WindowInsets.safeDrawing,

            topBar = {
                TopAppBar(
                    // ✅ Top bar already handles status bar insets
                    windowInsets = TopAppBarDefaults.windowInsets,
                    title = { if (!hideTitle) Text(title) },
                    navigationIcon = {
                        if (showHamburger) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // ✅ pushes content below TopAppBar + system insets
                    .padding(innerPadding)
                    // ✅ your consistent page padding like screenshot
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                content()
            }
        }
    }
}
