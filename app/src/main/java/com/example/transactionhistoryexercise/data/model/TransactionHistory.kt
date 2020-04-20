package com.example.transactionhistoryexercise.data.model

data class TransactionHistory(
    val account: Account,
    val transactions: List<TransactionHistoryViewItem.Transaction>,
    val pending: List<TransactionHistoryViewItem.PendingTransaction>,
    val atms: List<Atm>
)