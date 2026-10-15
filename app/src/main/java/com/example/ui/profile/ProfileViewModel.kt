package com.example.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.DailyCheckInStatus
import com.example.domain.model.UserProfile
import com.example.domain.model.UserWallet
import com.example.domain.model.WatchHistory
import com.example.domain.repository.DramaRepository
import com.example.domain.repository.UserDataRepository
import com.example.domain.usecase.PurchaseCoinsUseCase
import com.example.domain.usecase.SetVipStatusUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userWallet: UserWallet = UserWallet(),
    val userProfile: UserProfile = UserProfile(),
    val checkInStatus: DailyCheckInStatus = DailyCheckInStatus(),
    val watchHistory: List<WatchHistory> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val selectedLanguage: String = "English",
    val showTopUpDialog: Boolean = false,
    val showRegisterDialog: Boolean = false,
    val showReferralDialog: Boolean = false,
    val showCheckInDialog: Boolean = false
)

private data class ProfileDialogsState(
    val notifications: Boolean = true,
    val language: String = "English",
    val showTopUp: Boolean = false,
    val showRegister: Boolean = false,
    val showReferral: Boolean = false,
    val showCheckIn: Boolean = false
)

class ProfileViewModel(
    private val dramaRepository: DramaRepository,
    private val userDataRepository: UserDataRepository,
    private val purchaseCoinsUseCase: PurchaseCoinsUseCase,
    private val setVipStatusUseCase: SetVipStatusUseCase
) : ViewModel() {

    private val _dialogsState = MutableStateFlow(ProfileDialogsState())

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val uiState: StateFlow<ProfileUiState> = combine(
        userDataRepository.getUserWallet(),
        userDataRepository.getUserProfile(),
        userDataRepository.getDailyCheckInStatus(),
        dramaRepository.getWatchHistory(),
        _dialogsState
    ) { wallet, profile, checkIn, history, dialogs ->
        ProfileUiState(
            userWallet = wallet,
            userProfile = profile,
            checkInStatus = checkIn,
            watchHistory = history,
            notificationsEnabled = dialogs.notifications,
            selectedLanguage = dialogs.language,
            showTopUpDialog = dialogs.showTopUp,
            showRegisterDialog = dialogs.showRegister,
            showReferralDialog = dialogs.showReferral,
            showCheckInDialog = dialogs.showCheckIn
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun toggleNotifications() {
        _dialogsState.value = _dialogsState.value.copy(notifications = !_dialogsState.value.notifications)
    }

    fun openTopUp() {
        _dialogsState.value = _dialogsState.value.copy(showTopUp = true)
    }

    fun dismissTopUp() {
        _dialogsState.value = _dialogsState.value.copy(showTopUp = false)
    }

    fun openRegister() {
        _dialogsState.value = _dialogsState.value.copy(showRegister = true)
    }

    fun dismissRegister() {
        _dialogsState.value = _dialogsState.value.copy(showRegister = false)
    }

    fun openReferral() {
        _dialogsState.value = _dialogsState.value.copy(showReferral = true)
    }

    fun dismissReferral() {
        _dialogsState.value = _dialogsState.value.copy(showReferral = false)
    }

    fun openCheckIn() {
        _dialogsState.value = _dialogsState.value.copy(showCheckIn = true)
    }

    fun dismissCheckIn() {
        _dialogsState.value = _dialogsState.value.copy(showCheckIn = false)
    }

    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val (success, message) = userDataRepository.claimDailyCheckIn()
            _toastEvent.emit(message)
            if (success) {
                dismissCheckIn()
            }
        }
    }

    fun registerWithGoogle(name: String, email: String) {
        viewModelScope.launch {
            val result = userDataRepository.registerWithGoogle(name, email)
            if (result.isSuccess) {
                _toastEvent.emit("🎉 Registered with Google! +200 Welcome Coins added to your wallet.")
                dismissRegister()
            } else {
                _toastEvent.emit("Registration failed. Please try again.")
            }
        }
    }

    fun registerWithEmail(name: String, email: String, pass: String) {
        viewModelScope.launch {
            val result = userDataRepository.registerWithEmail(name, email, pass)
            if (result.isSuccess) {
                _toastEvent.emit("🎉 Welcome to DramaStream! +200 Free Coins added to your wallet.")
                dismissRegister()
            } else {
                _toastEvent.emit("Registration failed. Please check your credentials.")
            }
        }
    }

    fun applyReferralCode(code: String) {
        viewModelScope.launch {
            val (_, message) = userDataRepository.applyReferralCode(code)
            _toastEvent.emit(message)
        }
    }

    fun simulateFriendReferral() {
        viewModelScope.launch {
            userDataRepository.simulateFriendReferral()
            _toastEvent.emit("🎉 Great news! A friend joined with your code. +150 Free Coins earned!")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userDataRepository.signOut()
            _toastEvent.emit("Signed out of your account.")
        }
    }

    fun addCoins(amount: Int) {
        viewModelScope.launch {
            purchaseCoinsUseCase(amount)
            _toastEvent.emit("Successfully purchased $amount coins!")
            _dialogsState.value = _dialogsState.value.copy(showTopUp = false)
        }
    }

    fun toggleVip(active: Boolean) {
        viewModelScope.launch {
            setVipStatusUseCase(active, durationDays = 30)
            if (active) {
                _toastEvent.emit("VIP Membership activated for 30 days!")
            } else {
                _toastEvent.emit("VIP Membership deactivated.")
            }
        }
    }
}
