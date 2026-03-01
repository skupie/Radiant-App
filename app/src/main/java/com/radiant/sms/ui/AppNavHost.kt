package com.radiant.sms.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.radiant.sms.ui.screens.AdminHomeScreen
import com.radiant.sms.ui.screens.LoginScreen
import com.radiant.sms.ui.screens.MemberHomeScreen
import com.radiant.sms.ui.screens.SplashScreen
import com.radiant.sms.ui.screens.member.MemberDueSummaryScreen
import com.radiant.sms.ui.screens.member.MemberLedgerScreen
import com.radiant.sms.ui.screens.member.MemberProfileScreen
import com.radiant.sms.ui.screens.member.MemberShareDetailsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController) }

        composable(Routes.ADMIN_HOME) { AdminHomeScreen(navController) }
        composable(Routes.MEMBER_HOME) { MemberHomeScreen(navController) }

        composable(Routes.MEMBER_PROFILE) { MemberProfileScreen(navController) }
        composable(Routes.MEMBER_LEDGER) { MemberLedgerScreen(navController) }
        composable(Routes.MEMBER_DUE_SUMMARY) { MemberDueSummaryScreen(navController) }
        composable(Routes.MEMBER_SHARE_DETAILS) { MemberShareDetailsScreen(navController) }
    }
}
