package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.ebnbin.card16.card.Card
import com.ebnbin.card16.util.centerX
import com.ebnbin.card16.util.centerY

/**
 * 基础卡片.
 *
 * @param defElevation 默认高度.
 *
 * @param defRadius 默认圆角.
 */
abstract class BaseCardView(context: Context, val defElevation: Float, val defRadius: Float) :
        android.support.v7.widget.CardView(context) {
    /**
     * 动画时的高度.
     */
    val animateElevation = ANIMATE_ELEVATION_MULTIPLE * defElevation

    init {
        visibility = View.GONE
        elevation = defElevation
        radius = defRadius
    }

    protected val card16Layout get() = parent as Card16Layout

    /**
     * 卡片大小. 与宽, 高相同.
     */
    protected val size get() = width

    //*****************************************************************************************************************
    // 卡片正反面.

    /**
     * 卡片正面根视图.
     */
    private val cardFrontRootView = FrameLayout(this.context).apply {
        this@BaseCardView.addView(this)
        visibility = View.VISIBLE
    }

    /**
     * 卡片反面根视图.
     */
    private val cardBackRootView = FrameLayout(this.context).apply {
        this@BaseCardView.addView(this)
        visibility = View.GONE
    }

    /**
     * 卡片正面或反面.
     */
    private var isCardFront = true
        set(value) {
            if (field == value) return
            field = value
            cardFrontRootView.visibility = if (field) View.VISIBLE else View.GONE
            cardBackRootView.visibility = if (field) View.GONE else View.VISIBLE
        }

    protected var card: Card? = null
        set(value) {
            if (field === value) return
            field = value
            cardFrontRootView.removeAllViews()
            cardBackRootView.removeAllViews()
            value ?: return
            val cardFrontView = getCardFrontView(value)
            if (cardFrontView != null) cardFrontRootView.addView(cardFrontView)
            val cardBackView = getCardBackView(value)
            if (cardBackView != null) cardBackRootView.addView(cardBackView)
        }

    protected abstract fun getCardFrontView(card: Card): View?
    protected abstract fun getCardBackView(card: Card): View?

    /**
     * 翻转动画更新监听器. 监听卡片正反面改变.
     */
    protected inner class CardFrontBackAnimatorUpdateListener : ValueAnimator.AnimatorUpdateListener {
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

    //*****************************************************************************************************************

    /**
     * 根据参数创建翻转动画.
     *
     * @param isIn 出现或消失.
     *
     * @param isHorizontal 水平方向或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param hasCardBack 翻转动画是否显示卡片反面.
     *
     * @param shouldHide 当翻转动画显示卡片反面时, 是否从卡片不可见的角度开始或到卡片不可见的角度结束.
     */
    protected fun rotationAnimator(
            isIn: Boolean,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasCardBack: Boolean,
            shouldHide: Boolean) = ObjectAnimator().apply {
        propertyName = if (isHorizontal) "rotationY" else "rotationX"
        val degree = if (hasCardBack) if (shouldHide) 270f else 180f else 90f
        val valueFrom = if (isIn) (if (isClockwise) 1 else -1) * degree else 0f
        val valueTo = if (isIn) 0f else (if (isClockwise) -1 else 1) * degree
        setFloatValues(valueFrom, valueTo)
        interpolator = if (isIn) DecelerateInterpolator() else AccelerateInterpolator()
        addUpdateListener(CardFrontBackAnimatorUpdateListener())
    }

    //*****************************************************************************************************************

    /**
     * 卡片出现或消失动画.
     *
     * @param isIn 出现或消失.
     *
     * @param isHorizontal 水平方向或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param hasCardBack 翻转动画是否显示卡片反面.
     *
     * @param duration 动画时长.
     *
     * @param startDelay 动画延时.
     *
     * @param onStart 动画开始回调.
     *
     * @param onEnd 动画结束回调.
     *
     * @return 当前动画.
     */
    protected fun animateInOut(
            isIn: Boolean,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasCardBack: Boolean,
            duration: Long,
            startDelay: Long,
            onStart: ((Animator) -> Unit)?,
            onEnd: ((Animator) -> Unit)?,
            card: Card?):
            Animator = AnimatorSet().apply {
        val rotationAnimator = rotationAnimator(isIn, isHorizontal, isClockwise, hasCardBack, true).apply {
            this.duration = duration
            addListener(CardAnimatorListener(
                    onStart = {
                        if (isIn) this@BaseCardView.card = card
                        onStart?.invoke(it)
                    },
                    onEnd = onEnd))
        }
        play(rotationAnimator)
        this.startDelay = startDelay
        setTarget(this@BaseCardView)
        start()
    }

    /**
     * 卡片切换动画.
     *
     * @param isHorizontal 水平方向或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param hasCardBack 翻转动画是否显示卡片反面.
     *
     * @param duration 动画时长.
     *
     * @param startDelay 动画延时.
     *
     * @param onCut 卡片切换回调. 返回新的卡片正面视图.
     *
     * @param onStart 动画开始回调.
     *
     * @param onEnd 动画结束回调.
     *
     * @return 当前动画.
     */
    protected fun animateCut(
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasCardBack: Boolean,
            duration: Long,
            startDelay: Long,
            onCut: (() -> Unit)?,
            onStart: ((Animator) -> Unit)?,
            onEnd: ((Animator) -> Unit)?,
            card: Card?):
            Animator = AnimatorSet().apply {
        val animatorSet = AnimatorSet().apply {
            val rotationOutAnimator = rotationAnimator(false, isHorizontal, isClockwise, hasCardBack,
                    false).apply {
                this.duration = duration / 2L
            }
            val rotationInAnimator = rotationAnimator(true, isHorizontal, isClockwise, hasCardBack, false).apply {
                this.duration = duration - duration / 2L
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)

                        this@BaseCardView.card = card
                        onCut?.invoke()
                    }
                })
            }
            playSequentially(rotationOutAnimator, rotationInAnimator)
            addListener(CardAnimatorListener(onStart, onEnd))
        }
        play(animatorSet)
        this.startDelay = startDelay
        setTarget(this@BaseCardView)
        start()
    }

    /**
     * 小卡片放大或大卡片缩小动画.
     *
     * @param isBigCard 大卡片或小卡片.
     *
     * @param isIn 放大或缩小.
     *
     * @param row 行.
     *
     * @param column 列.
     *
     * @param isHorizontal 水平方向或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param hasCardBack 翻转动画是否显示卡片反面.
     *
     * @param duration 动画时长.
     *
     * @param startDelay 动画延时.
     *
     * @param onCut 卡片切换回调. 返回新的卡片正面视图.
     *
     * @param onStart 动画开始回调.
     *
     * @param onEnd 动画结束回调.
     */
    protected fun internalAnimateZoomInOut(
            isBigCard: Boolean,
            isIn: Boolean,
            row: Int,
            column: Int,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasCardBack: Boolean,
            duration: Long,
            startDelay: Long?,
            onCut: (() -> Unit)?,
            onStart: ((Animator) -> Unit)?,
            onEnd: ((Animator) -> Unit)?):
            Animator = AnimatorSet().apply {
        // 大卡片的 in 是出现 out 是消失, 小卡片的 in 是消失 out 是出现.
        val isRealIn = isBigCard == isIn

        val cardView = card16Layout.cardViews[row][column]
        val bigCardView = card16Layout.bigCardView
        val animatorSet = AnimatorSet().apply {
            val rotationAnimator = rotationAnimator(isRealIn, isHorizontal, isClockwise, hasCardBack, false)
            val translationXAnimator = ObjectAnimator().apply {
                propertyName = "translationX"
                val cardCenter = cardView.centerX
                val bigCardCenter = bigCardView.centerX
                val translation = if (isBigCard) cardCenter - bigCardCenter else bigCardCenter - cardCenter
                val halfTranslation = translation / 2f
                val valueFrom = if (isRealIn) halfTranslation else 0f
                val valueTo = if (isRealIn) 0f else halfTranslation
                setFloatValues(valueFrom, valueTo)
            }
            val translationYAnimator = ObjectAnimator().apply {
                propertyName = "translationY"
                val cardCenter = cardView.centerY
                val bigCardCenter = bigCardView.centerY
                val translation = if (isBigCard) cardCenter - bigCardCenter else bigCardCenter - cardCenter
                val halfTranslation = translation / 2f
                val valueFrom = if (isRealIn) halfTranslation else 0f
                val valueTo = if (isRealIn) 0f else halfTranslation
                setFloatValues(valueFrom, valueTo)
            }
            val scale = if (isBigCard)
                cardView.size.toFloat() / bigCardView.size
            else
                bigCardView.size.toFloat() / cardView.size
            val halfScale = (1f + scale) / 2f
            val scaleValueFrom = if (isRealIn) halfScale else 1f
            val scaleValueTo = if (isRealIn) 1f else halfScale
            val scaleXAnimator = ObjectAnimator().apply {
                propertyName = "scaleX"
                setFloatValues(scaleValueFrom, scaleValueTo)
            }
            val scaleYAnimator = ObjectAnimator().apply {
                propertyName = "scaleY"
                setFloatValues(scaleValueFrom, scaleValueTo)
            }
            val radiusAnimator = ObjectAnimator().apply {
                propertyName = "radius"
                val cardRadius = cardView.defRadius
                val bigCardRadius = bigCardView.defRadius
                val radius = if (isBigCard) bigCardRadius else cardRadius
                val halfRadius = (cardRadius + bigCardRadius) / 2f
                val valueFrom = if (isRealIn) halfRadius else radius
                val valueTo = if (isRealIn) radius else halfRadius
                setFloatValues(valueFrom, valueTo)
            }
            val elevationAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val cardElevation = cardView.animateElevation
                val bigCardElevation = bigCardView.animateElevation
                val elevation = if (isBigCard) bigCardElevation else cardElevation
                val halfElevation = (cardElevation + bigCardElevation) / 2f
                val valueFrom = if (isRealIn) halfElevation else elevation
                val valueTo = if (isRealIn) elevation else halfElevation
                setFloatValues(valueFrom, valueTo)
            }
            playTogether(rotationAnimator, translationXAnimator, translationYAnimator, scaleXAnimator, scaleYAnimator,
                    radiusAnimator, elevationAnimator)
            interpolator = if (isRealIn) DecelerateInterpolator() else AccelerateInterpolator()
            this.duration = if (isRealIn) duration - duration / 2L else duration / 2L
            addListener(CardAnimatorListener(
                    onStart = {
                        if (isBigCard) {
                            if (isIn) {
                                bigCardView.card = cardView.card
                                onCut?.invoke()
                            } else {
                                card16Layout.cardViews(row, column) { it.visibility = View.VISIBLE }
                                onStart?.invoke(it)
                            }
                        } else {
                            if (isIn) {
                                onStart?.invoke(it)
                            } else {
                                cardView.card = bigCardView.card
                                bigCardView.card = null
                                onCut?.invoke()
                            }
                        }
                    },
                    onEnd = {
                        if (isBigCard) {
                            if (isIn) {
                                card16Layout.cardViews(row, column) { it.visibility = View.GONE }
                                onEnd?.invoke(it)
                            } else {
                                bigCardView.visibility = View.GONE
                                cardView.internalAnimateZoomInOut(
                                        isBigCard = false,
                                        isIn = false,
                                        row = row,
                                        column = column,
                                        isHorizontal = isHorizontal,
                                        isClockwise = isClockwise,
                                        hasCardBack = hasCardBack,
                                        duration = duration,
                                        startDelay = null,
                                        onCut = onCut,
                                        onStart = null,
                                        onEnd = onEnd)
                            }
                        } else {
                            if (isIn) {
                                cardView.visibility = View.GONE
                                bigCardView.internalAnimateZoomInOut(
                                        isBigCard = true,
                                        isIn = true,
                                        row = row,
                                        column = column,
                                        isHorizontal = isHorizontal,
                                        isClockwise = isClockwise,
                                        hasCardBack = hasCardBack,
                                        duration = duration,
                                        startDelay = null,
                                        onCut = onCut,
                                        onStart = null,
                                        onEnd = onEnd)
                            } else {
                                onEnd?.invoke(it)
                            }
                        }
                    }))
        }
        play(animatorSet)
        this.startDelay = if (isRealIn) 0L else startDelay ?: 0L
        setTarget(this@BaseCardView)
        start()
    }

    //*****************************************************************************************************************

    /**
     * 卡片动画监听器.
     */
    protected inner class CardAnimatorListener(
            private val onStart: ((Animator) -> Unit)? = null,
            private val onEnd: ((Animator) -> Unit)? = null) : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
            animation ?: return

            elevation = animateElevation

            // TODO
            card16Layout.cardViews { it.isClickable = false }
            card16Layout.bigCardView.isClickable = false

            visibility = View.VISIBLE

            onStart?.invoke(animation)
        }

        override fun onAnimationEnd(animation: Animator?) {
            animation ?: return

            removeAllListeners(animation)

            elevation = defElevation
            radius = defRadius
            translationX = 0f
            translationY = 0f
            scaleX = 1f
            scaleY = 1f

            // TODO
            card16Layout.cardViews { it.isClickable = true }
            card16Layout.bigCardView.isClickable = true

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

        override fun onAnimationCancel(animation: Animator?) = Unit

        override fun onAnimationRepeat(animation: Animator?) = Unit
    }

    companion object {
        /**
         * 动画时的高度 (默认高度倍数).
         */
        private const val ANIMATE_ELEVATION_MULTIPLE = 2f
    }
}
