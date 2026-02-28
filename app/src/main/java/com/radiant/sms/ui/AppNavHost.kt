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
import com.radiant.sms.ui.screens.member.MemberDueSummaryScreen
import com.radiant.sms.ui.screens.member.MemberLedgerScreen
import com.radiant.sms.ui.screens.member.MemberProfileScreen
import com.radiant.sms.ui.screens.member.MemberShareDetailsScreen

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val ctx = LocalContext.current

    NavHost(navController = nav, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen(nav) }
        composable(Routes.LOGIN) { LoginScreen(nav) }
        composable(Routes.MEMBER_HOME) { MemberHomeScreen(nav) }
        composable(Routes.ADMIN_HOME) { AdminHomeScreen(nav) }

        // ✅ New member feature screens
        composable(Routes.MEMBER_PROFILE) { MemberProfileScreen(nav) }
        composable(Routes.MEMBER_LEDGER) { MemberLedgerScreen(nav) }
        composable(Routes.MEMBER_DUE_SUMMARY) { MemberDueSummaryScreen(nav) }
        composable(Routes.MEMBER_SHARE_DETAILS) { MemberShareDetailsScreen(nav) }
    }
}
