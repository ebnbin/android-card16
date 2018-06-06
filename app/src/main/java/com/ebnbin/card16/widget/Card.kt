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
class Card(context: Context) : BaseCard(context) {
    override val defElevation = DEF_ELEVATION_DP.dp
    override val maxElevation = MAX_ELEVATION_DP.dp

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
                animateCut(
                        isHorizontal = true,
                        isClockwise = false,
                        hasBack = false,
                        rotationDuration = 400L)
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
        val elevationFromValue = if (isZoomIn) DEF_ELEVATION_DP.dp else MAX_ELEVATION_DP.dp
        val elevationToValue = if (isZoomIn) MAX_ELEVATION_DP.dp else DEF_ELEVATION_DP.dp
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

        val translationXFromValue = if (isZoomIn) 0f else (card16Layout.getCenterX() - getCenterX()) / 2f
        val translationXToValue = if (isZoomIn) (card16Layout.getCenterX() - getCenterX()) / 2f else 0f
        val translationXObjectAnimator = ObjectAnimator.ofFloat(this, "translationX", translationXFromValue,
                translationXToValue)

        val translationYFromValue = if (isZoomIn) 0f else (card16Layout.getCenterY() - getCenterY()) / 2f
        val translationYToValue = if (isZoomIn) (card16Layout.getCenterY() - getCenterY()) / 2f else 0f
        val translationYObjectAnimator = ObjectAnimator.ofFloat(this, "translationY", translationYFromValue,
                translationYToValue)

        val translationZFromValue = if (isZoomIn) 0f else 1f
        val translationZToValue = if (isZoomIn) 1f else 0f
        val translationZObjectAnimator = ObjectAnimator.ofFloat(this, "translationZ", translationZFromValue,
                translationZToValue)

        val scaleFromValue = if (isZoomIn) 1f else (card16Layout.scaleIn - 1f) / 2f + 1f
        val scaleToValue = if (isZoomIn) (card16Layout.scaleIn - 1f) / 2f + 1f else 1f
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

                card16Layout.setAllCardsClickable(true)

                visibility = if (isZoomIn) View.GONE else View.VISIBLE

                if (isZoomIn) {
                    card16Layout.getBigCard().animZoom(
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

    companion object {
        /**
         * 默认高度 dp.
         */
        private const val DEF_ELEVATION_DP = 2f
        /**
         * 最大高度 dp.
         */
        private const val MAX_ELEVATION_DP = 8f
    }
}
