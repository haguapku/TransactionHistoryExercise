package com.example.transactionhistoryexercise.data.model

sealed class TransactionHistoryViewItem {

    data class Transaction(
        val id: String,
        val effectiveDate: String,
        val description: String,
        val amount: Float
    ): TransactionHistoryViewItem()

    data class PendingTransaction(
        val id: String,
        val effectiveDate: String,
        val description: String,
        val amount: Float
    ): TransactionHistoryViewItem()

    data class Header (val date: String)
        : TransactionHistoryViewItem()
}