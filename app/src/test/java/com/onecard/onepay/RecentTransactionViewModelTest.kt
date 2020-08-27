package com.onecard.onepay

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.mock
import com.onecard.onepay.di.repoModule
import com.onecard.onepay.di.viewModelModule
import com.onecard.onepay.gpay.RecentTransRepository
import com.onecard.onepay.gpay.RecentTransViewModel
import com.onecard.onepay.gpay.RecentTransactionData
import com.onecard.onepay.util.Resource
import com.onecard.onepay.util.Status
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mock
import org.mockito.Mockito.verify

class RecentTransactionViewModelTest : KoinTest {

    val recentTransViewModel: RecentTransViewModel by inject()
    val recentTransRepository: RecentTransRepository by inject()

    @Mock
    lateinit var recentTransObserver: Observer<Resource<RecentTransactionData>>

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun before() {
        recentTransObserver = mock()
        startKoin { modules(viewModelModule, repoModule) }
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun after() {
        stopKoin()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun getRecentTransactionsSuccess() = runBlocking {
        recentTransViewModel.recentTransData.observeForever(recentTransObserver)
        recentTransViewModel.fetchRecentTrans()
        delay(10)
        val value = recentTransViewModel.recentTransData.value
            ?: recentTransRepository.getRecentTransaction()
        verify(recentTransObserver).onChanged(value)
        assert(recentTransViewModel.recentTransData.value!!.status == Status.SUCCESS)
    }

}