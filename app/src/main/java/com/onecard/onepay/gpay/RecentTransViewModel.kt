package com.onecard.onepay.gpay

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onecard.onepay.util.Resource
import kotlinx.coroutines.launch

class RecentTransViewModel(
    val recentTransRepository: RecentTransRepository
) : ViewModel() {

    val recentTransData = MutableLiveData<Resource<RecentTransactionData>>()

    init {
        fetchRecentTrans()
    }

    fun fetchRecentTrans() {
        viewModelScope.launch {
          //  recentTransData.postValue(Resource.loading(null))
          //  if (networkHelper.isNetworkConnected()) {
                recentTransRepository.getRecentTransaction().let {
                    recentTransData.value = it
                }
         //   } else recentTransData.postValue(Resource.error("No Internet connection", null))
        }
    }
}