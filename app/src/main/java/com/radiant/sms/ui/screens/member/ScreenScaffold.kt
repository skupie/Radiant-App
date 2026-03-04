package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Screen scaffold used by MEMBER screens.
 * This restores the missing ScreenScaffold reference and fixes compilation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    nav: NavController,
    title: String = "",
    showMenu: Boolean = true,
    onMenuClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        // TopAppBar already handles system insets; avoid extra top gaps
        contentWindowInsets = TopAppBarDefaults.windowInsets,
        topBar = {
            TopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    if (showMenu) {
                        IconButton(onClick = { (onMenuClick ?: { nav.popBackStack() })() }) {
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
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            content()
        }
    }
}
