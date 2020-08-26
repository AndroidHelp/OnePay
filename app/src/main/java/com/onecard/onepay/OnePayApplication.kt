package com.onecard.onepay

import android.app.Application
import com.onecard.onepay.di.appModule
import com.onecard.onepay.di.repoModule
import com.onecard.onepay.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OnePayApplication :Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OnePayApplication)
            modules(listOf(appModule, repoModule, viewModelModule))
        }
    }
}