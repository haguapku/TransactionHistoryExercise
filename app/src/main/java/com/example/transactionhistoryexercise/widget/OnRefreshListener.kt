package com.example.transactionhistoryexercise.widget

interface OnRefreshListener {

    fun onRefresh()
    fun onRefreshTimeout()

    fun onLoadMore()
    fun onLoreMoreTimeout()
}