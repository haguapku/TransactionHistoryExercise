package com.example.transactionhistoryexercise.ui

import androidx.recyclerview.widget.RecyclerView
import com.example.transactionhistoryexercise.data.model.TransactionHistoryViewItem
import com.example.transactionhistoryexercise.databinding.ItemPendingHistoryBinding

class PendingItemViewHolder(private val itemPendingHistoryBinding: ItemPendingHistoryBinding)
    : RecyclerView.ViewHolder(itemPendingHistoryBinding.root) {

    fun bind(item: TransactionHistoryViewItem.PendingTransaction) {
        itemPendingHistoryBinding.pending = item
        itemPendingHistoryBinding.executePendingBindings()
    }
}