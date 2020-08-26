package com.onecard.onepay.gpay

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onecard.onepay.util.NetworkHelper
import com.onecard.onepay.util.Resource
import kotlinx.coroutines.launch

class RecentTransViewModel(
    val recentTransRepository: RecentTransRepository,
    val networkHelper: NetworkHelper
) : ViewModel() {

    val recentTransData = MutableLiveData<Resource<RecentTransactionData>>()

    init {
        fetchRecentTrans()
    }

    private fun fetchRecentTrans() {
        viewModelScope.launch {
            recentTransData.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                recentTransRepository.getRecentTransaction().let {
                    recentTransData.postValue(Resource.success(it))
                }
            } else recentTransData.postValue(Resource.error("No Internet connection", null))
        }
    }
}