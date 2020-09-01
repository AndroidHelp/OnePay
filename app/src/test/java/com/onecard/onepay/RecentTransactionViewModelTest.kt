package com.onecard.onepay

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.mock
import com.onecard.onepay.gpay.RecentTransRepository
import com.onecard.onepay.gpay.RecentTransViewModel
import com.onecard.onepay.gpay.RecentTransactionData
import com.onecard.onepay.gpay.RecentTransactionItem
import com.onecard.onepay.util.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class RecentTransactionViewModelTest : KoinTest {

//    val recentTransViewModel: RecentTransViewModel by inject()
//    val recentTransRepository: RecentTransRepository by inject()

    val recentTransData = ArrayList<RecentTransactionItem>()
    private lateinit var recentTransViewModel: RecentTransViewModel

    @Mock
    lateinit var recentTransRepository: RecentTransRepository

    @Mock
    lateinit var recentTransObserver: Observer<Resource<RecentTransactionData>>

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        recentTransObserver = mock()
        //   startKoin { modules(viewModelModule, repoModule) }
        Dispatchers.setMain(mainThreadSurrogate)
        recentTransViewModel = RecentTransViewModel(recentTransRepository)
        recentTransViewModel.recentTransData.observeForever(recentTransObserver)

    }

    @After
    fun after() {
        stopKoin()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    /*  @Test
      fun getRecentTransactionsSuccess() = runBlocking {
          recentTransViewModel.recentTransData.observeForever(recentTransObserver)
          recentTransViewModel.fetchRecentTrans()
          delay(10)
          val value = recentTransViewModel.recentTransData.value
              ?: recentTransRepository.getRecentTransaction()
          verify(recentTransObserver).onChanged(value)
          assert(recentTransViewModel.recentTransData.value!!.status == Status.SUCCESS)
      }*/

    @Test
    fun getRecentTransactionSuccess() = runBlocking {
        `when`(recentTransRepository.getRecentTransaction()).thenReturn(
            Resource.success(
                RecentTransactionData(recentTransData)
            )
        )
        recentTransViewModel.fetchRecentTrans()
        delay(10)
        assertNotNull(recentTransViewModel.recentTransData.value)
        assertEquals(recentTransRepository.getRecentTransaction().status, recentTransViewModel.recentTransData.value?.status)
        verify(recentTransObserver).onChanged(recentTransViewModel.recentTransData.value)
        println(recentTransViewModel.recentTransData.value!!.data!!.recentTransactionData.size)
    }

    @Test
    fun getRecentTransactionFailed() = runBlocking {
        `when`(recentTransRepository.getRecentTransaction()).thenReturn(
            Resource.error("error",
               null
            )
        )
        recentTransViewModel.fetchRecentTrans()
        delay(10)
        assertNotNull(recentTransViewModel.recentTransData.value)
        assertEquals(recentTransRepository.getRecentTransaction().status, recentTransViewModel.recentTransData.value?.status)
        verify(recentTransObserver).onChanged(recentTransViewModel.recentTransData.value)
//        println(recentTransViewModel.recentTransData.value!!.data!!.recentTransactionData.size)
    }

}