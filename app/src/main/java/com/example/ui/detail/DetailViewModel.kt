package com.example.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Episode
import com.example.domain.model.UserWallet
import com.example.domain.repository.UserDataRepository
import com.example.domain.usecase.GetSeriesDetailUseCase
import com.example.domain.usecase.PurchaseCoinsUseCase
import com.example.domain.usecase.SeriesDetail
import com.example.domain.usecase.SetVipStatusUseCase
import com.example.domain.usecase.UnlockAllEpisodesUseCase
import com.example.domain.usecase.UnlockEpisodeUseCase
import com.example.domain.usecase.UnlockResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = false,
    val seriesDetail: SeriesDetail? = null,
    val userWallet: UserWallet = UserWallet(),
    val showPaywall: Boolean = false,
    val paywallTargetEpisode: Episode? = null
)

class DetailViewModel(
    private val getSeriesDetailUseCase: GetSeriesDetailUseCase,
    private val unlockEpisodeUseCase: UnlockEpisodeUseCase,
    private val unlockAllEpisodesUseCase: UnlockAllEpisodesUseCase,
    private val userDataRepository: UserDataRepository,
    private val purchaseCoinsUseCase: PurchaseCoinsUseCase,
    private val setVipStatusUseCase: SetVipStatusUseCase
) : ViewModel() {

    private val _seriesId = MutableStateFlow<String?>(null)
    private val _showPaywall = MutableStateFlow(false)
    private val _paywallTargetEpisode = MutableStateFlow<Episode?>(null)

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val uiState: StateFlow<DetailUiState> = combine(
        _seriesId,
        userDataRepository.getUserWallet(),
        _showPaywall,
        _paywallTargetEpisode
    ) { id, wallet, showPaywall, targetEp ->
        if (id == null) {
            DetailUiState(isLoading = true, userWallet = wallet)
        } else {
            // We combine with the series detail flow dynamically
            DetailUiState(
                isLoading = false,
                userWallet = wallet,
                showPaywall = showPaywall,
                paywallTargetEpisode = targetEp
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailUiState(isLoading = true)
    )

    private val _seriesDetailState = MutableStateFlow<SeriesDetail?>(null)
    val seriesDetailState: StateFlow<SeriesDetail?> = _seriesDetailState.asStateFlow()

    fun loadSeries(seriesId: String) {
        _seriesId.value = seriesId
        viewModelScope.launch {
            getSeriesDetailUseCase(seriesId).collect { detail ->
                _seriesDetailState.value = detail
            }
        }
    }

    fun openPaywall(episode: Episode?) {
        _paywallTargetEpisode.value = episode
        _showPaywall.value = true
    }

    fun dismissPaywall() {
        _showPaywall.value = false
        _paywallTargetEpisode.value = null
    }

    fun unlockEpisode(episode: Episode) {
        viewModelScope.launch {
            when (val result = unlockEpisodeUseCase(episode)) {
                is UnlockResult.SuccessUnlockedWithCoins -> {
                    _toastEvent.emit("Unlocked Episode ${episode.episodeNumber} with 50 Coins!")
                    dismissPaywall()
                }
                is UnlockResult.SuccessVipFreeAccess -> {
                    _toastEvent.emit("Unlocked with VIP Access!")
                    dismissPaywall()
                }
                is UnlockResult.AlreadyUnlocked -> {
                    dismissPaywall()
                }
                is UnlockResult.InsufficientCoins -> {
                    _toastEvent.emit("Insufficient coins. Top up below!")
                    _paywallTargetEpisode.value = episode
                    _showPaywall.value = true
                }
                is UnlockResult.Error -> {
                    _toastEvent.emit(result.message)
                }
            }
        }
    }

    fun unlockAllEpisodes(seriesId: String) {
        viewModelScope.launch {
            when (val result = unlockAllEpisodesUseCase(seriesId)) {
                is UnlockResult.SuccessUnlockedWithCoins -> {
                    _toastEvent.emit("All episodes unlocked with Coins!")
                    dismissPaywall()
                }
                is UnlockResult.SuccessVipFreeAccess -> {
                    _toastEvent.emit("All episodes unlocked with VIP Access!")
                    dismissPaywall()
                }
                is UnlockResult.AlreadyUnlocked -> {
                    _toastEvent.emit("All episodes are already unlocked!")
                    dismissPaywall()
                }
                is UnlockResult.InsufficientCoins -> {
                    _toastEvent.emit("Need ${result.required} coins to unlock all. Top up or Go VIP!")
                    _paywallTargetEpisode.value = null
                    _showPaywall.value = true
                }
                is UnlockResult.Error -> {
                    _toastEvent.emit(result.message)
                }
            }
        }
    }

    fun purchaseCoins(amount: Int) {
        viewModelScope.launch {
            purchaseCoinsUseCase(amount)
            _toastEvent.emit("Added $amount coins to your balance!")
            val target = _paywallTargetEpisode.value
            if (target != null) {
                unlockEpisode(target)
            }
        }
    }

    fun subscribeVip() {
        viewModelScope.launch {
            setVipStatusUseCase(isVip = true, durationDays = 30)
            _toastEvent.emit("Welcome to VIP! All series and episodes are unlocked.")
            dismissPaywall()
        }
    }
}
