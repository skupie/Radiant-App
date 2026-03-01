package com.radiant.sms.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.radiant.sms.ui.screens.admin.AdminHomeScreen
import com.radiant.sms.ui.screens.login.LoginScreen
import com.radiant.sms.ui.screens.member.MemberDueSummaryScreen
import com.radiant.sms.ui.screens.member.MemberLedgerScreen
import com.radiant.sms.ui.screens.member.MemberProfileScreen
import com.radiant.sms.ui.screens.member.MemberShareDetailsScreen
import com.radiant.sms.ui.screens.splash.SplashScreen

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
        composable(Routes.MEMBER_HOME) { /* your existing member home screen here */ }

        composable(
            route = Routes.MEMBER_PROFILE,
            arguments = listOf(navArgument("memberId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getInt("memberId") ?: 0
            MemberProfileScreen(navController, memberId)
        }

        composable(
            route = Routes.MEMBER_LEDGER,
            arguments = listOf(navArgument("memberId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getInt("memberId") ?: 0
            MemberLedgerScreen(navController, memberId)
        }

        composable(
            route = Routes.MEMBER_DUE_SUMMARY,
            arguments = listOf(navArgument("memberId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getInt("memberId") ?: 0
            MemberDueSummaryScreen(navController, memberId)
        }

        composable(
            route = Routes.MEMBER_SHARE_DETAILS,
            arguments = listOf(navArgument("memberId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getInt("memberId") ?: 0
            MemberShareDetailsScreen(navController, memberId)
        }
    }
}
