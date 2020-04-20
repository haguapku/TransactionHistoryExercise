package com.example.transactionhistoryexercise.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.transactionhistoryexercise.R
import com.example.transactionhistoryexercise.data.model.TransactionHistoryViewItem

class TransactionHistoryAdapter(
    var histories: MutableList<TransactionHistoryViewItem>,
    var headers: MutableSet<String>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var onItemClick: OnItemClick

    fun resetData() {
        histories.clear()
        headers.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TRANSACTION -> HistoryItemViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_transaction_history, parent,
                false), onItemClick)
            TYPE_PENDING -> PendingItemViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_pending_history,
                parent, false))
            else -> HistorySectionViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.section_transaction_history,
                parent, false))
        }
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is HistoryItemViewHolder -> holder.itemTransactionHistoryBinding.transaction =
                (histories[position] as TransactionHistoryViewItem.Transaction)
            is PendingItemViewHolder -> holder.itemPendingHistoryBinding.pending =
                (histories[position] as TransactionHistoryViewItem.PendingTransaction)
            is HistorySectionViewHolder -> holder.sectionTransactionHistoryBinding.header =
                (histories[position] as TransactionHistoryViewItem.Header)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (histories[position]) {
            is TransactionHistoryViewItem.Transaction -> TYPE_TRANSACTION
            is TransactionHistoryViewItem.PendingTransaction -> TYPE_PENDING
            is TransactionHistoryViewItem.Header -> TYPE_HEADER
        }
    }

    companion object {
        const val TYPE_TRANSACTION = 1
        const val TYPE_PENDING = 2
        const val TYPE_HEADER = 3
    }

    fun setOnItemClick(onItemClick: OnItemClick) {
        this.onItemClick = onItemClick
    }
}