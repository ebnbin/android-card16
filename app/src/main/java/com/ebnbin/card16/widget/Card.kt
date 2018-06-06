package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import com.ebnbin.eb.util.dp
import com.ebnbin.eb.view.getCenterX
import com.ebnbin.eb.view.getCenterY

/**
 * 卡片.
 */
class Card(context: Context) : BaseCardLayout(context) {
    private val button = Button(this.context).apply {
        this@Card.addView(this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    init {
        setOnClickListener {
            if (row % 2 == column % 2) {
                animZoom(
                        isZoomIn = true,
                        elevationDuration = 50L,
                        isHorizontal = false,
                        isClockwise = false,
                        is180 = true,
                        rotationXYDuration = 200L,
                        startDelay = 0L)
            } else {
                animCut(
                        elevationDuration = 50L,
                        isHorizontal = true,
                        isClockwise = false,
                        is180 = true,
                        rotationXYDuration = 400L,
                        startDelay = 0L)
            }
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

                getCard16Layout()?.setAllCardsClickable(false)

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

                getCard16Layout()?.setAllCardsClickable(true)

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
     * 动画总时长 [elevationDuration] * 2 + [rotationXYDuration].
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
     * @param isFromSmallToBig 如果为空则不做位移和缩放. 如果为 true 则从小卡片变换成大卡片. 如果为 false 则从大卡片变换成小卡片.
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
        val rotationXYOutFromValue = 0f
        val rotationXYOutToClockwiseSign = if (isClockwise) -1 else 1
        val rotationXYOutDegrees = if (is180) 90f else 180f
        val rotationXYOutToValue = rotationXYOutToClockwiseSign * rotationXYOutDegrees
        val rotationXYOutObjectAnimator = ObjectAnimator.ofFloat(this, rotationXYPropertyName, rotationXYOutFromValue,
                rotationXYOutToValue)
        rotationXYOutObjectAnimator.duration = rotationXYDuration / 2L
        rotationXYOutObjectAnimator.interpolator = AccelerateInterpolator()
        val rotationXYOutAnimatorListener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                onCardCut?.invoke()
            }
        }

        val rotationXYInFromClockwiseSign = -rotationXYOutToClockwiseSign
        val rotationXYInFromValue = rotationXYInFromClockwiseSign * rotationXYOutDegrees
        val rotationXYInObjectAnimator = ObjectAnimator.ofFloat(this, rotationXYPropertyName, rotationXYInFromValue,
                0f)
        rotationXYInObjectAnimator.duration = rotationXYDuration - rotationXYOutObjectAnimator.duration
        rotationXYInObjectAnimator.interpolator = DecelerateInterpolator()

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

        val rotationXYAnimatorSet = AnimatorSet()
        rotationXYAnimatorSet.playSequentially(rotationXYOutObjectAnimator, rotationXYInObjectAnimator)

        val elevationOutObjectAnimator = ObjectAnimator.ofFloat(this, "elevation", MAX_ELEVATION, DEF_ELEVATION)
        elevationOutObjectAnimator.duration = elevationDuration
        elevationOutObjectAnimator.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(elevationInObjectAnimator, rotationXYAnimatorSet, elevationOutObjectAnimator)
        animatorSet.startDelay = startDelay
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)

                rotationXYOutObjectAnimator.addListener(rotationXYOutAnimatorListener)
                rotationXYOutObjectAnimator.addUpdateListener(rotationXYAnimatorUpdateListener)
                rotationXYInObjectAnimator.addUpdateListener(rotationXYAnimatorUpdateListener)

                getCard16Layout()?.setAllCardsClickable(false)

                visibility = View.VISIBLE

                elevation = elevationInFromValue
                if (isHorizontal) rotationY = rotationXYOutFromValue else rotationX = rotationXYOutFromValue

                onStart?.invoke()
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                animatorSet.removeListener(this)

                rotationXYOutObjectAnimator.removeListener(rotationXYOutAnimatorListener)
                rotationXYOutObjectAnimator.removeUpdateListener(rotationXYAnimatorUpdateListener)
                rotationXYInObjectAnimator.removeUpdateListener(rotationXYAnimatorUpdateListener)

                getCard16Layout()?.setAllCardsClickable(true)

                onEnd?.invoke()
            }
        })
        animatorSet.start()
    }

    fun animZoom(
            isZoomIn: Boolean,
            elevationDuration: Long,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            is180: Boolean,
            rotationXYDuration: Long,
            startDelay: Long,
            onStart: (() -> Unit)? = null,
            onEnd: (() -> Unit)? = null,
            onCardFront: (() -> Unit)? = null,
            onCardBack: (() -> Unit)? = null) {
        val elevationFromValue = if (isZoomIn) DEF_ELEVATION else MAX_ELEVATION
        val elevationToValue = if (isZoomIn) MAX_ELEVATION else DEF_ELEVATION
        val elevationObjectAnimator = ObjectAnimator.ofFloat(this, "elevation", elevationFromValue, elevationToValue)
        elevationObjectAnimator.duration = elevationDuration
        elevationObjectAnimator.interpolator = AccelerateDecelerateInterpolator()

        val rotationXYPropertyName = if (isHorizontal) "rotationY" else "rotationX"
        val rotationXYDegrees = if (is180) 90f else 180f
        val rotationXYFromClockwiseSign = if (isClockwise) 1 else -1
        val rotationXYFromValue = if (isZoomIn) 0f else rotationXYFromClockwiseSign * rotationXYDegrees
        val rotationXYToClockwiseSign = -rotationXYFromClockwiseSign
        val rotationXYToValue = if (isZoomIn) rotationXYToClockwiseSign * rotationXYDegrees else 0f
        val rotationXYObjectAnimator = ObjectAnimator.ofFloat(this, rotationXYPropertyName, rotationXYFromValue,
                rotationXYToValue)
        rotationXYObjectAnimator.duration = rotationXYDuration
        rotationXYObjectAnimator.interpolator = if (isZoomIn) AccelerateInterpolator() else DecelerateInterpolator()
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

        val translationXFromValue = if (isZoomIn) 0f else (getCard16Layout().getCenterX() - getCenterX()) / 2f
        val translationXToValue = if (isZoomIn) (getCard16Layout().getCenterX() - getCenterX()) / 2f else 0f
        val translationXObjectAnimator = ObjectAnimator.ofFloat(this, "translationX", translationXFromValue,
                translationXToValue)

        val translationYFromValue = if (isZoomIn) 0f else (getCard16Layout().getCenterY() - getCenterY()) / 2f
        val translationYToValue = if (isZoomIn) (getCard16Layout().getCenterY() - getCenterY()) / 2f else 0f
        val translationYObjectAnimator = ObjectAnimator.ofFloat(this, "translationY", translationYFromValue,
                translationYToValue)

        val translationZFromValue = if (isZoomIn) 0f else 1f
        val translationZToValue = if (isZoomIn) 1f else 0f
        val translationZObjectAnimator = ObjectAnimator.ofFloat(this, "translationZ", translationZFromValue,
                translationZToValue)

        val cardScale = getCard16Layout()?.scaleIn ?: 1f
        val scaleFromValue = if (isZoomIn) 1f else (cardScale - 1f) / 2f + 1f
        val scaleToValue = if (isZoomIn) (cardScale - 1f) / 2f + 1f else 1f
        val scaleXObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX", scaleFromValue, scaleToValue)

        val scaleYObjectAnimator = ObjectAnimator.ofFloat(this, "scaleY", scaleFromValue, scaleToValue)

        // TODO: elevation.

        val rotationXYTranslationScaleAnimatorSet = AnimatorSet()
        rotationXYTranslationScaleAnimatorSet.playTogether(rotationXYObjectAnimator, translationXObjectAnimator,
                translationYObjectAnimator, translationZObjectAnimator, scaleXObjectAnimator, scaleYObjectAnimator)
        rotationXYTranslationScaleAnimatorSet.duration = rotationXYDuration
        rotationXYTranslationScaleAnimatorSet.interpolator =
                if (isZoomIn) AccelerateInterpolator() else DecelerateInterpolator()

        val animatorSet = AnimatorSet()
        if (isZoomIn)
            animatorSet.playSequentially(elevationObjectAnimator, rotationXYTranslationScaleAnimatorSet)
        else
            animatorSet.playSequentially(rotationXYTranslationScaleAnimatorSet, elevationObjectAnimator)
        animatorSet.startDelay = startDelay
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)

                rotationXYObjectAnimator.addUpdateListener(rotationXYAnimatorUpdateListener)

                getCard16Layout()?.setAllCardsClickable(false)

                visibility = View.VISIBLE

                elevation = elevationFromValue
                if (isHorizontal) rotationY = rotationXYFromValue else rotationX = rotationXYFromValue
                translationX = translationXFromValue
                translationY = translationYFromValue
                translationZ = translationZFromValue
                scaleX = scaleFromValue
                scaleY = scaleFromValue

                onStart?.invoke()
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                animatorSet.removeListener(this)

                rotationXYObjectAnimator.removeUpdateListener(rotationXYAnimatorUpdateListener)

                getCard16Layout()?.setAllCardsClickable(true)

                visibility = if (isZoomIn) View.GONE else View.VISIBLE

                if (isZoomIn) {
                    getCard16Layout()?.getBigCard()?.animZoom(
                            card = this@Card,
                            isZoomIn = isZoomIn,
                            elevationDuration = elevationDuration,
                            isHorizontal = isHorizontal,
                            isClockwise = isClockwise,
                            is180 = is180,
                            rotationXYDuration = rotationXYDuration,
                            startDelay = startDelay)
                }

                onEnd?.invoke()
            }
        })
        animatorSet.start()
    }

    private fun getCard16Layout() = parent as? Card16Layout

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
