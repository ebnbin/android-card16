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
import com.ebnbin.eb.view.getCenterX
import com.ebnbin.eb.view.getCenterY

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
                    isHorizontal = false,
                    isClockwise = scaleX != 1f,
                    is180 = true,
                    rotationXYDuration = 400L,
                    isFromSmallToBig = scaleX == 1f,
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
            isFromSmallToBig: Boolean?,
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

        val rotationXYInFromClockwiseSign = if (isClockwise) 1 else -1
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

        var translationXFromValue: Float? = null
        var translationYFromValue: Float? = null
        var translationZFromValue: Float? = null
        var translationXObjectAnimator: ObjectAnimator? = null
        var translationYObjectAnimator: ObjectAnimator? = null
        var translationZObjectAnimator: ObjectAnimator? = null
        var scaleFromValue: Float? = null
        var scaleXObjectAnimator: ObjectAnimator? = null
        var scaleYObjectAnimator: ObjectAnimator? = null
        if (isFromSmallToBig != null) {
            val translationXToValue: Float
            val translationYToValue: Float
            val translationZToValue: Float
            val scaleToValue: Float
            if (isFromSmallToBig) {
                translationXFromValue = 0f
                translationYFromValue = 0f
                translationZFromValue = 0f
                translationXToValue = getCard16Layout().getCenterX() - getCenterX()
                translationYToValue = getCard16Layout().getCenterY() - getCenterY()
                translationZToValue = 1f
                scaleFromValue = 1f
                scaleToValue = getCard16Layout()?.cardScale ?: 1f
            } else {
                translationXFromValue = getCard16Layout().getCenterX() - getCenterX()
                translationYFromValue = getCard16Layout().getCenterY() - getCenterY()
                translationZFromValue = 1f
                translationXToValue = 0f
                translationYToValue = 0f
                translationZToValue = 0f
                scaleFromValue = getCard16Layout()?.cardScale ?: 1f
                scaleToValue = 1f
            }
            translationXObjectAnimator = ObjectAnimator.ofFloat(this, "translationX", translationXFromValue,
                    translationXToValue)
            translationXObjectAnimator?.duration = rotationXYDuration
            translationXObjectAnimator?.interpolator = AccelerateDecelerateInterpolator()
            translationYObjectAnimator = ObjectAnimator.ofFloat(this, "translationY", translationYFromValue,
                    translationYToValue)
            translationYObjectAnimator?.duration = rotationXYDuration
            translationYObjectAnimator?.interpolator = AccelerateDecelerateInterpolator()
            translationZObjectAnimator = ObjectAnimator.ofFloat(this, "translationZ", translationZFromValue,
                    translationZToValue)
            translationZObjectAnimator?.duration = rotationXYDuration
            translationZObjectAnimator?.interpolator = AccelerateDecelerateInterpolator()
            scaleXObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX", scaleFromValue, scaleToValue)
            scaleXObjectAnimator?.duration = rotationXYDuration
            scaleXObjectAnimator?.interpolator = AccelerateDecelerateInterpolator()
            scaleYObjectAnimator = ObjectAnimator.ofFloat(this, "scaleY", scaleFromValue, scaleToValue)
            scaleYObjectAnimator?.duration = rotationXYDuration
            scaleYObjectAnimator?.interpolator = AccelerateDecelerateInterpolator()
        }

        val rotationXYTranslationScaleAnimatorSet = AnimatorSet()
        var rotationXYTranslationScaleAnimatorListener: Animator.AnimatorListener? = null
        if (isFromSmallToBig == null) {
            rotationXYTranslationScaleAnimatorSet.playTogether(rotationXYAnimatorSet)
        } else {
            rotationXYTranslationScaleAnimatorSet.playTogether(rotationXYAnimatorSet, translationXObjectAnimator,
                    translationYObjectAnimator, translationZObjectAnimator, scaleXObjectAnimator, scaleYObjectAnimator)
            rotationXYTranslationScaleAnimatorListener = object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)

                    if (isFromSmallToBig) return
                    getCard16Layout()?.setOtherCardsVisibility(row, column, View.VISIBLE)
                }

                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)

                    if (!isFromSmallToBig) return
                    getCard16Layout()?.setOtherCardsVisibility(row, column, View.GONE)
                }
            }
        }

        val elevationOutObjectAnimator = ObjectAnimator.ofFloat(this, "elevation", MAX_ELEVATION, DEF_ELEVATION)
        elevationOutObjectAnimator.duration = elevationDuration
        elevationOutObjectAnimator.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(elevationInObjectAnimator, rotationXYTranslationScaleAnimatorSet,
                elevationOutObjectAnimator)
        animatorSet.startDelay = startDelay
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)

                rotationXYOutObjectAnimator.addListener(rotationXYOutAnimatorListener)
                rotationXYOutObjectAnimator.addUpdateListener(rotationXYAnimatorUpdateListener)
                rotationXYInObjectAnimator.addUpdateListener(rotationXYAnimatorUpdateListener)
                if (rotationXYTranslationScaleAnimatorListener != null) {
                    rotationXYTranslationScaleAnimatorSet.addListener(rotationXYTranslationScaleAnimatorListener)
                }

                getCard16Layout()?.setAllCardsClickable(false)

                visibility = View.VISIBLE

                elevation = elevationInFromValue
                if (isHorizontal) rotationY = rotationXYOutFromValue else rotationX = rotationXYOutFromValue
                if (translationXFromValue != null) {
                    translationX = translationXFromValue
                }
                if (translationYFromValue != null) {
                    translationY = translationYFromValue
                }
                if (translationZFromValue != null) {
                    translationZ = translationZFromValue
                }
                if (scaleFromValue != null) {
                    scaleX = scaleFromValue
                    scaleY = scaleFromValue
                }

                onStart?.invoke()
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                animatorSet.removeListener(this)

                rotationXYOutObjectAnimator.removeListener(rotationXYOutAnimatorListener)
                rotationXYOutObjectAnimator.removeUpdateListener(rotationXYAnimatorUpdateListener)
                rotationXYInObjectAnimator.removeUpdateListener(rotationXYAnimatorUpdateListener)
                if (rotationXYTranslationScaleAnimatorListener != null) {
                    rotationXYTranslationScaleAnimatorSet.removeListener(rotationXYTranslationScaleAnimatorListener)
                }

                getCard16Layout()?.setAllCardsClickable(true)

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
