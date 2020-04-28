package com.example.transactionhistoryexercise.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.transactionhistoryexercise.widget.RefreshHeader.Companion.STATE_REFRESHING
import com.example.transactionhistoryexercise.widget.RefreshHeader.Companion.STATE_RELEASE

@Suppress("UNCHECKED_CAST")
class PullToRefreshRecyclerView: RecyclerView {

    private var pullToRefreshEnabled: Boolean = true
    private var loadMoreEnabled: Boolean = true
    private var isLoadMore = false
    private var isNoMore = false

    private lateinit var wrapperAdapter: WrapAdapter
    lateinit var refreshHeader: RefreshHeader
    lateinit var loadMoreFooter: LoadMoreFooter
    private lateinit var dataObserver: AdapterDataObserver

    private var lastY:Float = -1f

    private lateinit var onRefreshListener: OnRefreshListener

    constructor(context: Context): super(context)

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    init {
        init()
    }

    companion object {
        const val TYPE_REFRESH_HEADER: Int = 10000
        const val TYPE_LOAD_MORE_FOOTER: Int = 10001
        const val DRAG_RATE: Float = 3f
        const val TIMEOUT: Long = 30000
        const val DEFAULT_DURATION: Long = 500
    }

    private fun init() {
        dataObserver = DataObserver()
        if (pullToRefreshEnabled) {
            refreshHeader = RefreshHeader(context)
        }
        if (loadMoreEnabled) {
            loadMoreFooter = LoadMoreFooter(context)
        }
    }

    private fun isOnTop(): Boolean = refreshHeader.parent != null

    fun reset() {
        refreshComplete()
        loadMoreComplete()
    }

    fun refreshComplete() {
        handler.removeCallbacksAndMessages(null)
        refreshHeader.onComplete()
        setNoMore(false)
    }

    fun loadMoreComplete() {
        handler.removeCallbacksAndMessages(null)
        isLoadMore = false
        loadMoreFooter.onStateChange(LoadMoreFooter.STATE_COMPLETE)
    }

    fun setNoMore(noMore: Boolean) {
        handler.removeCallbacksAndMessages(null)
        isLoadMore = false
        isNoMore = noMore
        loadMoreFooter.onStateChange(if (noMore) LoadMoreFooter.STATE_COMPLETE else LoadMoreFooter.STATE_LOADING)
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        wrapperAdapter = WrapAdapter(adapter as Adapter<ViewHolder>)
        super.setAdapter(wrapperAdapter)
        adapter.registerAdapterDataObserver(dataObserver)
        dataObserver.onChanged()
    }

    override fun getAdapter(): Adapter<*> {
        return wrapperAdapter.getOriginalAdapter()
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (lastY == -1f) {
            lastY = ev!!.rawY
        }
        when (ev!!.action) {
            MotionEvent.ACTION_DOWN -> lastY = ev.rawY
            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.rawY - lastY
                lastY = ev.rawY
                if (isOnTop() && pullToRefreshEnabled) {
                    refreshHeader.onMove(deltaY/ DRAG_RATE)
                    if (refreshHeader.getVisibleHeight() > 0 && refreshHeader.getState() <= STATE_RELEASE) {
                        return true
                    }
                }
            }
            else -> {
                lastY = -1f
                if (isOnTop() && pullToRefreshEnabled) {
                    if (refreshHeader.onRelease()) {
                        onRefreshListener.onRefresh()

                        handler.postDelayed(
                            {
                                onRefreshListener.onRefreshTimeout()
                                reset()
                            }, TIMEOUT
                        )
                    }
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE
            && !isLoadMore) {
            val lastVisibleItemPosition = when (layoutManager) {
                is GridLayoutManager -> {
                    (layoutManager as GridLayoutManager).findLastVisibleItemPosition()
                }
                is StaggeredGridLayoutManager -> {
                    val into = IntArray((layoutManager as StaggeredGridLayoutManager).spanCount)
                    (layoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(into)
                    findMaxSpan(into)
                }
                else -> {
                    (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                }
            }

            if (layoutManager!!.childCount > 0
                && lastVisibleItemPosition >= layoutManager!!.itemCount -1
                && layoutManager!!.itemCount >= layoutManager!!.childCount
                && !isNoMore && refreshHeader.getState() < STATE_REFRESHING) {
                isLoadMore = true
                loadMoreFooter.onStateChange(LoadMoreFooter.STATE_LOADING)
                onRefreshListener.onLoadMore()

                handler.postDelayed({
                    smoothScrollBy(0, -loadMoreFooter.getActualMeasureHeight())
                    onRefreshListener.onLoreMoreTimeout()
                    handler.postDelayed({
                        isLoadMore = false
                    }, DEFAULT_DURATION)

                }, TIMEOUT)
            }
        }

    }

    private fun findMaxSpan(lastPosition: IntArray): Int {
        var max = lastPosition[0]
        for (value in lastPosition) {
            if (value > max) {
                max = value
            }
        }
        return max
    }

    inner class WrapAdapter(private val adapter: Adapter<ViewHolder>): RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return when (viewType) {
                TYPE_REFRESH_HEADER -> {
                    SimpleViewHolder(refreshHeader)
                }
                TYPE_LOAD_MORE_FOOTER -> {
                    SimpleViewHolder(loadMoreFooter)
                }
                else -> {
                    adapter.onCreateViewHolder(parent, viewType)
                }
            }
        }

        override fun getItemCount(): Int {
            return adapter.itemCount + 2
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (isRefreshHeader(position) || isLoadMoreFooter(position)) {
                return
            }
            val adjPosition = position - 1;
            if (adjPosition < adapter.itemCount) {
                adapter.onBindViewHolder(holder, adjPosition)
            }
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (isRefreshHeader(position) || isLoadMoreFooter(position)) {
                return
            }
            val adjPosition = position - 1;
            if (adjPosition < adapter.itemCount) {
                adapter.onBindViewHolder(holder, adjPosition, payloads)
            }
        }

        override fun getItemId(position: Int): Long {
            if (position >= 1) {
                val adjPosition = position - 1
                if (adjPosition < adapter.itemCount) {
                    return adapter.getItemId(adjPosition)
                }
            }
            return -1
        }

        override fun getItemViewType(position: Int): Int {
            val adjPosition = position - 1
            if (isRefreshHeader(position)) {
                return TYPE_REFRESH_HEADER
            }
            if (isLoadMoreFooter(position)) {
                return TYPE_LOAD_MORE_FOOTER
            }
            if (adjPosition < adapter.itemCount) {
                return adapter.getItemViewType(adjPosition)
            }
            return 0
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            if (layoutManager is GridLayoutManager) {
                (layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (isRefreshHeader(position)) {
                            (layoutManager as GridLayoutManager).spanCount
                        } else {
                            1
                        }
                    }
                }
            }
            adapter.onAttachedToRecyclerView(recyclerView)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            adapter.onDetachedFromRecyclerView(recyclerView)
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            super.onViewAttachedToWindow(holder)
            val layoutParams = holder.itemView.layoutParams
            if (layoutParams != null
                && layoutParams is StaggeredGridLayoutManager.LayoutParams
                && isRefreshHeader(holder.layoutPosition)) {
                layoutParams.isFullSpan = true
            }
            adapter.onViewAttachedToWindow(holder)
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            adapter.onViewDetachedFromWindow(holder)
        }

        override fun onViewRecycled(holder: ViewHolder) {
            adapter.onViewRecycled(holder)
        }

        override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
            return adapter.onFailedToRecycleView(holder)
        }

        override fun registerAdapterDataObserver(observer: AdapterDataObserver) {
            adapter.registerAdapterDataObserver(observer)
        }

        override fun unregisterAdapterDataObserver(observer: AdapterDataObserver) {
            adapter.unregisterAdapterDataObserver(observer)
        }

        fun getOriginalAdapter() = adapter

        private fun isRefreshHeader(position: Int): Boolean {
            return position == 0
        }

        private fun isLoadMoreFooter(position: Int): Boolean {
            return position == itemCount - 1
        }

        inner class SimpleViewHolder(private val itemView: View): RecyclerView.ViewHolder(itemView)
    }

    inner class DataObserver: AdapterDataObserver() {

        override fun onChanged() {
            wrapperAdapter.notifyDataSetChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            wrapperAdapter.notifyItemMoved(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            wrapperAdapter.notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            wrapperAdapter.notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            wrapperAdapter.notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            wrapperAdapter.notifyItemRangeChanged(positionStart, itemCount, payload)
        }
    }

    fun isPullToRefreshEnabled() = pullToRefreshEnabled

    fun setPullToRefreshEnabled(pullToRefreshEnabled: Boolean) {
        this.pullToRefreshEnabled = pullToRefreshEnabled
    }

    fun setLoadMoreEnable(loadMoreEnabled: Boolean) {
        this.loadMoreEnabled = loadMoreEnabled
        if (!loadMoreEnabled) {
            loadMoreFooter.visibility = View.GONE
        }
    }

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        this.onRefreshListener = onRefreshListener
    }


}