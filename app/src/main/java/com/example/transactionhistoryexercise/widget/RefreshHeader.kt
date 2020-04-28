package com.example.transactionhistoryexercise.widget

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import com.example.transactionhistoryexercise.R


class RefreshHeader: LinearLayout, BaseRefreshHeader {

    private lateinit var container: RelativeLayout
    private lateinit var arrowImg: ImageView
    private lateinit var hintTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rotateUpAnimation: Animation
    private lateinit var rotateDownAnimation: Animation

    private var mState = STATE_NORMAL

    private var actualMeasuredHeight = 0

    companion object {
        const val STATE_NORMAL = 0
        const val STATE_RELEASE = 1
        const val STATE_REFRESHING = 2
        const val STATE_COMPLETE = 3
        const val ROTATE_ANIM_DURATION = 200L
    }

    constructor(context: Context): super(context)

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    init {
        init()
    }

    private fun init() {
        // Add header view
        container = LayoutInflater.from(context).inflate(R.layout.refresh_header, null) as RelativeLayout
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(container, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0))

        arrowImg = container.findViewById(R.id.PullToRefresh_Header_ArrowImageView)
        hintTextView = container.findViewById(R.id.PullToRefresh_Header_HintTextView)
        progressBar = container.findViewById(R.id.PullToRefresh_Header_ProgressBar)

        // Set animation of arrow
        rotateUpAnimation = RotateAnimation(0.0f, -180.0f,
            Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
        rotateUpAnimation.duration = ROTATE_ANIM_DURATION
        rotateUpAnimation.fillAfter = true
        rotateDownAnimation = RotateAnimation(-180.0f, 0.0f,
            Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
        rotateDownAnimation.duration = ROTATE_ANIM_DURATION
        rotateDownAnimation.fillAfter = true

        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        actualMeasuredHeight = measuredHeight
    }

    fun getVisibleHeight(): Int {
        return container.layoutParams.height
    }

    fun setVisibleHeight(height: Int) {
        val params = container.layoutParams as LayoutParams
        if (height < 0) {
            params.height = 0
        } else {
            params.height = height
        }
        container.layoutParams = params
    }

    override fun onMove(delta: Float) {
        if (getVisibleHeight() > 0 || delta > 0) {
            setVisibleHeight((getVisibleHeight() + delta).toInt())
            if ( mState <= STATE_RELEASE) {
                if (getVisibleHeight() > actualMeasuredHeight) {
                    onStateChange(STATE_RELEASE)
                } else {
                    onStateChange(STATE_NORMAL)
                }
            }
        }
    }

    private fun smoothScrollTo(destHeight: Int) {
        val animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight)
        animator.addUpdateListener { setVisibleHeight(it!!.animatedValue as Int) }
        animator.setDuration(300).start();
    }

    private fun reset() {
        smoothScrollTo(0)
        Handler().postDelayed({
            onStateChange(STATE_NORMAL)
        }, 500)
    }

    fun getState() = mState

    override fun onComplete() {
        onStateChange(STATE_COMPLETE)
        Handler().postDelayed({
            reset()
        }, 200)
    }

    override fun onRelease(): Boolean {
        var isOnRefresh: Boolean = false

        val height = getVisibleHeight()
        if (height == 0) {
            isOnRefresh = false
        }

        if (height >= actualMeasuredHeight && mState == STATE_RELEASE) {
            onStateChange(STATE_REFRESHING)
            isOnRefresh = true
        }

        if (mState != STATE_REFRESHING) {
            smoothScrollTo(0)
        }

        if (mState == STATE_REFRESHING) {
            smoothScrollTo(actualMeasuredHeight)
        }

        return isOnRefresh
    }

    override fun onStateChange(state: Int) {
        if (mState == state) {
            return
        }

        when (state) {
            STATE_NORMAL -> {
                arrowImg.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                hintTextView.text = resources.getText(R.string.PullToRefresh_Header_Hint_Normal)
//                if (mState == PullToRefreshState.STATE_REFRESHING) {
//                    arrowImg.clearAnimation()
//                }
                if (mState == STATE_RELEASE) {
                    arrowImg.clearAnimation()
                    arrowImg.startAnimation(rotateDownAnimation)
                }
            }
            STATE_RELEASE -> {
                arrowImg.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                if (mState == STATE_NORMAL) {
                    arrowImg.clearAnimation()
                    arrowImg.startAnimation(rotateUpAnimation)
                }
                hintTextView.text = resources.getText(R.string.PullToRefresh_Header_Hint_Release)
            }
            STATE_REFRESHING -> {
                arrowImg.clearAnimation()
                arrowImg.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                smoothScrollTo(actualMeasuredHeight)
                hintTextView.text = resources.getText(R.string.PullToRefresh_Header_Hint_Refreshing)
            }
            STATE_COMPLETE -> {
                arrowImg.visibility = View.GONE
                progressBar.visibility = View.GONE
                hintTextView.text = resources.getText(R.string.PullToRefresh_Header_Hint_Complete)
            }
        }

        mState = state
    }
}