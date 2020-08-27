package com.onecard.onepay.gpay

import com.onecard.onepay.di.repoModule
import com.onecard.onepay.util.Resource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RecentTransRepositoryTest:KoinTest {

    val recentTransRepository : RecentTransRepository by inject()

    @Before
    fun setUp() {
        startKoin { modules(repoModule) }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun getRecentTransaction() {
        val data = recentTransRepository.getRecentTransaction()
        assertNotNull(data)
        assertEquals(8, Resource.success(data).data!!.data!!.recentTransactionData.size)
    }
}