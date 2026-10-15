package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Series
import com.example.domain.model.UserWallet
import com.example.domain.repository.UserDataRepository
import com.example.domain.usecase.GetSeriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val isLoading: Boolean = false,
    val seriesList: List<Series> = emptyList(),
    val selectedCategory: String = "All",
    val userWallet: UserWallet = UserWallet()
) {
    val trendingSeries: List<Series>
        get() = seriesList.filter { it.isTrending }

    val filteredSeries: List<Series>
        get() = if (selectedCategory == "All") {
            seriesList
        } else {
            seriesList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

    val featuredSeries: Series?
        get() = seriesList.firstOrNull { it.isTrending } ?: seriesList.firstOrNull()
}

class HomeViewModel(
    private val getSeriesUseCase: GetSeriesUseCase,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        getSeriesUseCase(),
        userDataRepository.getUserWallet(),
        _selectedCategory
    ) { series, wallet, category ->
        HomeUiState(
            isLoading = false,
            seriesList = series,
            selectedCategory = category,
            userWallet = wallet
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
}
