package com.arcora.presentation.agentwallets

import com.arcora.domain.repository.AgentWallet
import com.arcora.domain.repository.AgentWalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class AgentWalletsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AgentWalletRepository
    private lateinit var viewModel: AgentWalletsViewModel

    private val fakeWallets = listOf(
        AgentWallet("w1", "owner1", "Research Bot", "Does research", "0x123", "100.00", listOf("read"), "2026-01-01", "2026-01-01"),
        AgentWallet("w2", "owner1", "Dev Runner", "Runs tools", "0x456", "250.00", listOf("read", "write"), "2026-01-01", "2026-01-01")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.list() } returns fakeWallets
        coEvery { repository.create(any(), any(), any(), any()) } returns fakeWallets[0]
        coEvery { repository.delete(any()) } returns Unit
        viewModel = AgentWalletsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads wallets`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(2, state.wallets.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onNameChange updates name`() {
        viewModel.onNameChange("New Agent")
        assertEquals("New Agent", viewModel.uiState.value.name)
    }

    @Test
    fun `createWallet with empty name sets error`() {
        viewModel.onNameChange("")
        viewModel.createWallet()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `createWallet with valid data calls repository`() = runTest {
        viewModel.onNameChange("Test Agent")
        viewModel.onBudgetChange("50.00")
        viewModel.createWallet()
        advanceUntilIdle()
        coVerify { repository.create("Test Agent", any(), "50.00", any()) }
    }

    @Test
    fun `deleteWallet calls repository`() = runTest {
        viewModel.deleteWallet("w1")
        advanceUntilIdle()
        coVerify { repository.delete("w1") }
    }
}
