package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

/**
 * 基础卡片.
 */
abstract class BaseCard(context: Context) : BaseCardLayout(context) {
    protected val card16Layout get() = parent as Card16Layout

    /**
     * 默认高度.
     */
    protected abstract val defElevation: Float
    /**
     * 最大高度.
     */
    protected abstract val maxElevation: Float

    /**
     * 卡片出现动画.
     *
     * 开始状态: 高度为 [maxElevation].
     *
     * 动画过程: 从 +-90 度或 +-270 度减速翻转到 0 度, 然后高度加速减速降低.
     *
     * 动画时长: [rotationDuration] + [ELEVATION_DURATION].
     *
     * @param isHorizontal 水平方向或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param hasBack 卡片翻转时是否有背面 (空白面).
     *
     * @param rotationDuration 翻转动画时长.
     */
    protected fun animateIn(
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasBack: Boolean,
            rotationDuration: Long) {
        AnimatorSet().apply {
            val rotationAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "rotationY" else "rotationX"
                val valueFromSign = if (isClockwise) 1 else -1
                val valueFromDegree = if (hasBack) 270f else 90f
                val valueFrom = valueFromSign * valueFromDegree
                val valueTo = 0f
                setFloatValues(valueFrom, valueTo)
                duration = rotationDuration
                interpolator = DecelerateInterpolator()
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            val elevationAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val valueFrom = maxElevation
                val valueTo = defElevation
                setFloatValues(valueFrom, valueTo)
                duration = ELEVATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }
            playSequentially(rotationAnimator, elevationAnimator)
            val animatorListener = CardAnimatorListener()
            addListener(animatorListener)
            setTarget(this@BaseCard)
        }.start()
    }

    /**
     * 卡片消失动画.
     *
     * 开始状态: 翻转角度为 0 度.
     *
     * 动画过程: 高度加速减速升高, 然后从 0 度加速翻转到 +-90 度或 +-270 度.
     *
     * 结束状态: 移除当前卡片.
     *
     * 动画时长: [ELEVATION_DURATION] + [rotationDuration].
     *
     * @param isHorizontal 水平方向或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param hasBack 卡片翻转时是否有背面 (空白面).
     *
     * @param rotationDuration 翻转动画时长.
     */
    protected fun animateOut(
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasBack: Boolean,
            rotationDuration: Long) {
        AnimatorSet().apply {
            val elevationAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val valueFrom = defElevation
                val valueTo = maxElevation
                setFloatValues(valueFrom, valueTo)
                duration = ELEVATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }
            val rotationAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "rotationY" else "rotationX"
                val valueFrom = 0f
                val valueToSign = if (isClockwise) -1 else 1
                val valueToDegree = if (hasBack) 270f else 90f
                val valueTo = valueToSign * valueToDegree
                setFloatValues(valueFrom, valueTo)
                duration = rotationDuration
                interpolator = AccelerateInterpolator()
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            playSequentially(elevationAnimator, rotationAnimator)
            val animatorListener = CardAnimatorListener(onEnd = {
                // TODO
            })
            addListener(animatorListener)
            setTarget(this@BaseCard)
        }.start()
    }

    /**
     * 卡片切换动画.
     *
     * 开始状态: 翻转角度为 0 度.
     *
     * 动画过程: 高度加速减速升高, 然后从 0 度加速翻转到 +-90 度或 +-180 度, 然后从 +-270 度或 +-180 减速翻转到 0 度,
     * 然后高度加速减速降低.
     *
     * 动画时长: 2 * [ELEVATION_DURATION] + [rotationDuration].
     *
     * @param isHorizontal 水平方向或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param hasBack 卡片翻转时是否有背面 (空白面).
     *
     * @param rotationDuration 翻转动画时长. 包括两个动画过程.
     *
     * @param onCardCut 卡片切换回调.
     */
    protected fun animateCut(
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasBack: Boolean,
            rotationDuration: Long,
            onCardCut: (() -> Unit)? = null) {
        AnimatorSet().apply {
            val elevationInAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val valueFrom = defElevation
                val valueTo = maxElevation
                setFloatValues(valueFrom, valueTo)
                duration = ELEVATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }
            val rotationOutAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "rotationY" else "rotationX"
                val valueFrom = 0f
                val valueToSign = if (isClockwise) -1 else 1
                val valueToDegree = if (hasBack) 180f else 90f
                val valueTo = valueToSign * valueToDegree
                setFloatValues(valueFrom, valueTo)
                duration = rotationDuration / 2L
                interpolator = AccelerateInterpolator()
                val animatorListener = object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                        onCardCut?.invoke()
                    }
                }
                addListener(animatorListener)
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            val rotationInAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "rotationY" else "rotationX"
                val valueFromSign = if (isClockwise) 1 else -1
                val valueFromDegree = if (hasBack) 180f else 90f
                val valueFrom = valueFromSign * valueFromDegree
                val valueTo = 0f
                setFloatValues(valueFrom, valueTo)
                duration = rotationDuration - rotationDuration / 2L
                interpolator = DecelerateInterpolator()
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            val elevationOutAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val valueFrom = maxElevation
                val valueTo = defElevation
                setFloatValues(valueFrom, valueTo)
                duration = ELEVATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }
            playSequentially(elevationInAnimator, rotationOutAnimator, rotationInAnimator, elevationOutAnimator)
            val animatorListener = CardAnimatorListener()
            addListener(animatorListener)
            setTarget(this@BaseCard)
        }.start()
    }

    /**
     * 动画开始时设置全部卡片和大卡片不可点击. 动画结束时设置全部卡片和大卡片可点击.
     */
    protected open inner class CardAnimatorListener(onStart: ((Animator) -> Unit)? = null,
            onEnd: ((Animator) -> Unit)? = null) : CardLayoutAnimatorListener(onStart, onEnd) {
        override fun onStart(animator: Animator) {
            super.onStart(animator)

            // TODO
            card16Layout.setAllCardsClickable(false)
            card16Layout.getBigCard().isClickable = false
        }

        override fun onEnd(animator: Animator) {
            super.onEnd(animator)

            // TODO
            card16Layout.setAllCardsClickable(true)
            card16Layout.getBigCard().isClickable = true
        }
    }

    companion object {
        /**
         * 高度动画时长.
         */
        private const val ELEVATION_DURATION = 50L
    }
}
