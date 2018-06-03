package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button

/**
 * 卡片布局.
 */
class CardLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        CardView(context, attrs, defStyleAttr) {
    init {
        setOnClickListener { anim() }
    }

    private val button = Button(this.context).apply {
        this@CardLayout.addView(this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    /**
     * 行.
     */
    var row = 0
        private set
    /**
     * 列.
     */
    var column = 0
        private set

    fun setIndex(row: Int, column: Int) {
        this.row = row
        this.column = column

        button.text = "$row-$column"
    }

    private fun anim() {
        val rotationYObjectAnimator = ObjectAnimator.ofFloat(this, "rotationY", 0f, 90f)
        rotationYObjectAnimator.duration = 250L
        rotationYObjectAnimator.interpolator = AccelerateInterpolator()
        val rotationYObjectAnimator2 = ObjectAnimator.ofFloat(this, "rotationY", -90f, 0f)
        rotationYObjectAnimator2.duration = 250L
        rotationYObjectAnimator2.interpolator = DecelerateInterpolator()
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(rotationYObjectAnimator, rotationYObjectAnimator2)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                animatorSet.removeListener(this)
            }
        })
        animatorSet.start()
    }
}
