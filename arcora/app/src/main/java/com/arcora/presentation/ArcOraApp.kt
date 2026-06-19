package com.arcora.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arcora.presentation.activity.ActivityScreen
import com.arcora.presentation.assistant.AssistantScreen
import com.arcora.presentation.bridge.BridgeScreen
import com.arcora.presentation.dashboard.DashboardScreen
import com.arcora.presentation.agents.AgentMarketplaceScreen
import com.arcora.presentation.merchant.MerchantDashboardScreen
import com.arcora.presentation.navigation.ArcOraRoute
import com.arcora.presentation.onboarding.OnboardingScreen
import com.arcora.presentation.payments.SendPaymentScreen
import com.arcora.presentation.receive.ReceiveScreen
import com.arcora.presentation.session.SessionUiState
import com.arcora.presentation.session.SessionViewModel
import com.arcora.presentation.subscriptions.SubscriptionsScreen
import com.arcora.presentation.components.ArcOraLoadingScreen

@Composable
fun ArcOraApp(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    when (val state = sessionState) {
        SessionUiState.Loading -> ArcOraLoadingScreen(message = "Connecting to ArcOra…")
        is SessionUiState.Ready -> ArcOraNavHost(startAtDashboard = state.hasSession)
    }
}

@Composable
private fun ArcOraNavHost(startAtDashboard: Boolean) {
    val navController = rememberNavController()
    val startDestination = if (startAtDashboard) ArcOraRoute.Dashboard.route else ArcOraRoute.Onboarding.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ArcOraRoute.Onboarding.route) {
            OnboardingScreen(onWalletReady = {
                navController.navigate(ArcOraRoute.Dashboard.route) {
                    popUpTo(ArcOraRoute.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(ArcOraRoute.Dashboard.route) {
            DashboardScreen(
                onSend = { navController.navigate(ArcOraRoute.Send.route) },
                onReceive = { navController.navigate(ArcOraRoute.Receive.route) },
                onBridge = { navController.navigate(ArcOraRoute.Bridge.route) },
                onActivity = { navController.navigate(ArcOraRoute.Activity.route) },
                onAssistant = { navController.navigate(ArcOraRoute.Assistant.route) },
                onMerchant = { navController.navigate(ArcOraRoute.Merchant.route) },
                onSubscriptions = { navController.navigate(ArcOraRoute.Subscriptions.route) },
                onAgents = { navController.navigate(ArcOraRoute.Agents.route) }
            )
        }
        composable(ArcOraRoute.Send.route) { SendPaymentScreen(onDone = { navController.popBackStack() }) }
        composable(ArcOraRoute.Receive.route) { ReceiveScreen(onDone = { navController.popBackStack() }) }
        composable(ArcOraRoute.Bridge.route) { BridgeScreen(onDone = { navController.popBackStack() }) }
        composable(ArcOraRoute.Activity.route) { ActivityScreen(onBack = { navController.popBackStack() }) }
        composable(ArcOraRoute.Assistant.route) { AssistantScreen(onDone = { navController.popBackStack() }) }
        composable(ArcOraRoute.Merchant.route) { MerchantDashboardScreen(onDone = { navController.popBackStack() }) }
        composable(ArcOraRoute.Subscriptions.route) { SubscriptionsScreen(onDone = { navController.popBackStack() }) }
        composable(ArcOraRoute.Agents.route) { AgentMarketplaceScreen(onDone = { navController.popBackStack() }) }
    }
}
