package com.example.transactionhistoryexercise

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.transactionhistoryexercise.data.model.Atm
import com.example.transactionhistoryexercise.data.model.TransactionHistoryViewItem
import com.example.transactionhistoryexercise.databinding.ActivityMainBinding
import com.example.transactionhistoryexercise.ui.HeaderItemDecoration
import com.example.transactionhistoryexercise.ui.OnItemClick
import com.example.transactionhistoryexercise.ui.TransactionHistoryAdapter
import com.example.transactionhistoryexercise.viewmodel.TransactionHistoryViewModel
import com.example.transactionhistoryexercise.viewmodel.TransactionHistoryViewModelFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

const val LOCATION = "LOCATION"
const val NAMEOFATM = "NAME"

class MainActivity : DaggerAppCompatActivity(), OnItemClick {

    @Inject
    lateinit var factory: TransactionHistoryViewModelFactory

    private lateinit var historyViewModel: TransactionHistoryViewModel

    @Inject
    lateinit var historyAdapter: TransactionHistoryAdapter

    private lateinit var atmList: List<Atm>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Setup actionBar
        supportActionBar!!.apply {
            setDisplayShowHomeEnabled(true)
            setLogo(R.drawable.icon_home_as_up_icon)
            setDisplayUseLogoEnabled(true)
            title = getString(R.string.account_details)
        }

        setupRecyclerView()

        setupViewModel(binding)

        // Setup pull down refreshing
        swipeRefreshLayout.setOnRefreshListener {
            historyViewModel.getTransactionHistory()
        }
    }

    private fun setupViewModel(binding: ActivityMainBinding) {
        historyViewModel =
            ViewModelProvider(this, factory).get(TransactionHistoryViewModel::class.java)
        historyViewModel.getHistoryLiveData().observe(this, Observer { it ->
            it?.let {
                atmList = it.atms
                binding.account = it.account
                historyAdapter.resetData()
                prepareMultiRecordTypeAdapter(it.transactions, it.pending, historyAdapter)
                swipeRefreshLayout.post{
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
        historyViewModel.snackBar.observe(this, Observer { text ->
            text?.let {
                Snackbar.make(rootLayout, it, Snackbar.LENGTH_SHORT).show()
                historyViewModel.onSnackBarShown()
                swipeRefreshLayout.post{
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    private fun setupRecyclerView() {
        history_list.layoutManager = LinearLayoutManager(this)
//        LinearSnapHelper().attachToRecyclerView(history_list)
        historyAdapter.setOnItemClick(this)
        history_list.adapter = historyAdapter
//        val horizontalDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            horizontalDecoration.setDrawable(getDrawable(R.drawable.horizontal_divider)!!)
//        }
//        history_list.addItemDecoration(horizontalDecoration)
        val headerItemDecoration = HeaderItemDecoration(this, DividerItemDecoration.VERTICAL, history_list) {itemPosition ->
            if (itemPosition >= 0 && itemPosition < historyAdapter.itemCount) {
                (history_list.adapter as TransactionHistoryAdapter).getItemViewType(itemPosition) == TransactionHistoryAdapter.TYPE_HEADER
            } else false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            headerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.horizontal_divider)!!)
        }
        history_list.addItemDecoration(headerItemDecoration)
    }

    override fun onResume() {
        super.onResume()
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        historyViewModel.getTransactionHistory()
    }

    // Generate adapter for recyclerView to display
    private fun prepareMultiRecordTypeAdapter(
        transactions: List<TransactionHistoryViewItem.Transaction>,
        pendingTransactions: List<TransactionHistoryViewItem.PendingTransaction>,
        adapter: TransactionHistoryAdapter) {

        val dateToTransactionHistoryItems = arrangeTransactionHistoryIntoSections(transactions, pendingTransactions)
        for (key in dateToTransactionHistoryItems.keys) {
            val header = TransactionHistoryViewItem.Header(key)
            if (!adapter.headers.contains(key)) {
                adapter.headers.add(key)
                adapter.histories.add(header)
            }
            adapter.histories.addAll(dateToTransactionHistoryItems[key] as List<TransactionHistoryViewItem>)
        }
        adapter.notifyDataSetChanged()
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

        val dateTimeFormatter = DateTimeFormat.forPattern(getString(R.string.ddMMyyy_format))

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

    // Start the activity for ATM location display
    override fun onItemClick(view: View, position: Int) {
        when (historyAdapter.histories[position]) {
            is TransactionHistoryViewItem.Transaction -> {
                val description = (historyAdapter.histories[position] as TransactionHistoryViewItem.Transaction).description
                for (atm in atmList) {
                    if (description.toLowerCase(Locale.ROOT).contains(atm.name.toLowerCase(Locale.ROOT))) {
                        Timber.i(description)
                        val fromPosition = LatLng(atm.location.lat, atm.location.lng)
                        val args = Bundle().apply {
                            putParcelable(LOCATION, fromPosition)
                        }

                        val intent = Intent(this, DisplayLocationActivity::class.java).apply {
                            putExtras(args)
                            putExtra(NAMEOFATM, atm.name)
                        }
                        startActivity(intent)
                        return
                    }
                }
            }
        }
    }
}
