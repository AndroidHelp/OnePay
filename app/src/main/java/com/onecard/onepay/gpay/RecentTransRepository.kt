package com.onecard.onepay.gpay

import com.onecard.onepay.util.Resource

class RecentTransRepository {
    val recentTrans = ArrayList<RecentTransactionItem>()
    fun getRecentTransaction(): Resource<RecentTransactionData> {

        recentTrans.add(RecentTransactionItem(0, 100.0, true))
        recentTrans.add(RecentTransactionItem(0, 10.2, false))
        recentTrans.add(RecentTransactionItem(0, 50.0, true))
        recentTrans.add(RecentTransactionItem(0, 70.5, false))
        recentTrans.add(RecentTransactionItem(0, 120.50, true))
        recentTrans.add(RecentTransactionItem(0, 180.50000, false))
        recentTrans.add(RecentTransactionItem(0, 1000.50000, true))
        recentTrans.add(RecentTransactionItem(0, 800.0, true))
        return Resource.success(RecentTransactionData(recentTrans))
    }
}