package com.onecard.onepay.di

import com.onecard.onepay.gpay.RecentTransViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        RecentTransViewModel(get())
    }
}