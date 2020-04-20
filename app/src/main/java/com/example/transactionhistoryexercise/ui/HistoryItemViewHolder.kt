package com.example.transactionhistoryexercise.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.transactionhistoryexercise.data.model.TransactionHistoryViewItem
import com.example.transactionhistoryexercise.databinding.ItemTransactionHistoryBinding

class HistoryItemViewHolder(private val itemTransactionHistoryBinding: ItemTransactionHistoryBinding, private val onItemClick: OnItemClick)
    :RecyclerView.ViewHolder(itemTransactionHistoryBinding.root), View.OnClickListener {

    init {
        itemTransactionHistoryBinding.transactionItem.setOnClickListener(this)
    }

    fun bind(item: TransactionHistoryViewItem.Transaction) {
        itemTransactionHistoryBinding.transaction = item
        itemTransactionHistoryBinding.executePendingBindings()
    }

    override fun onClick(view: View?) {

        when (view) {
            itemTransactionHistoryBinding.transactionItem -> view?.let {onItemClick.onItemClick(view, adapterPosition)}
        }
    }
}