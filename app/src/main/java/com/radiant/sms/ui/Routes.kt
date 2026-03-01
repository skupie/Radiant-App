package com.radiant.sms.ui

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MEMBER_HOME = "member_home"
    const val ADMIN_HOME = "admin_home"

    // Member feature routes (require memberId)
    const val MEMBER_PROFILE = "member_profile/{memberId}"
    const val MEMBER_LEDGER = "member_ledger/{memberId}"
    const val MEMBER_DUE_SUMMARY = "member_due_summary/{memberId}"
    const val MEMBER_SHARE_DETAILS = "member_share_details/{memberId}"

    fun memberProfile(memberId: Int) = "member_profile/$memberId"
    fun memberLedger(memberId: Int) = "member_ledger/$memberId"
    fun memberDueSummary(memberId: Int) = "member_due_summary/$memberId"
    fun memberShareDetails(memberId: Int) = "member_share_details/$memberId"
}
