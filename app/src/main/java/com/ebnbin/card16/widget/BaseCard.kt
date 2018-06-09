package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.CardView
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

/**
 * 基础卡片.
 *
 * @param defElevation 默认高度.
 *
 * @param defRadius 默认圆角.
 */
abstract class BaseCard(context: Context, private val defElevation: Float, val defRadius: Float) : CardView(context) {
    protected val card16Layout get() = parent as Card16Layout

    /**
     * 最大高度.
     */
    val maxElevation = MAX_ELEVATION_MULTIPLE * defElevation

    private val cardFrontContainerView = FrameLayout(this.context).apply {
        this@BaseCard.addView(this)

        visibility = View.VISIBLE
    }

    private val cardBackContainerView = FrameLayout(this.context).apply {
        this@BaseCard.addView(this)

        visibility = View.GONE
    }

    protected var cardFrontView: View? = null
        protected set(value) {
            if (field === value) return
            field = value
            cardFrontContainerView.removeAllViews()
            if (field == null) return
            cardFrontContainerView.addView(field)
        }

    protected var cardBackView: View? = null
        protected set(value) {
            if (field === value) return
            field = value
            cardBackContainerView.removeAllViews()
            if (field == null) return
            cardBackContainerView.addView(field)
        }

    /**
     * 卡片出现动画.
     *
     * 开始状态: 高度为 [maxElevation].
     *
     * 动画过程: 从 +-90 度或 +-270 度减速翻转到 0 度, 然后高度加速减速降低.
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
                val valueFrom = (if (isClockwise) 1 else -1) * (if (hasBack) 270f else 90f)
                val valueTo = 0f
                setFloatValues(valueFrom, valueTo)
                duration = rotationDuration
                interpolator = DecelerateInterpolator()
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            play(rotationAnimator)
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
            val rotationAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "rotationY" else "rotationX"
                val valueFrom = 0f
                val valueTo = (if (isClockwise) -1 else 1) * (if (hasBack) 270f else 90f)
                setFloatValues(valueFrom, valueTo)
                duration = rotationDuration
                interpolator = AccelerateInterpolator()
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            play(rotationAnimator)
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
            onCardCut: (() -> View)? = null) {
        AnimatorSet().apply {
            val rotationOutAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "rotationY" else "rotationX"
                val valueFrom = 0f
                val valueTo = (if (isClockwise) -1 else 1) * (if (hasBack) 180f else 90f)
                setFloatValues(valueFrom, valueTo)
                duration = rotationDuration / 2L
                interpolator = AccelerateInterpolator()
                val animatorListener = object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                        if (onCardCut != null) cardFrontView = onCardCut()
                    }
                }
                addListener(animatorListener)
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            val rotationInAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "rotationY" else "rotationX"
                val valueFrom = (if (isClockwise) 1 else -1) * (if (hasBack) 180f else 90f)
                val valueTo = 0f
                setFloatValues(valueFrom, valueTo)
                duration = rotationDuration - rotationDuration / 2L
                interpolator = DecelerateInterpolator()
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            playSequentially(rotationOutAnimator, rotationInAnimator)
            val animatorListener = CardAnimatorListener()
            addListener(animatorListener)
            setTarget(this@BaseCard)
        }.start()
    }

    /**
     * 卡片正面或反面.
     */
    var isCardFront = true
        private set(value) {
            if (field == value) return
            field = value
            cardFrontContainerView.visibility = if (field) View.VISIBLE else View.GONE
            cardBackContainerView.visibility = if (field) View.GONE else View.VISIBLE
        }

    /**
     * 翻转动画更新监听器. 监听卡片正反面改变.
     *
     * @param isClockwise 翻转方向顺时针或逆时针.
     */
    protected inner class CardFrontBackAnimatorUpdateListener(private val isClockwise: Boolean) :
            ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator?) {
            val rotation = animation?.animatedValue as? Float? ?: return
            val validRotation = (rotation % 360f + 360f) % 360f
            isCardFront = when (validRotation) {
                0f, 90f, 180f, 270f -> isCardFront
                in 90f..270f -> false
                else -> true
            }
        }
    }

    /**
     * 动画开始时显示当前视图. 动画结束时递归移除全部动画监听器.
     * 动画开始时设置全部卡片和大卡片不可点击. 动画结束时设置全部卡片和大卡片可点击.
     */
    protected open inner class CardAnimatorListener(
            private val onStart: ((Animator) -> Unit)? = null,
            private val onEnd: ((Animator) -> Unit)? = null) : Animator.AnimatorListener {
        final override fun onAnimationStart(animation: Animator?) {
            animation ?: return

            visibility = View.VISIBLE

            elevation = maxElevation

//            outlineProvider = null

            // TODO
            card16Layout.cards { it.isClickable = false }
            card16Layout.bigCard.isClickable = false

            onStart(animation)
            onStart?.invoke(animation)
        }

        protected open fun onStart(animator: Animator) = Unit

        final override fun onAnimationEnd(animation: Animator?) {
            animation ?: return

            elevation = defElevation

//            outlineProvider = ViewOutlineProvider.BACKGROUND

            removeAllListeners(animation)

            // TODO
            card16Layout.cards { it.isClickable = true }
            card16Layout.bigCard.isClickable = true

            onEnd(animation)
            onEnd?.invoke(animation)
        }

        /**
         * 递归移除全部动画监听器.
         */
        private fun removeAllListeners(animator: Animator) {
            animator.removeAllListeners()
            if (animator is ValueAnimator) {
                animator.removeAllUpdateListeners()
            } else if (animator is AnimatorSet) {
                animator.childAnimations.forEach { removeAllListeners(it) }
            }
        }

        protected open fun onEnd(animator: Animator) = Unit

        final override fun onAnimationCancel(animation: Animator?) = Unit

        final override fun onAnimationRepeat(animation: Animator?) = Unit
    }

    companion object {
        /**
         * 最大高度倍数.
         */
        private const val MAX_ELEVATION_MULTIPLE = 2f
    }
}
