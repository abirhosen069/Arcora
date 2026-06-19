package com.arcora.domain.agent

import com.arcora.domain.model.Money

data class AgentListing(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val monthlyBudget: Money,
    val reputationLabel: String
)
