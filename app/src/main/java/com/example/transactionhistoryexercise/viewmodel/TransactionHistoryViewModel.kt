package com.example.transactionhistoryexercise.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transactionhistoryexercise.data.TransactionHistoryRepository
import com.example.transactionhistoryexercise.data.model.TransactionHistory
import kotlinx.coroutines.launch
import javax.inject.Inject

class TransactionHistoryViewModel @Inject constructor(
    private val historyRepository: TransactionHistoryRepository)
    : ViewModel() {

    var historyLivaData = MutableLiveData<TransactionHistory>()

    private val _snackBar = MutableLiveData<String>()

    val snackBar: LiveData<String>
        get() = _snackBar

    fun getTransactionHistory() {
        return launchDataLoad {
                val historyResponse = historyRepository.getTransactionHistory()
                historyLivaData.value = historyResponse
        }
    }

    private fun launchDataLoad(block:suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (error: TransactionHistoryRepository.TransactionHistoryRefreshError) {
                _snackBar.value = error.message
            }
        }
    }

    fun getHistoryLiveData(): LiveData<TransactionHistory> = historyLivaData

    fun onSnackBarShown() {
        _snackBar.value = null
    }
}