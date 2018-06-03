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
    private val button = Button(this.context).apply {
        this@CardLayout.addView(this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private var cutCount = 0

    init {
        setOnClickListener {
            animCut(
                    elevationDuration = 50L,
                    isHorizontal = true,
                    isClockwise = false,
                    is180 = true,
                    rotationXYDuration = 400L,
                    startDelay = 0L,
                    onCardCut = {
                        button.text = "$row-$column ${++cutCount}"
                    })
        }
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
     * 动画总时长 [elevationDuration] + [alphaRotationXYDuration].
     *
     * @param isIn 出现或消失.
     *
     * @param elevationDuration 高度动画时长.
     *
     * @param isHorizontal 水平或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param inOutDegrees 翻转角度.
     *
     * @param alphaRotationXYDuration 淡入和翻转动画时长.
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
            elevationDuration: Long,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            inOutDegrees: InOutDegrees,
            alphaRotationXYDuration: Long,
            startDelay: Long,
            onStart: (() -> Unit)? = null,
            onEnd: (() -> Unit)? = null,
            onCardFront: (() -> Unit)? = null,
            onCardBack: (() -> Unit)? = null) {
        val elevationFromValue = if (isIn) MAX_ELEVATION else DEF_ELEVATION
        val elevationToValue = if (isIn) DEF_ELEVATION else MAX_ELEVATION
        val elevationObjectAnimator = ObjectAnimator.ofFloat(this, "elevation", elevationFromValue, elevationToValue)
        elevationObjectAnimator.duration = elevationDuration
        elevationObjectAnimator.interpolator = AccelerateDecelerateInterpolator()

        val alphaFromValue = if (isIn) 0f else 1f
        val alphaToValue = if (isIn) 1f else 0f
        val alphaObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", alphaFromValue, alphaToValue)

        val rotationXYPropertyName = if (isHorizontal) "rotationY" else "rotationX"
        val rotationXYFromClockwiseSign = if (isClockwise) 1 else -1
        val rotationXYFromValue = if (isIn) rotationXYFromClockwiseSign * inOutDegrees.ordinal * 90f else 0f
        val rotationXYToClockwiseSign = -rotationXYFromClockwiseSign
        val rotationXYToValue = if (isIn) 0f else rotationXYToClockwiseSign * inOutDegrees.ordinal * 90f
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

                elevation = elevationFromValue
                alpha = alphaFromValue
                if (isHorizontal) rotationY = rotationXYFromValue else rotationX = rotationXYFromValue

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
     * 翻转出现或消失动画翻转角度.
     */
    private enum class InOutDegrees {
        D0,
        D90,
        D180,
        D270,
        D360
    }

    /**
     * 翻转切换动画.
     *
     * 如果为出现动画:
     *
     * 1. 高度从 [DEF_ELEVATION] 到 [MAX_ELEVATION]. 插值器为 [AccelerateDecelerateInterpolator].
     *
     * 2. 如果 180 度翻转, 翻转角度从 0f 到 +-90f, 插值器为 [AccelerateInterpolator], 然后从 -+270 到 0f, 插值器为
     * [DecelerateInterpolator]. 如果为 360 度翻转, 翻转角度从 0f 到 +- 360f, 插值器为 [AccelerateDecelerateInterpolator].
     *
     * 3. 高度从 [MAX_ELEVATION] 到 [DEF_ELEVATION]. 插值器为 [AccelerateDecelerateInterpolator].
     *
     * 卡片旋转到背面时隐藏子视图. 动画时不可点击.
     *
     * 动画总时长 [elevationDuration] * 2 + [alphaRotationXYDuration].
     *
     * @param elevationDuration 高度动画时长. 有升高和降低两部分.
     *
     * @param isHorizontal 水平或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param is180 180 度或 360 度翻转.
     *
     * @param rotationXYDuration 翻转动画时长.
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
     *
     * @param onCardCut 卡片切换回调.
     */
    private fun animCut(
            elevationDuration: Long,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            is180: Boolean,
            rotationXYDuration: Long,
            startDelay: Long,
            onStart: (() -> Unit)? = null,
            onEnd: (() -> Unit)? = null,
            onCardFront: (() -> Unit)? = null,
            onCardBack: (() -> Unit)? = null,
            onCardCut: (() -> Unit)? = null) {
        val elevationInFromValue = DEF_ELEVATION
        val elevationInObjectAnimator = ObjectAnimator.ofFloat(this, "elevation", elevationInFromValue, MAX_ELEVATION)
        elevationInObjectAnimator.duration = elevationDuration
        elevationInObjectAnimator.interpolator = AccelerateDecelerateInterpolator()

        val rotationXYPropertyName = if (isHorizontal) "rotationY" else "rotationX"
        val rotationXYFromValue = 0f
        var rotationXY180OutObjectAnimator: ObjectAnimator? = null
        var rotationXY180OutAnimatorListener: Animator.AnimatorListener? = null
        var rotationXY180InObjectAnimator: ObjectAnimator? = null
        var rotationXY360ObjectAnimator: ObjectAnimator? = null
        if (is180) {
            val rotationXY180OutToClockwiseSign = if (isClockwise) -1 else 1
            val rotationXY180OutToValue = rotationXY180OutToClockwiseSign * 90f
            rotationXY180OutObjectAnimator = ObjectAnimator.ofFloat(this, rotationXYPropertyName, rotationXYFromValue,
                    rotationXY180OutToValue)
            rotationXY180OutObjectAnimator.duration = rotationXYDuration / 2L
            rotationXY180OutObjectAnimator.interpolator = AccelerateInterpolator()
            rotationXY180OutAnimatorListener = object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)

                    onCardCut?.invoke()
                }
            }

            val rotationXY180InFromClockwiseSign = if (isClockwise) 1 else -1
            val rotationXY180InFromValue = rotationXY180InFromClockwiseSign * 90f
            rotationXY180InObjectAnimator = ObjectAnimator.ofFloat(this, rotationXYPropertyName,
                    rotationXY180InFromValue, 0f)
            rotationXY180InObjectAnimator.duration = rotationXYDuration - rotationXY180OutObjectAnimator.duration
            rotationXY180InObjectAnimator.interpolator = DecelerateInterpolator()
        } else {
            val rotationXY360ToClockwiseSign = if (isClockwise) -1 else 1
            val rotationXY360ToValue = rotationXY360ToClockwiseSign * 360f
            rotationXY360ObjectAnimator = ObjectAnimator.ofFloat(this, rotationXYPropertyName, rotationXYFromValue,
                    rotationXY360ToValue)
            rotationXY360ObjectAnimator.duration = rotationXYDuration
            rotationXY360ObjectAnimator.interpolator = AccelerateDecelerateInterpolator()
        }
        val rotationXYAnimatorUpdateListener = object : ValueAnimator.AnimatorUpdateListener {
            private var isFront: Boolean? = null

            private var isCut = false

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

                            if (animation === rotationXY360ObjectAnimator) {
                                if (!isCut) {
                                    isCut = true

                                    onCardCut?.invoke()
                                }
                            }
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

        val elevationOutObjectAnimator = ObjectAnimator.ofFloat(this, "elevation", MAX_ELEVATION, DEF_ELEVATION)
        elevationOutObjectAnimator.duration = elevationDuration
        elevationOutObjectAnimator.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        if (is180)
            animatorSet.playSequentially(elevationInObjectAnimator, rotationXY180OutObjectAnimator,
                    rotationXY180InObjectAnimator, elevationOutObjectAnimator)
        else
            animatorSet.playSequentially(elevationInObjectAnimator, rotationXY360ObjectAnimator,
                    elevationOutObjectAnimator)
        animatorSet.startDelay = startDelay
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)

                rotationXY180OutObjectAnimator?.addUpdateListener(rotationXYAnimatorUpdateListener)
                if (rotationXY180OutAnimatorListener != null) {
                    rotationXY180OutObjectAnimator?.addListener(rotationXY180OutAnimatorListener)
                }
                rotationXY180InObjectAnimator?.addUpdateListener(rotationXYAnimatorUpdateListener)
                rotationXY360ObjectAnimator?.addUpdateListener(rotationXYAnimatorUpdateListener)

                isClickable = false

                visibility = View.VISIBLE

                elevation = elevationInFromValue
                if (isHorizontal) rotationY = rotationXYFromValue else rotationX = rotationXYFromValue

                onStart?.invoke()
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                animatorSet.removeListener(this)

                rotationXY180OutObjectAnimator?.removeUpdateListener(rotationXYAnimatorUpdateListener)
                if (rotationXY180OutAnimatorListener != null) {
                    rotationXY180OutObjectAnimator?.removeListener(rotationXY180OutAnimatorListener)
                }
                rotationXY180InObjectAnimator?.removeUpdateListener(rotationXYAnimatorUpdateListener)
                rotationXY360ObjectAnimator?.removeUpdateListener(rotationXYAnimatorUpdateListener)

                isClickable = true

                onEnd?.invoke()
            }
        })
        animatorSet.start()
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
