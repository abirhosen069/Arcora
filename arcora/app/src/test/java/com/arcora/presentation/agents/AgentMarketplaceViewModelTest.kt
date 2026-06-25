package com.arcora.presentation.agents

import com.arcora.domain.repository.AgentListing
import com.arcora.domain.repository.AgentMarketplaceRepository
import com.arcora.domain.repository.MarketplaceResult
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
class AgentMarketplaceViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AgentMarketplaceRepository
    private lateinit var viewModel: AgentMarketplaceViewModel

    private val fakeAgents = listOf(
        AgentListing("a1", "Research Scout", "Research", "Summarizes markets", "100.00", "USDC", "Verified", "low", listOf("read")),
        AgentListing("a2", "Dev Runner", "Coding", "Pays for APIs", "250.00", "USDC", "Budget-limited", "medium", listOf("read", "write"))
    )

    private val fakeMarketplace = MarketplaceResult(
        categories = listOf("Research", "Coding"),
        agents = fakeAgents,
        settlementToken = "USDC",
        network = "Arc Testnet",
        policy = "Agents cannot bypass approval"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.getMarketplace() } returns fakeMarketplace
        viewModel = AgentMarketplaceViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads marketplace`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(2, state.agents.size)
        assertTrue(state.categories.contains("All"))
        assertEquals("Arc Testnet", state.network)
    }

    @Test
    fun `selectCategory filters agents`() = runTest {
        advanceUntilIdle()
        viewModel.selectCategory("Research")
        assertEquals("Research", viewModel.uiState.value.selectedCategory)
        assertEquals(1, viewModel.uiState.value.visibleAgents.size)
    }

    @Test
    fun `selectAll shows all agents`() = runTest {
        advanceUntilIdle()
        viewModel.selectCategory("Research")
        viewModel.selectCategory("All")
        assertEquals(2, viewModel.uiState.value.visibleAgents.size)
    }

    @Test
    fun `refresh reloads marketplace`() = runTest {
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
