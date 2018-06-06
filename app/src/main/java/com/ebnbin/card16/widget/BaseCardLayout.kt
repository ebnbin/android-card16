package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View

/**
 * 基础卡片布局.
 */
abstract class BaseCardLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    /**
     * 卡片正面或反面.
     */
    var isCardFront = true
        private set(value) {
            if (field == value) return
            field = value
            onCardFrontBack(field)
        }

    /**
     * 卡片正反面改变回调.
     */
    protected open fun onCardFrontBack(isFront: Boolean) {
        // TODO
        for (index in 0 until childCount) {
            getChildAt(index).visibility = if (isFront) View.VISIBLE else View.GONE
        }
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
                90f -> isClockwise
                270f -> !isClockwise
                in 90f..270f -> false
                else -> true
            }
        }
    }

    /**
     * 动画开始时显示当前视图. 动画结束时递归移除全部动画监听器.
     */
    protected open inner class CardLayoutAnimatorListener(
            private val onStart: ((Animator) -> Unit)? = null,
            private val onEnd: ((Animator) -> Unit)? = null) : Animator.AnimatorListener {
        final override fun onAnimationStart(animation: Animator?) {
            animation ?: return

            visibility = View.VISIBLE

            onStart(animation)
            onStart?.invoke(animation)
        }

        protected open fun onStart(animator: Animator) = Unit

        final override fun onAnimationEnd(animation: Animator?) {
            animation ?: return

            removeAllListeners(animation)

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
}
