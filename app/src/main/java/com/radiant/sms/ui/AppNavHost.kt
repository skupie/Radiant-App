// app/src/main/java/com/radiant/sms/ui/AppNavHost.kt
package com.radiant.sms.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.radiant.sms.ui.screens.*
import com.radiant.sms.ui.screens.member.*

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) { SplashScreen(nav) }
        composable(Routes.LOGIN) { LoginScreen(nav) }

        composable(Routes.MEMBER_HOME) { MemberHomeScreen(nav) }
        composable(Routes.ADMIN_HOME) { AdminHomeScreen(nav) }

        // ✅ Member feature routes
        composable(Routes.MEMBER_PROFILE) { MemberProfileScreen(nav) }
        composable(Routes.MEMBER_LEDGER) { MemberLedgerScreen(nav) }
        composable(Routes.MEMBER_DUE_SUMMARY) { MemberDueSummaryScreen(nav) }
        composable(Routes.MEMBER_SHARE_DETAILS) { MemberShareDetailsScreen(nav) }
    }
}
