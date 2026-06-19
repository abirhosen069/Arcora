package com.arcora.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.model.TransactionRecord
import com.arcora.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    walletRepository: WalletRepository
) : ViewModel() {
    val activity: StateFlow<List<TransactionRecord>> = walletRepository.observeActivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
