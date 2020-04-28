package com.example.transactionhistoryexercise.widget

interface BaseRefreshHeader {

    fun onMove(delta: Float)
    fun onComplete()
    fun onRelease(): Boolean
    fun onStateChange(state: Int)
}