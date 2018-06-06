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
import com.ebnbin.eb.util.sp

/**
 * 大卡片.
 */
class BigCard(context: Context) : BaseCard(context) {
    init {
        visibility = View.GONE
        elevation = DEF_ELEVATION
        radius = DEF_RADIUS
    }

    private val button = Button(this.context).apply {
        visibility = View.GONE
        text = "BigCard"
        textSize = 16f.sp
        this@BigCard.addView(this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun animZoom(
            card: Card,
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

        // TODO: Card index 有效性.
        val cardCenterX = card16Layout.cardCenterXs[card.row][card.column]
        val offsetX = (cardCenterX - card16Layout.bigCardCenterX) / 2f
        val translationXFromValue = if (isZoomIn) offsetX else 0f
        val translationXToValue = if (isZoomIn) 0f else offsetX
        val translationXObjectAnimator = ObjectAnimator.ofFloat(this, "translationX", translationXFromValue,
                translationXToValue)

        val cardCenterY = card16Layout.cardCenterYs[card.row][card.column]
        val offsetY = (cardCenterY - card16Layout.bigCardCenterY) / 2f
        val translationYFromValue = if (isZoomIn) offsetY else 0f
        val translationYToValue = if (isZoomIn) 0f else offsetY
        val translationYObjectAnimator = ObjectAnimator.ofFloat(this, "translationY", translationYFromValue,
                translationYToValue)

        val translationZFromValue = if (isZoomIn) 1f else 2f
        val translationZToValue = if (isZoomIn) 2f else 1f
        val translationZObjectAnimator = ObjectAnimator.ofFloat(this, "translationZ", translationZFromValue,
                translationZToValue)

        val scaleFromValue = if (isZoomIn) (1f - card16Layout.scaleOut) / 2f + card16Layout.scaleOut else 1f
        val scaleToValue = if (isZoomIn) 1f else (1f - card16Layout.scaleOut) / 2f + card16Layout.scaleOut
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
                card16Layout.setAllCardsVisibility(View.VISIBLE, card.row, card.column)
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                if (!isZoomIn) return
                card16Layout.setAllCardsVisibility(View.GONE, card.row, card.column)
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

                card16Layout.setAllCardsClickable(false)

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

                card16Layout.setAllCardsClickable(true)

                visibility = if (isZoomIn) View.VISIBLE else View.GONE

                if (isZoomIn) {
                    setOnClickListener {
                        animZoom(
                                card = card,
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
                    card.animZoom(
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
