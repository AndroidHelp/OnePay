package com.onecard.onepay.di

import com.onecard.onepay.gpay.RecentTransRepository
import org.koin.dsl.module

val repoModule = module {
    single {
        RecentTransRepository()
    }
}