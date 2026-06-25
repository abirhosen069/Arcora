package com.arcora.presentation.bridge

import com.arcora.domain.model.BridgeQuote
import com.arcora.domain.model.Money
import com.arcora.domain.usecase.BridgeToArcUseCase
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
class BridgeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var bridgeToArc: BridgeToArcUseCase
    private lateinit var viewModel: BridgeViewModel

    private val fakeQuote = BridgeQuote(
        sourceChain = "Base Sepolia",
        amount = Money.usdc("100"),
        estimatedTime = "2-5 minutes",
        routeSummary = "Base Sepolia USDC → CCTP → Arc Testnet USDC"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bridgeToArc = mockk()
        coEvery { bridgeToArc.quote(any(), any()) } returns fakeQuote
        viewModel = BridgeViewModel(bridgeToArc)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value
        assertEquals("Base Sepolia", state.sourceChain)
        assertEquals("", state.amount)
        assertNull(state.quote)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onSourceChainChange updates source chain`() {
        viewModel.onSourceChainChange("Ethereum Sepolia")
        assertEquals("Ethereum Sepolia", viewModel.uiState.value.sourceChain)
    }

    @Test
    fun `onAmountChange filters non-numeric characters`() {
        viewModel.onAmountChange("abc100.50xyz")
        assertEquals("100.50", viewModel.uiState.value.amount)
    }

    @Test
    fun `previewRoute with valid amount fetches quote`() = runTest {
        viewModel.onAmountChange("100")
        viewModel.previewRoute()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.quote)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `previewRoute with invalid amount sets error`() = runTest {
        viewModel.onAmountChange("abc")
        viewModel.previewRoute()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `previewRoute error sets error state`() = runTest {
        coEvery { bridgeToArc.quote(any(), any()) } throws RuntimeException("Quote failed")
        viewModel.onAmountChange("100")
        viewModel.previewRoute()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }
}
