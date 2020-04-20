package com.example.transactionhistoryexercise.ui

import androidx.recyclerview.widget.RecyclerView
import com.example.transactionhistoryexercise.data.model.TransactionHistoryViewItem
import com.example.transactionhistoryexercise.databinding.SectionTransactionHistoryBinding

class HistorySectionViewHolder(private val sectionTransactionHistoryBinding: SectionTransactionHistoryBinding)
    : RecyclerView.ViewHolder(sectionTransactionHistoryBinding.root) {

    fun bind(item: TransactionHistoryViewItem.Header) {
        sectionTransactionHistoryBinding.header = item
        sectionTransactionHistoryBinding.executePendingBindings()
    }
}