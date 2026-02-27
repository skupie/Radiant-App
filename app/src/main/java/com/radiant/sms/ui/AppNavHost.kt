package com.radiant.sms.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.radiant.sms.ui.screens.AdminHomeScreen
import com.radiant.sms.ui.screens.LoginScreen
import com.radiant.sms.ui.screens.MemberHomeScreen
import com.radiant.sms.ui.screens.SplashScreen

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val ctx = LocalContext.current

    NavHost(navController = nav, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen(nav) }
        composable(Routes.LOGIN) { LoginScreen(nav) }
        composable(Routes.MEMBER_HOME) { MemberHomeScreen(nav) }
        composable(Routes.ADMIN_HOME) { AdminHomeScreen(nav) }
    }
}
