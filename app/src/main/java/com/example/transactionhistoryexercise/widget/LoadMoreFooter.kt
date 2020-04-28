package com.example.transactionhistoryexercise.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.transactionhistoryexercise.R

class LoadMoreFooter: LinearLayout {

    private lateinit var container: LinearLayout
    private lateinit var hintTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var actualMeasuredHeight = 0

    companion object {
        const val STATE_LOADING = 0
        const val STATE_COMPLETE = 1
        const val STATE_NO_MORE = 2
    }

    constructor(context: Context): super(context)

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    init {
        init()
    }

    private fun init() {
        container = LayoutInflater.from(context).inflate(R.layout.load_more_footer, null) as LinearLayout
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(container, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        hintTextView = container.findViewById(R.id.LoadingMoreFooter_HintTextView)
        progressBar = container.findViewById(R.id.LoadingMoreFooter_ProgressBar)

        measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        actualMeasuredHeight = measuredHeight
    }

    fun onStateChange(state: Int) {
        when (state) {
            STATE_LOADING -> {
                progressBar.visibility = View.VISIBLE
                hintTextView.text = resources.getText(R.string.PullUpToLoadMore_Footer_Hint_Loading)
            }
            STATE_COMPLETE -> {
                progressBar.visibility = View.GONE
                hintTextView.text = resources.getText(R.string.PullUpToLoadMore_Footer_Hint_Complete)
            }
            else -> {
                progressBar.visibility = View.GONE
                hintTextView.text = resources.getText(R.string.PullUpToLoadMore_Footer_Hint_No_More)
            }
        }
    }

    fun getActualMeasureHeight(): Int = actualMeasuredHeight
}