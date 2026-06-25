package com.arcora.presentation.subscriptions

import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.repository.Subscription
import com.arcora.domain.repository.SubscriptionRepository
import com.arcora.domain.repository.UserProfile
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: SubscriptionsViewModel

    private val fakeSubscriptions = listOf(
        Subscription("s1", "u1", null, "a1", "15.00", "USDC", "monthly", "PENDING", "2026-07-25"),
        Subscription("s2", "u1", "m1", null, "25.00", "USDC", "weekly", "PENDING", "2026-07-01")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        subscriptionRepository = mockk()
        authRepository = mockk()
        coEvery { authRepository.currentUser } returns MutableStateFlow(
            UserProfile("u1", "test@test.com", "@test", "Test", "0x123", 50, true)
        )
        coEvery { subscriptionRepository.list("u1") } returns fakeSubscriptions
        coEvery { subscriptionRepository.create(any(), any(), any(), any(), any()) } returns fakeSubscriptions[0]
        coEvery { subscriptionRepository.pause(any()) } returns fakeSubscriptions[0].copy(status = "REJECTED")
        coEvery { subscriptionRepository.cancel(any()) } returns fakeSubscriptions[0].copy(status = "FAILED")
        viewModel = SubscriptionsViewModel(subscriptionRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads subscriptions`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(2, state.subscriptions.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onAmountChange updates amount`() {
        viewModel.onAmountChange("25.00")
        assertEquals("25.00", viewModel.uiState.value.amount)
    }

    @Test
    fun `onIntervalChange updates interval`() {
        viewModel.onIntervalChange("weekly")
        assertEquals("weekly", viewModel.uiState.value.interval)
    }

    @Test
    fun `createDemoSubscription with invalid amount sets error`() {
        viewModel.onAmountChange("abc")
        viewModel.createDemoSubscription()
        assertNotNull(viewModel.uiState.value.error)
    }
}
