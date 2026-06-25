package com.arcora.presentation.reputation

import com.arcora.domain.repository.LeaderboardEntry
import com.arcora.domain.repository.ReputationProfile
import com.arcora.domain.repository.ReputationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ReputationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ReputationRepository
    private lateinit var viewModel: ReputationViewModel

    private val fakeProfile = ReputationProfile(
        score = 85,
        level = "Gold",
        factors = listOf("5 sends (+10)"),
        sentTransactions = 5,
        receivedTransactions = 3,
        totalVolume = "500",
        agentWallets = 1,
        isVerified = true
    )

    private val fakeLeaderboard = listOf(
        LeaderboardEntry("u1", "@alice", "Alice", 95, true),
        LeaderboardEntry("u2", "@bob", "Bob", 80, false)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.getMyReputation() } returns fakeProfile
        coEvery { repository.getLeaderboard(10) } returns fakeLeaderboard
        viewModel = ReputationViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads reputation and leaderboard`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(85, state.myReputation?.score)
        assertEquals("Gold", state.myReputation?.level)
        assertEquals(2, state.leaderboard.size)
    }

    @Test
    fun `refresh reloads data`() = runTest {
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `error sets error state`() = runTest {
        coEvery { repository.getMyReputation() } throws RuntimeException("Network error")
        val vm = ReputationViewModel(repository)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)
    }
}
