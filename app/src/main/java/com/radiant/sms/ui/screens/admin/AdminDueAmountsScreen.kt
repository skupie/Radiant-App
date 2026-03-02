package com.radiant.sms.ui.screens.admin

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun AdminDueAmountsScreen(nav: NavController) {
    AdminScaffold(nav = nav) {
        Text("Due Amounts (admin) - connect UI as needed")
    }
}
