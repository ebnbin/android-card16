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
import com.ebnbin.eb.util.dpInt
import com.ebnbin.eb.util.sp
import com.ebnbin.eb.view.getCenterX
import com.ebnbin.eb.view.getCenterY
import kotlin.math.min

/**
 * 大卡片布局.
 */
class BigCardLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        CardView(context, attrs, defStyleAttr) {
    init {
        visibility = View.GONE
        elevation = DEF_ELEVATION
        radius = DEF_RADIUS
    }

    private val button = Button(this.context).apply {
        visibility = View.GONE
        text = "BigCardLayout"
        textSize = 32f.sp
        this@BigCardLayout.addView(this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun animZoom(
            cardLayout: CardLayout,
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
        val elevationFromValue = if (isZoomIn) MAX_ELEVATION else DEF_ELEVATION
        val elevationToValue = if (isZoomIn) DEF_ELEVATION else MAX_ELEVATION
        val elevationObjectAnimator = ObjectAnimator.ofFloat(this, "elevation", elevationFromValue, elevationToValue)
        elevationObjectAnimator.duration = elevationDuration
        elevationObjectAnimator.interpolator = AccelerateDecelerateInterpolator()

        val rotationXYPropertyName = if (isHorizontal) "rotationY" else "rotationX"
        val rotationXYDegrees = if (is180) 90f else 180f
        val rotationXYFromClockwiseSign = if (isClockwise) 1 else -1
        val rotationXYFromValue = if (isZoomIn) rotationXYFromClockwiseSign * rotationXYDegrees else 0f
        val rotationXYToClockwiseSign = if (isClockwise) -1 else 1
        val rotationXYToValue = if (isZoomIn) 0f else rotationXYToClockwiseSign * rotationXYDegrees
        val rotationXYObjectAnimator = ObjectAnimator.ofFloat(this, rotationXYPropertyName, rotationXYFromValue,
                rotationXYToValue)
        rotationXYObjectAnimator.duration = rotationXYDuration
        rotationXYObjectAnimator.interpolator = if (isZoomIn) DecelerateInterpolator() else AccelerateInterpolator()
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

        val width = getCard16Layout()?.width ?: 0
        val height = getCard16Layout()?.height ?: 0
        val minSize = min(width, height)
        val spacing = Card16Layout.SPACING_DP.dpInt
        val childSize = (minSize - (Card16Layout.GRID + 1) * spacing) / Card16Layout.GRID
        val leftSpacing = (width - (Card16Layout.GRID - 1) * spacing - Card16Layout.GRID * childSize) / 2
        val topSpacing = (height - (Card16Layout.GRID - 1) * spacing - Card16Layout.GRID * childSize) / 2
        val childL = leftSpacing + (childSize + spacing) * cardLayout.column
        val childT = topSpacing + (childSize + spacing) * cardLayout.row
        val childR = childL + childSize
        val childB = childT + childSize
        val childCenterX = (childL + childR) / 2f
        val childCenterY = (childT + childB) / 2f

        val translationXFromValue = if (isZoomIn) (childCenterX - getCard16Layout().getCenterX()) / 2f else 0f
        val translationXToValue = if (isZoomIn) 0f else (childCenterX - getCard16Layout().getCenterX()) / 2f
        val translationXObjectAnimator = ObjectAnimator.ofFloat(this, "translationX", translationXFromValue,
                translationXToValue)

        val translationYFromValue = if (isZoomIn) (childCenterY - getCard16Layout().getCenterY()) / 2f else 0f
        val translationYToValue = if (isZoomIn) 0f else (childCenterY - getCard16Layout().getCenterY()) / 2f
        val translationYObjectAnimator = ObjectAnimator.ofFloat(this, "translationY", translationYFromValue,
                translationYToValue)

        val translationZFromValue = if (isZoomIn) 1f else 2f
        val translationZToValue = if (isZoomIn) 2f else 1f
        val translationZObjectAnimator = ObjectAnimator.ofFloat(this, "translationZ", translationZFromValue,
                translationZToValue)

        val cardScaleInverse = getCard16Layout()?.cardScaleInverse ?: 1f
        val scaleFromValue = if (isZoomIn) (1f - cardScaleInverse) / 2f + cardScaleInverse else 1f
        val scaleToValue = if (isZoomIn) 1f else (1f - cardScaleInverse) / 2f + cardScaleInverse
        val scaleXObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX", scaleFromValue, scaleToValue)

        val scaleYObjectAnimator = ObjectAnimator.ofFloat(this, "scaleY", scaleFromValue, scaleToValue)

        // TODO: elevation.

        val rotationXYTranslationScaleAnimatorSet = AnimatorSet()
        rotationXYTranslationScaleAnimatorSet.playTogether(rotationXYObjectAnimator, translationXObjectAnimator,
                translationYObjectAnimator, translationZObjectAnimator, scaleXObjectAnimator, scaleYObjectAnimator)
        rotationXYTranslationScaleAnimatorSet.duration = rotationXYDuration
        rotationXYTranslationScaleAnimatorSet.interpolator =
                if (isZoomIn) DecelerateInterpolator() else AccelerateInterpolator()
        val rotationXYTranslationScaleAnimatorListener = object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)

                if (isZoomIn) return
                getCard16Layout()?.setOtherCardsVisibility(cardLayout.row, cardLayout.column, View.VISIBLE)
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                if (!isZoomIn) return
                getCard16Layout()?.setOtherCardsVisibility(cardLayout.row, cardLayout.column, View.GONE)
            }
        }

        val animatorSet = AnimatorSet()
        if (isZoomIn)
            animatorSet.playSequentially(rotationXYTranslationScaleAnimatorSet, elevationObjectAnimator)
        else
            animatorSet.playSequentially(elevationObjectAnimator, rotationXYTranslationScaleAnimatorSet)
        animatorSet.startDelay = startDelay
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)

                rotationXYObjectAnimator.addUpdateListener(rotationXYAnimatorUpdateListener)
                rotationXYTranslationScaleAnimatorSet.addListener(rotationXYTranslationScaleAnimatorListener)

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
                rotationXYTranslationScaleAnimatorSet.removeListener(rotationXYTranslationScaleAnimatorListener)

                getCard16Layout()?.setAllCardsClickable(true)

                visibility = if (isZoomIn) View.VISIBLE else View.GONE

                if (isZoomIn) {
                    setOnClickListener {
                        animZoom(
                                cardLayout = cardLayout,
                                isZoomIn = !isZoomIn,
                                elevationDuration = elevationDuration,
                                isHorizontal = isHorizontal,
                                isClockwise = !isClockwise,
                                is180 = is180,
                                rotationXYDuration = rotationXYDuration,
                                startDelay = startDelay)
                    }
                } else {
                    setOnClickListener(null)
                    cardLayout.animZoom(
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
        private val DEF_ELEVATION = 8f.dp
        /**
         * 最大高度.
         */
        private val MAX_ELEVATION = 32f.dp

        /**
         * 默认圆角.
         */
        private val DEF_RADIUS = 8f.dp
    }
}
