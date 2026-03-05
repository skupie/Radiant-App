package com.radiant.sms.ui

object Routes {

    const val SPLASH = "splash"
    const val LOGIN = "login"

    // MEMBER
    // (Keep MEMBER_HOME mapped to ledger if you want home to open ledger)
    const val MEMBER_HOME = "member_ledger"
    const val MEMBER_LEDGER = "member_ledger"
    const val MEMBER_PROFILE = "member_profile"
    const val MEMBER_DUE_SUMMARY = "member_due_summary"
    const val MEMBER_SHARE_DETAILS = "member_share_details"

    // ADMIN
    const val ADMIN_HOME = "admin_dashboard" // keep this for compatibility
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_DEPOSITS = "admin_deposits"
    const val ADMIN_DUE_AMOUNTS = "admin_due_amounts"
    const val ADMIN_PROFILE = "admin_profile"
    const val ADMIN_PANEL = "admin_panel"
}
