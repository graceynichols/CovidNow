package com.example.covidnow.helpers

import androidx.recyclerview.widget.RecyclerView.OnFlingListener

open class RecyclerViewSwipeListener // change swipe listener depending on whether we are scanning items horizontally or vertically
protected constructor(var mIsScrollingVertically: Boolean) : OnFlingListener() {
    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
        if (mIsScrollingVertically && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
            if (velocityY < 0) {
                onSwipeDown()
            } else {
                onSwipeUp()
            }
            return true
        } else if (!mIsScrollingVertically && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (velocityX < 0) {
                onSwipeLeft()
            } else {
                onSwipeRight()
            }
            return true
        }
        return false
    }

    fun onSwipeRight() {}
    fun onSwipeLeft() {}
    open fun onSwipeUp() {}
    open fun onSwipeDown() {}

    companion object {
        private const val SWIPE_VELOCITY_THRESHOLD = 2000
    }

}