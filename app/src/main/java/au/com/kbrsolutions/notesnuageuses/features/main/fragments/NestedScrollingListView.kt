package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.util.AttributeSet
import android.widget.ListView

/**
 * Add ScrollingViewBehavior for ListView.
 *
 * From stackoverflow - 'I love coding'
 * http://stackoverflow.com/questions/30612453/scrollingviewbehavior-for-listview
 */

class NestedScrollingListView: ListView, NestedScrollingChild {
    private var mNestedScrollingChildHelper: NestedScrollingChildHelper? = null

    constructor(context: Context): super(context) {
        initHelper()
    }


    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        initHelper()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr) {
        initHelper()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int):
        super(context, attrs, defStyleAttr, defStyleRes) {
        initHelper()
    }

    private fun initHelper() {
        mNestedScrollingChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mNestedScrollingChildHelper!!.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mNestedScrollingChildHelper!!.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mNestedScrollingChildHelper!!.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mNestedScrollingChildHelper!!.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mNestedScrollingChildHelper!!.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            offsetInWindow: IntArray?): Boolean {
        return mNestedScrollingChildHelper!!.dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                offsetInWindow)
    }

    override fun dispatchNestedPreScroll(
            dx: Int,
            dy: Int,
            consumed: IntArray?,
            offsetInWindow: IntArray?
    ): Boolean {
        return mNestedScrollingChildHelper!!.dispatchNestedPreScroll(
                dx,
                dy,
                consumed,
                offsetInWindow)
    }

    override fun dispatchNestedFling(
            velocityX: Float,
            velocityY: Float,
            consumed: Boolean): Boolean {
        return mNestedScrollingChildHelper!!.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mNestedScrollingChildHelper!!.dispatchNestedPreFling(velocityX, velocityY)
    }
}