package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import com.ebnbin.eb.util.dp

/**
 * 卡片布局.
 */
class CardLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        CardView(context, attrs, defStyleAttr) {
    init {
        setOnClickListener { animIn(true, false, Degrees.D90, 400L, 100L) }
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

    /**
     * 翻转出现动画.
     *
     * 1. 透明度从 0f 到 1f, 同时翻转. 插值器为 [DecelerateInterpolator].
     *
     * 2. 高度从 [MAX_ELEVATION] 到 [DEF_ELEVATION]. 插值器为 [AccelerateDecelerateInterpolator].
     *
     * 卡片旋转到背面时隐藏子视图. 动画时不可点击.
     *
     * @param isHorizontal 水平或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param degrees 翻转角度.
     *
     * @param alphaRotationXYDuration 淡入和翻转动画时长.
     *
     * @param elevationDuration 降低高度动画时长.
     */
    private fun animIn(isHorizontal: Boolean, isClockwise: Boolean, degrees: Degrees, alphaRotationXYDuration: Long,
            elevationDuration: Long) {
        val alphaFromValue = 0f
        val alphaObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", alphaFromValue, 1f)

        val rotationXYPropertyName = if (isHorizontal) "rotationY" else "rotationX"
        val rotationXYFromValue = (if (isClockwise) 1 else -1) * degrees.ordinal * 90f
        val rotationXYObjectAnimator = ObjectAnimator.ofFloat(this, rotationXYPropertyName, rotationXYFromValue, 0f)
        val rotationXYAnimatorUpdateListener = object : ValueAnimator.AnimatorUpdateListener {
            private var isFront: Boolean? = null

            override fun onAnimationUpdate(animation: ValueAnimator?) {
                val rotationXY = animation?.animatedValue as Float? ?: return
                val validRotationXY = (rotationXY % 360f + 360f) % 360f
                when (validRotationXY) {
                    0f, 90f, 180f, 270f -> Unit
                    in 90f..270f -> {
                        if (isFront != false) {
                            isFront = false
                            getChildAt(0)?.visibility = View.GONE
                        }
                    }
                    else -> {
                        if (isFront != true) {
                            isFront = true
                            getChildAt(0)?.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        val alphaRotationXYAnimatorSet = AnimatorSet()
        alphaRotationXYAnimatorSet.playTogether(alphaObjectAnimator, rotationXYObjectAnimator)
        alphaRotationXYAnimatorSet.duration = alphaRotationXYDuration
        alphaRotationXYAnimatorSet.interpolator = DecelerateInterpolator()

        val elevationFromValue = MAX_ELEVATION
        val elevationObjectAnimator = ObjectAnimator.ofFloat(this, "elevation", elevationFromValue, DEF_ELEVATION)
        elevationObjectAnimator.duration = elevationDuration
        elevationObjectAnimator.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(alphaRotationXYAnimatorSet, elevationObjectAnimator)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)

                rotationXYObjectAnimator.addUpdateListener(rotationXYAnimatorUpdateListener)

                isClickable = false

                visibility = View.VISIBLE

                alpha = alphaFromValue
                if (isHorizontal) rotationY = rotationXYFromValue else rotationX = rotationXYFromValue
                elevation = elevationFromValue
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                animatorSet.removeListener(this)

                rotationXYObjectAnimator.removeUpdateListener(rotationXYAnimatorUpdateListener)

                isClickable = true
            }
        })
        animatorSet.start()
    }

    /**
     * 翻转角度.
     */
    private enum class Degrees {
        D0,
        D90,
        D180,
        D270,
        D360
    }

    companion object {
        /**
         * 默认高度.
         */
        private val DEF_ELEVATION = 2f.dp
        /**
         * 最大高度.
         */
        private val MAX_ELEVATION = 8f.dp
    }
}
