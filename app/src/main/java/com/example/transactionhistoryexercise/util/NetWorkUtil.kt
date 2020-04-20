package com.example.transactionhistoryexercise.util

import android.content.Context
import android.net.ConnectivityManager
import com.example.transactionhistoryexercise.MainApplication

class NetWorkUtil {
    companion object {
        fun isNetWorkConnected(): Boolean {
            val cm = MainApplication.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val currentNet = cm.activeNetworkInfo ?: return false
            return currentNet.isAvailable
        }
    }
}