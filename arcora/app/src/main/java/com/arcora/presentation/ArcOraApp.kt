package com.arcora.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arcora.presentation.activity.ActivityScreen
import com.arcora.presentation.agentwallets.AgentWalletsScreen
import com.arcora.presentation.assistant.AssistantScreen
import com.arcora.presentation.bridge.BridgeScreen
import com.arcora.presentation.dashboard.DashboardScreen
import com.arcora.presentation.agents.AgentMarketplaceScreen
import com.arcora.presentation.merchant.MerchantDashboardScreen
import com.arcora.presentation.navigation.ArcOraRoute
import com.arcora.presentation.onboarding.LoginScreen
import com.arcora.presentation.onboarding.OnboardingScreen
import com.arcora.presentation.onboarding.OtpScreen
import com.arcora.presentation.onboarding.RegisterScreen
import com.arcora.presentation.payments.SendPaymentScreen
import com.arcora.presentation.receive.ReceiveScreen
import com.arcora.presentation.reputation.ReputationScreen
import com.arcora.presentation.settings.SettingsScreen
import com.arcora.presentation.session.SessionUiState
import com.arcora.presentation.session.SessionViewModel
import com.arcora.presentation.subscriptions.SubscriptionsScreen
import com.arcora.presentation.components.ArcOraLoadingScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun ArcOraApp(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    when (val state = sessionState) {
        SessionUiState.Loading -> ArcOraLoadingScreen(message = "Connecting to ArcOra…")
        is SessionUiState.Ready -> ArcOraNavHost(startAtDashboard = state.hasSession)
    }
}

private fun String.urlEncode() = URLEncoder.encode(this, "UTF-8")
private fun String.urlDecode() = URLDecoder.decode(this, "UTF-8")

@Composable
private fun ArcOraNavHost(startAtDashboard: Boolean) {
    val navController = rememberNavController()
    val startDestination = if (startAtDashboard) ArcOraRoute.Dashboard.route else ArcOraRoute.Onboarding.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ArcOraRoute.Onboarding.route) {
            OnboardingScreen(
                onWalletReady = {
                    navController.navigate(ArcOraRoute.Dashboard.route) {
                        popUpTo(ArcOraRoute.Onboarding.route) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(ArcOraRoute.Register.route) },
                onGoToLogin = { navController.navigate(ArcOraRoute.Login.route) }
            )
        }

        composable(ArcOraRoute.Register.route) {
            RegisterScreen(
                onOtpNeeded = { email, passwordHash, displayName, username ->
                    navController.navigate(
                        "otp/${email.urlEncode()}/${passwordHash.urlEncode()}/${displayName.urlEncode()}/${username.urlEncode()}"
                    ) {
                        popUpTo(ArcOraRoute.Register.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ArcOraRoute.Otp.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("passwordHash") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")?.urlDecode() ?: ""
            val passwordHash = backStackEntry.arguments?.getString("passwordHash")?.urlDecode() ?: ""
            val displayName = backStackEntry.arguments?.getString("displayName")?.urlDecode() ?: ""
            val username = backStackEntry.arguments?.getString("username")?.urlDecode() ?: ""

            OtpScreen(
                email = email,
                passwordHash = passwordHash,
                displayName = displayName,
                username = username,
                onWalletReady = {
                    navController.navigate(ArcOraRoute.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(ArcOraRoute.Login.route) {
            LoginScreen(
                onWalletReady = {
                    navController.navigate(ArcOraRoute.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
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
                onAgents = { navController.navigate(ArcOraRoute.Agents.route) },
                onAgentWallets = { navController.navigate(ArcOraRoute.AgentWallets.route) },
                onReputation = { navController.navigate(ArcOraRoute.Reputation.route) },
                onSettings = { navController.navigate(ArcOraRoute.Settings.route) }
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
        composable(ArcOraRoute.AgentWallets.route) { AgentWalletsScreen(onDone = { navController.popBackStack() }) }
        composable(ArcOraRoute.Reputation.route) { ReputationScreen(onDone = { navController.popBackStack() }) }
        composable(ArcOraRoute.Settings.route) {
            SettingsScreen(
                onDone = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(ArcOraRoute.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
