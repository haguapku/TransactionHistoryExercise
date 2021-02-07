package com.example.transactionhistoryexercise.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transactionhistoryexercise.data.TransactionHistoryRepository
import com.example.transactionhistoryexercise.data.model.Account
import com.example.transactionhistoryexercise.data.model.Atm
import com.example.transactionhistoryexercise.data.model.TransactionHistoryViewItem
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class TransactionHistoryViewModel @Inject constructor(
    private val historyRepository: TransactionHistoryRepository)
    : ViewModel() {

    var arrangedHistoryLivaData = MutableLiveData<LinkedHashMap<String, MutableList<TransactionHistoryViewItem>>>()

    var atmListLivaData = MutableLiveData<List<Atm>>()

    var accountLivaData = MutableLiveData<Account>()

    private val _snackBar = MutableLiveData<String>()

    val snackBar: LiveData<String>
        get() = _snackBar

    fun getTransactionHistory() {
        return launchDataLoad {
            val historyResponse = historyRepository.getTransactionHistory()
            historyResponse?.let {
                arrangedHistoryLivaData.value =
                    arrangeTransactionHistoryIntoSections(it.transactions, it.pending)
                atmListLivaData.value = it.atms
                accountLivaData.value = it.account
            }
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

    fun getArrangedHistoryLiveData(): LiveData<LinkedHashMap<String, MutableList<TransactionHistoryViewItem>>> = arrangedHistoryLivaData

    fun getAtmListLivaData(): LiveData<List<Atm>> = atmListLivaData

    fun getAccountLivaData(): LiveData<Account> = accountLivaData

    fun onSnackBarShown() {
        _snackBar.value = null
    }

    // Arrange the combined list into sections
    private fun arrangeTransactionHistoryIntoSections(
        transactions: List<TransactionHistoryViewItem.Transaction>,
        pendingTransactions: List<TransactionHistoryViewItem.PendingTransaction>)
            : LinkedHashMap<String, MutableList<TransactionHistoryViewItem>> {
        val combineList = combineTransactionAndPendingList(transactions, pendingTransactions)
        val dateToTransactionHistoryItems = LinkedHashMap<String, MutableList<TransactionHistoryViewItem>>()

        for (item in combineList) {
            val key = when (item) {
                is TransactionHistoryViewItem.Transaction -> item.effectiveDate
                is TransactionHistoryViewItem.PendingTransaction -> item.effectiveDate
                else -> null
            }
            if (!dateToTransactionHistoryItems.containsKey(key)) {
                dateToTransactionHistoryItems[key!!] = ArrayList()
            }
            dateToTransactionHistoryItems[key]?.add(item)
        }
        return dateToTransactionHistoryItems
    }

    // Combine transaction list and pending list into one list by date
    private fun combineTransactionAndPendingList(
        transactions: List<TransactionHistoryViewItem.Transaction>,
        pendingTransactions: List<TransactionHistoryViewItem.PendingTransaction>)
            : List<TransactionHistoryViewItem>{
        val combineList = ArrayList<TransactionHistoryViewItem>(transactions.size + pendingTransactions.size)
        var dequeTransaction = ArrayDeque<TransactionHistoryViewItem>(transactions)
        var dequePending = ArrayDeque<TransactionHistoryViewItem>(pendingTransactions)

        val dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")

        while (!dequeTransaction.isEmpty() && !dequePending.isEmpty()) {
            var temp = dequeTransaction.peek()
            val x = when(temp) {
                is TransactionHistoryViewItem.Transaction -> temp.effectiveDate
                is TransactionHistoryViewItem.PendingTransaction -> temp.effectiveDate
                else -> null
            }
            temp = dequePending.peek()
            val y = when(temp) {
                is TransactionHistoryViewItem.Transaction -> temp.effectiveDate
                is TransactionHistoryViewItem.PendingTransaction -> temp.effectiveDate
                else -> null
            }
            val transactionDate = dateTimeFormatter.parseDateTime(x!!)
            val pendingDate = dateTimeFormatter.parseDateTime(y!!)
            if (transactionDate!! > pendingDate!!) {
                combineList.add(dequeTransaction.poll()!!)
            } else {
                combineList.add(dequePending.poll()!!)
                val deque = dequeTransaction
                dequeTransaction = dequePending
                dequePending = deque
            }
        }

        combineList.addAll(dequeTransaction)
        combineList.addAll(dequePending)

        return combineList
    }
}