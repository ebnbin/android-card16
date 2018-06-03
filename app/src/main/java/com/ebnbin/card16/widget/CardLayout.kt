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
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import com.ebnbin.eb.util.dp

/**
 * 卡片布局.
 */
class CardLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        CardView(context, attrs, defStyleAttr) {
    init {
        setOnClickListener {
            animInOut(
                    isIn = false,
                    isHorizontal = true,
                    isClockwise = false,
                    degrees = Degrees.D90,
                    alphaRotationXYDuration = 400L,
                    elevationDuration = 100L,
                    startDelay = 0L,
                    onEnd = {
                        animInOut(
                                isIn = true,
                                isHorizontal = true,
                                isClockwise = false,
                                degrees = Degrees.D90,
                                alphaRotationXYDuration = 400L,
                                elevationDuration = 100L,
                                startDelay = 0L)
                    })
        }
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
     * 翻转出现或消失动画.
     *
     * 如果为出现动画:
     *
     * 1. 透明度从 0f 到 1f, 同时翻转. 插值器为 [DecelerateInterpolator].
     *
     * 2. 高度从 [MAX_ELEVATION] 到 [DEF_ELEVATION]. 插值器为 [AccelerateDecelerateInterpolator].
     *
     * 3. 动画完成后显示视图.
     *
     * 如果为消失动画:
     *
     * 1. 高度从 [DEF_ELEVATION] 到 [MAX_ELEVATION]. 插值器为 [AccelerateDecelerateInterpolator].
     *
     * 2. 透明度从 1f 到 0f, 同时翻转. 插值器为 [AccelerateInterpolator].
     *
     * 3. 动画完成后隐藏视图.
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
     * @param elevationDuration 高度动画时长.
     *
     * @param startDelay 动画延时.
     *
     * @param onStart 动画开始回调.
     *
     * @param onEnd 动画结束回调.
     *
     * @param onCardFront 卡片翻转到正面回调.
     *
     * @param onCardBack 卡片翻转到背面回调.
     */
    private fun animInOut(
            isIn: Boolean,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            degrees: Degrees,
            alphaRotationXYDuration: Long,
            elevationDuration: Long,
            startDelay: Long,
            onStart: (() -> Unit)? = null,
            onEnd: (() -> Unit)? = null,
            onCardFront: (() -> Unit)? = null,
            onCardBack: (() -> Unit)? = null) {
        val alphaFromValue = if (isIn) 0f else 1f
        val alphaToValue = if (isIn) 1f else 0f
        val alphaObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", alphaFromValue, alphaToValue)

        val rotationXYPropertyName = if (isHorizontal) "rotationY" else "rotationX"
        val rotationXYFromClockwiseSign = if (isClockwise) 1 else -1
        val rotationXYFromValue = if (isIn) rotationXYFromClockwiseSign * degrees.ordinal * 90f else 0f
        val rotationXYToClockwiseSign = -rotationXYFromClockwiseSign
        val rotationXYToValue = if (isIn) 0f else rotationXYToClockwiseSign * degrees.ordinal * 90f
        val rotationXYObjectAnimator = ObjectAnimator.ofFloat(this, rotationXYPropertyName, rotationXYFromValue,
                rotationXYToValue)
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

                            onCardBack?.invoke()
                        }
                    }
                    else -> {
                        if (isFront != true) {
                            isFront = true
                            getChildAt(0)?.visibility = View.VISIBLE

                            onCardFront?.invoke()
                        }
                    }
                }
            }
        }

        val alphaRotationXYAnimatorSet = AnimatorSet()
        alphaRotationXYAnimatorSet.playTogether(alphaObjectAnimator, rotationXYObjectAnimator)
        alphaRotationXYAnimatorSet.duration = alphaRotationXYDuration
        alphaRotationXYAnimatorSet.interpolator = if (isIn) DecelerateInterpolator() else AccelerateInterpolator()

        val elevationFromValue = if (isIn) MAX_ELEVATION else DEF_ELEVATION
        val elevationToValue = if (isIn) DEF_ELEVATION else MAX_ELEVATION
        val elevationObjectAnimator = ObjectAnimator.ofFloat(this, "elevation", elevationFromValue, elevationToValue)
        elevationObjectAnimator.duration = elevationDuration
        elevationObjectAnimator.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        if (isIn)
            animatorSet.playSequentially(alphaRotationXYAnimatorSet, elevationObjectAnimator)
        else
            animatorSet.playSequentially(elevationObjectAnimator, alphaRotationXYAnimatorSet)
        animatorSet.startDelay = startDelay
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)

                rotationXYObjectAnimator.addUpdateListener(rotationXYAnimatorUpdateListener)

                isClickable = false

                visibility = View.VISIBLE

                alpha = alphaFromValue
                if (isHorizontal) rotationY = rotationXYFromValue else rotationX = rotationXYFromValue
                elevation = elevationFromValue

                onStart?.invoke()
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                animatorSet.removeListener(this)

                rotationXYObjectAnimator.removeUpdateListener(rotationXYAnimatorUpdateListener)

                isClickable = true

                visibility = if (isIn) View.VISIBLE else View.GONE

                onEnd?.invoke()
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
