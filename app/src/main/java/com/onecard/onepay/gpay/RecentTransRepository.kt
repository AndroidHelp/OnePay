package com.onecard.onepay.gpay

class RecentTransRepository {

    fun getRecentTransaction(): RecentTransactionData {

        val recentTrans = ArrayList<RecentTransactionItem>()
        recentTrans.add(RecentTransactionItem(0, 100.0, true))
        recentTrans.add(RecentTransactionItem(0, 10.2, false))
        recentTrans.add(RecentTransactionItem(0, 50.0, true))
        recentTrans.add(RecentTransactionItem(0, 70.5, false))
        recentTrans.add(RecentTransactionItem(0, 120.50, true))
        recentTrans.add(RecentTransactionItem(0, 180.50000, false))
        recentTrans.add(RecentTransactionItem(0, 1000.50000, true))
        recentTrans.add(RecentTransactionItem(0, 800.0, true))
        return RecentTransactionData(recentTrans)
    }
}