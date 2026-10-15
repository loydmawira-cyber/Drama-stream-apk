package com.example.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Episode
import com.example.domain.model.Series
import com.example.domain.model.UserWallet
import com.example.domain.repository.DramaRepository
import com.example.domain.repository.UserDataRepository
import com.example.domain.usecase.PurchaseCoinsUseCase
import com.example.domain.usecase.SetVipStatusUseCase
import com.example.domain.usecase.UnlockEpisodeUseCase
import com.example.domain.usecase.UnlockResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isLoading: Boolean = false,
    val series: Series? = null,
    val episodes: List<Episode> = emptyList(),
    val currentEpisodeIndex: Int = 0,
    val userWallet: UserWallet = UserWallet(),
    val isPlaying: Boolean = true,
    val showPaywall: Boolean = false,
    val lockedTargetEpisode: Episode? = null,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
) {
    val currentEpisode: Episode?
        get() = episodes.getOrNull(currentEpisodeIndex)

    fun isEpisodeUnlocked(episode: Episode): Boolean {
        return episode.isUnlocked || episode.isFree || userWallet.isVip
    }
}

private data class PlaybackState(
    val currentEpisodeIndex: Int = 0,
    val isPlaying: Boolean = true,
    val showPaywall: Boolean = false,
    val lockedTargetEpisode: Episode? = null,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)

class PlayerViewModel(
    private val dramaRepository: DramaRepository,
    private val userDataRepository: UserDataRepository,
    private val unlockEpisodeUseCase: UnlockEpisodeUseCase,
    private val purchaseCoinsUseCase: PurchaseCoinsUseCase,
    private val setVipStatusUseCase: SetVipStatusUseCase
) : ViewModel() {

    private val _seriesId = MutableStateFlow<String?>(null)
    private val _playbackState = MutableStateFlow(PlaybackState())

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _seriesFlow = MutableStateFlow<Series?>(null)
    private val _episodesFlow = MutableStateFlow<List<Episode>>(emptyList())

    val uiState: StateFlow<PlayerUiState> = combine(
        _seriesFlow,
        _episodesFlow,
        userDataRepository.getUserWallet(),
        _playbackState
    ) { series, episodes, wallet, playback ->
        PlayerUiState(
            isLoading = series == null || episodes.isEmpty(),
            series = series,
            episodes = episodes,
            currentEpisodeIndex = playback.currentEpisodeIndex,
            userWallet = wallet,
            isPlaying = playback.isPlaying,
            showPaywall = playback.showPaywall,
            lockedTargetEpisode = playback.lockedTargetEpisode,
            isLiked = playback.isLiked,
            isBookmarked = playback.isBookmarked
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState(isLoading = true)
    )

    fun initPlayer(seriesId: String, initialEpisodeNumber: Int) {
        _seriesId.value = seriesId
        viewModelScope.launch {
            dramaRepository.getSeriesById(seriesId).collect { series ->
                _seriesFlow.value = series
            }
        }
        viewModelScope.launch {
            dramaRepository.getEpisodesForSeries(seriesId).collect { episodes ->
                _episodesFlow.value = episodes
                val targetIndex = (initialEpisodeNumber - 1).coerceIn(0, (episodes.size - 1).coerceAtLeast(0))
                _playbackState.value = _playbackState.value.copy(currentEpisodeIndex = targetIndex)
                checkIfTargetEpisodeIsLocked(episodes.getOrNull(targetIndex))
            }
        }
    }

    fun onEpisodePageChanged(newIndex: Int) {
        _playbackState.value = _playbackState.value.copy(currentEpisodeIndex = newIndex)
        val episode = _episodesFlow.value.getOrNull(newIndex)
        checkIfTargetEpisodeIsLocked(episode)
    }

    private fun checkIfTargetEpisodeIsLocked(episode: Episode?) {
        if (episode == null) return
        val wallet = uiState.value.userWallet
        val isUnlocked = episode.isUnlocked || episode.isFree || wallet.isVip
        if (!isUnlocked) {
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                lockedTargetEpisode = episode,
                showPaywall = true
            )
        } else {
            _playbackState.value = _playbackState.value.copy(
                isPlaying = true,
                showPaywall = false,
                lockedTargetEpisode = null
            )
        }
    }

    fun togglePlayPause() {
        val currentEp = uiState.value.currentEpisode ?: return
        if (uiState.value.isEpisodeUnlocked(currentEp)) {
            _playbackState.value = _playbackState.value.copy(isPlaying = !_playbackState.value.isPlaying)
        } else {
            _playbackState.value = _playbackState.value.copy(showPaywall = true)
        }
    }

    fun toggleLike() {
        _playbackState.value = _playbackState.value.copy(isLiked = !_playbackState.value.isLiked)
    }

    fun toggleBookmark() {
        _playbackState.value = _playbackState.value.copy(isBookmarked = !_playbackState.value.isBookmarked)
    }

    fun dismissPaywall() {
        _playbackState.value = _playbackState.value.copy(showPaywall = false)
    }

    fun unlockEpisode(episode: Episode) {
        viewModelScope.launch {
            when (val result = unlockEpisodeUseCase(episode)) {
                is UnlockResult.SuccessUnlockedWithCoins -> {
                    _toastEvent.emit("Unlocked Episode ${episode.episodeNumber}!")
                    _playbackState.value = _playbackState.value.copy(showPaywall = false, isPlaying = true)
                }
                is UnlockResult.SuccessVipFreeAccess -> {
                    _toastEvent.emit("VIP Access Granted!")
                    _playbackState.value = _playbackState.value.copy(showPaywall = false, isPlaying = true)
                }
                is UnlockResult.AlreadyUnlocked -> {
                    _playbackState.value = _playbackState.value.copy(showPaywall = false, isPlaying = true)
                }
                is UnlockResult.InsufficientCoins -> {
                    _toastEvent.emit("Need ${result.required} coins. Buy coins below!")
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
            _toastEvent.emit("Added $amount coins!")
            val target = _playbackState.value.lockedTargetEpisode
            if (target != null) {
                unlockEpisode(target)
            }
        }
    }

    fun subscribeVip() {
        viewModelScope.launch {
            setVipStatusUseCase(isVip = true, durationDays = 30)
            _toastEvent.emit("VIP Membership Activated!")
            _playbackState.value = _playbackState.value.copy(showPaywall = false, isPlaying = true)
        }
    }

    fun onEpisodeFinished() {
        val nextIndex = _playbackState.value.currentEpisodeIndex + 1
        if (nextIndex < _episodesFlow.value.size) {
            _playbackState.value = _playbackState.value.copy(currentEpisodeIndex = nextIndex)
            checkIfTargetEpisodeIsLocked(_episodesFlow.value[nextIndex])
        }
    }

    fun updateProgress(positionMs: Long, durationMs: Long) {
        val seriesId = _seriesId.value ?: return
        val epNum = _playbackState.value.currentEpisodeIndex + 1
        viewModelScope.launch {
            dramaRepository.updateWatchProgress(seriesId, epNum, positionMs, durationMs)
        }
    }
}
