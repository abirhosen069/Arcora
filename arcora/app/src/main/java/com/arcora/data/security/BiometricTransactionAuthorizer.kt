package com.arcora.data.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.arcora.domain.security.ApprovalResult
import com.arcora.domain.security.RiskLevel
import com.arcora.domain.security.TransactionApprovalRequest
import com.arcora.domain.security.TransactionAuthorizer
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class BiometricTransactionAuthorizer @Inject constructor() : TransactionAuthorizer {
    override suspend fun authorize(request: TransactionApprovalRequest): ApprovalResult {
        val activity = ArcOraActivityHolder.currentActivity()
            ?: return ApprovalResult.Failed("ArcOra needs an active screen before it can confirm this transaction.")

        val biometricManager = BiometricManager.from(activity)
        val authenticator = BiometricManager.Authenticators.BIOMETRIC_STRONG
        return when (biometricManager.canAuthenticate(authenticator)) {
            BiometricManager.BIOMETRIC_SUCCESS -> prompt(activity, request, authenticator)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> ApprovalResult.Failed(
                "No biometric credential is enrolled on this device. Add one in Android settings before approving ArcOra transactions."
            )
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> ApprovalResult.Failed(
                "This device does not have biometric hardware for ArcOra transaction approval."
            )
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> ApprovalResult.Failed(
                "Biometric hardware is temporarily unavailable. Try again shortly."
            )
            else -> ApprovalResult.Failed("Biometric transaction approval is not available on this device.")
        }
    }

    private suspend fun prompt(
        activity: FragmentActivity,
        request: TransactionApprovalRequest,
        authenticator: Int
    ): ApprovalResult = suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (continuation.isActive) {
                        continuation.resume(ApprovalResult.Approved)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (!continuation.isActive) return
                    val result = when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> ApprovalResult.Rejected
                        else -> ApprovalResult.Failed(errString.toString().ifBlank { "Biometric approval failed." })
                    }
                    continuation.resume(result)
                }

                override fun onAuthenticationFailed() {
                    // Keep the prompt open. Android will call onAuthenticationError after too many attempts.
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(request.title)
            .setSubtitle(request.subtitle)
            .setDescription(descriptionFor(request))
            .setAllowedAuthenticators(authenticator)
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(true)
            .build()

        continuation.invokeOnCancellation { prompt.cancelAuthentication() }
        prompt.authenticate(promptInfo)
    }

    private fun descriptionFor(request: TransactionApprovalRequest): String {
        val risk = when (request.riskLevel) {
            RiskLevel.Low -> "Low risk"
            RiskLevel.Medium -> "Medium risk"
            RiskLevel.High -> "High risk"
        }
        return "Approve ${request.amountLabel} with device biometrics. ArcOra risk level: $risk."
    }
}

object ArcOraActivityHolder {
    private var currentActivityRef: WeakReference<FragmentActivity>? = null

    fun setCurrentActivity(activity: FragmentActivity) {
        currentActivityRef = WeakReference(activity)
    }

    fun clearCurrentActivity(activity: FragmentActivity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef = null
        }
    }

    fun currentActivity(): FragmentActivity? = currentActivityRef?.get()
}
