package com.example.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.DramaDatabase
import com.example.data.preferences.UserPreferencesManager
import com.example.data.repository.DramaRepositoryImpl
import com.example.data.repository.UserDataRepositoryImpl
import com.example.domain.repository.DramaRepository
import com.example.domain.repository.UserDataRepository
import com.example.domain.usecase.GetEpisodesUseCase
import com.example.domain.usecase.GetSeriesDetailUseCase
import com.example.domain.usecase.GetSeriesUseCase
import com.example.domain.usecase.PurchaseCoinsUseCase
import com.example.domain.usecase.SetVipStatusUseCase
import com.example.domain.usecase.UnlockAllEpisodesUseCase
import com.example.domain.usecase.UnlockEpisodeUseCase
import com.example.ui.detail.DetailViewModel
import com.example.ui.home.HomeViewModel
import com.example.ui.player.PlayerViewModel
import com.example.ui.profile.ProfileViewModel
import com.example.ui.history.HistoryViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import com.example.BuildConfig

object ServiceLocator {

    @Volatile
    private var database: DramaDatabase? = null

    @Volatile
    private var preferencesManager: UserPreferencesManager? = null

    @Volatile
    private var dramaRepository: DramaRepository? = null

    @Volatile
    private var userDataRepository: UserDataRepository? = null

    @Volatile
    private var supabaseClient: SupabaseClient? = null

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        if (database == null) {
            database = DramaDatabase.getInstance(appContext)
        }
        if (preferencesManager == null) {
            preferencesManager = UserPreferencesManager(appContext)
        }
        if (dramaRepository == null) {
            val db = database!!
            dramaRepository = DramaRepositoryImpl(
                seriesDao = db.seriesDao(),
                episodeDao = db.episodeDao(),
                watchHistoryDao = db.watchHistoryDao()
            )
        }
        if (userDataRepository == null) {
            val db = database!!
            userDataRepository = UserDataRepositoryImpl(preferencesManager!!, db.referralDao())
        }
        if (supabaseClient == null) {
            supabaseClient = createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Auth)
                install(Postgrest)
                install(Realtime)
            }
        }
    }

    fun provideDramaRepository(context: Context): DramaRepository {
        initialize(context)
        return dramaRepository!!
    }

    fun provideUserDataRepository(context: Context): UserDataRepository {
        initialize(context)
        return userDataRepository!!
    }

    fun provideGetSeriesUseCase(context: Context) = GetSeriesUseCase(provideDramaRepository(context))
    fun provideGetSeriesDetailUseCase(context: Context) = GetSeriesDetailUseCase(provideDramaRepository(context))
    fun provideGetEpisodesUseCase(context: Context) = GetEpisodesUseCase(provideDramaRepository(context))
    fun provideUnlockEpisodeUseCase(context: Context) = UnlockEpisodeUseCase(
        dramaRepository = provideDramaRepository(context),
        userDataRepository = provideUserDataRepository(context)
    )
    fun provideUnlockAllEpisodesUseCase(context: Context) = UnlockAllEpisodesUseCase(
        dramaRepository = provideDramaRepository(context),
        userDataRepository = provideUserDataRepository(context)
    )
    fun providePurchaseCoinsUseCase(context: Context) = PurchaseCoinsUseCase(provideUserDataRepository(context))
    fun provideSetVipStatusUseCase(context: Context) = SetVipStatusUseCase(provideUserDataRepository(context))

    fun provideViewModelFactory(context: Context): ViewModelProvider.Factory {
        initialize(context)
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                        HomeViewModel(
                            getSeriesUseCase = provideGetSeriesUseCase(context),
                            userDataRepository = provideUserDataRepository(context)
                        ) as T
                    }
                    modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                        DetailViewModel(
                            getSeriesDetailUseCase = provideGetSeriesDetailUseCase(context),
                            unlockEpisodeUseCase = provideUnlockEpisodeUseCase(context),
                            unlockAllEpisodesUseCase = provideUnlockAllEpisodesUseCase(context),
                            userDataRepository = provideUserDataRepository(context),
                            purchaseCoinsUseCase = providePurchaseCoinsUseCase(context),
                            setVipStatusUseCase = provideSetVipStatusUseCase(context)
                        ) as T
                    }
                    modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                        PlayerViewModel(
                            dramaRepository = provideDramaRepository(context),
                            userDataRepository = provideUserDataRepository(context),
                            unlockEpisodeUseCase = provideUnlockEpisodeUseCase(context),
                            purchaseCoinsUseCase = providePurchaseCoinsUseCase(context),
                            setVipStatusUseCase = provideSetVipStatusUseCase(context)
                        ) as T
                    }
                    modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                        ProfileViewModel(
                            dramaRepository = provideDramaRepository(context),
                            userDataRepository = provideUserDataRepository(context),
                            purchaseCoinsUseCase = providePurchaseCoinsUseCase(context),
                            setVipStatusUseCase = provideSetVipStatusUseCase(context)
                        ) as T
                    }
                    modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                        HistoryViewModel(
                            dramaRepository = provideDramaRepository(context)
                        ) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
