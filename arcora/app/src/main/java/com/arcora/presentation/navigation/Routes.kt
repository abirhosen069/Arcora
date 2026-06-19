package com.arcora.presentation.navigation

sealed class ArcOraRoute(val route: String) {
    data object Onboarding : ArcOraRoute("onboarding")
    data object Dashboard : ArcOraRoute("dashboard")
    data object Send : ArcOraRoute("send")
    data object Receive : ArcOraRoute("receive")
    data object Bridge : ArcOraRoute("bridge")
    data object Activity : ArcOraRoute("activity")
    data object Assistant : ArcOraRoute("assistant")
    data object Merchant : ArcOraRoute("merchant")
    data object Subscriptions : ArcOraRoute("subscriptions")
    data object Agents : ArcOraRoute("agents")
}
