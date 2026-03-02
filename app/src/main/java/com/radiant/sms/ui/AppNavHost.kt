package com.radiant.sms.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.radiant.sms.ui.screens.LoginScreen
import com.radiant.sms.ui.screens.MemberHomeScreen
import com.radiant.sms.ui.screens.SplashScreen
import com.radiant.sms.ui.screens.admin.AdminDashboardScreen
import com.radiant.sms.ui.screens.admin.AdminDepositsScreen
import com.radiant.sms.ui.screens.admin.AdminDueAmountsScreen
import com.radiant.sms.ui.screens.admin.AdminPanelScreen
import com.radiant.sms.ui.screens.admin.AdminProfileScreen
import com.radiant.sms.ui.screens.member.MemberDueSummaryScreen
import com.radiant.sms.ui.screens.member.MemberLedgerScreen
import com.radiant.sms.ui.screens.member.MemberProfileScreen
import com.radiant.sms.ui.screens.member.MemberShareDetailsScreen

/**
 * Local route strings (so build won't fail even if Routes.kt is missing constants).
 */
private object Rts {
    const val SPLASH = "splash"
    const val LOGIN = "login"

    const val MEMBER_HOME = "member_home"
    const val MEMBER_PROFILE = "member_profile"
    const val MEMBER_LEDGER = "member_ledger"
    const val MEMBER_DUE_SUMMARY = "member_due_summary"
    const val MEMBER_SHARE_DETAILS = "member_share_details"

    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_DEPOSITS = "admin_deposits"
    const val ADMIN_DUE_AMOUNTS = "admin_due_amounts"
    const val ADMIN_PROFILE = "admin_profile"
    const val ADMIN_PANEL = "admin_panel"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rts.SPLASH
    ) {
        composable(Rts.SPLASH) { SplashScreen(navController) }
        composable(Rts.LOGIN) { LoginScreen(navController) }

        // Member
        composable(Rts.MEMBER_HOME) { MemberHomeScreen(navController) }
        composable(Rts.MEMBER_PROFILE) { MemberProfileScreen(navController) }
        composable(Rts.MEMBER_LEDGER) { MemberLedgerScreen(navController) }
        composable(Rts.MEMBER_DUE_SUMMARY) { MemberDueSummaryScreen(navController) }
        composable(Rts.MEMBER_SHARE_DETAILS) { MemberShareDetailsScreen(navController) }

        // Admin
        composable(Rts.ADMIN_DASHBOARD) { AdminDashboardScreen(navController) }
        composable(Rts.ADMIN_DEPOSITS) { AdminDepositsScreen(navController) }
        composable(Rts.ADMIN_DUE_AMOUNTS) { AdminDueAmountsScreen(navController) }

        // ✅ NOTE: matches the code I gave you earlier:
        // AdminProfileScreen(modifier, onLogout)  and AdminPanelScreen(modifier)
        composable(Rts.ADMIN_PROFILE) {
            AdminProfileScreen(
                onLogout = {
                    navController.navigate(Rts.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Rts.ADMIN_PANEL) {
            AdminPanelScreen()
        }
    }
}
