package com.karanrajux.criczen.di

import com.karanrajux.criczen.data.AppDatabase
import com.karanrajux.criczen.data.CricketRepository
import com.karanrajux.criczen.data.OnboardingManager
import com.karanrajux.criczen.viewmodel.CricketViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { CricketRepository(androidContext()) }
    single { OnboardingManager(androidContext()) }
    
    viewModel { CricketViewModel(get(), get()) }
}
