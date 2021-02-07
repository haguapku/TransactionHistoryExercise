package com.example.transactionhistoryexercise

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.transactionhistoryexercise.data.model.Atm
import com.example.transactionhistoryexercise.data.model.TransactionHistoryViewItem
import com.example.transactionhistoryexercise.databinding.ActivityMainBinding
import com.example.transactionhistoryexercise.ui.HeaderItemDecoration
import com.example.transactionhistoryexercise.ui.OnItemClick
import com.example.transactionhistoryexercise.widget.OnRefreshListener
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
//        swipeRefreshLayout.setOnRefreshListener {
//            historyViewModel.getTransactionHistory()
//        }
    }

    private fun setupViewModel(binding: ActivityMainBinding) {
        historyViewModel =
            ViewModelProvider(this, factory).get(TransactionHistoryViewModel::class.java)

        historyViewModel.getArrangedHistoryLiveData().observe(this, Observer {it ->
            prepareMultiRecordTypeAdapter(it, historyAdapter)
            history_list.refreshComplete()
        })
        historyViewModel.getAtmListLivaData().observe(this, Observer {
            atmList = it
        })
        historyViewModel.getAccountLivaData().observe(this, Observer {
            binding.account = it
        })
        historyViewModel.snackBar.observe(this, Observer { text ->
            text?.let {
                Snackbar.make(rootLayout, it, Snackbar.LENGTH_SHORT).show()
                historyViewModel.onSnackBarShown()
//                swipeRefreshLayout.post{
//                    swipeRefreshLayout.isRefreshing = false
//                }
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

        history_list.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                historyViewModel.getTransactionHistory()
            }

            override fun onRefreshTimeout() {
                Handler().removeCallbacksAndMessages(null)
            }

            override fun onLoadMore() {
                Thread.sleep(1000)
                history_list.loadMoreComplete()
                history_list.setNoMore(true)
            }

            override fun onLoreMoreTimeout() {
                Handler().removeCallbacksAndMessages(null)
            }
        })
    }

    override fun onResume() {
        super.onResume()
//        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        historyViewModel.getTransactionHistory()
    }

    // Generate adapter for recyclerView to display
    private fun prepareMultiRecordTypeAdapter(
        dateToTransactionHistoryItems: LinkedHashMap<String, MutableList<TransactionHistoryViewItem>>,
        adapter: TransactionHistoryAdapter) {

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

    // Start the activity for ATM location display
    override fun onItemClick(view: View, position: Int) {
        when (historyAdapter.histories[position - 1]) {
            is TransactionHistoryViewItem.Transaction -> {
                val description = (historyAdapter.histories[position - 1] as TransactionHistoryViewItem.Transaction).description
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
