package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import com.ebnbin.card16.widget.BaseCard.Companion.ELEVATION_DURATION
import com.ebnbin.eb.util.dp

/**
 * 卡片.
 */
class Card(context: Context) : BaseCard(context) {
    override val defElevation = DEF_ELEVATION_DP.dp
    override val maxElevation = MAX_ELEVATION_DP.dp

    override val defRadius = DEF_RADIUS_DP.dp

    private val button = Button(this.context).apply {
        this@Card.addView(this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    init {
        setOnClickListener {
            if (row % 2 == column % 2) {
                animateZoomIn(
                        isHorizontal = false,
                        isClockwise = false,
                        hasBack = false,
                        rotationDuration = 400L)
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

    /**
     * 卡片放大动画.
     *
     * 需要调用 [BigCard.animateZoomIn].
     *
     * [Card] 负责前半部分的动画, 衔接 [BigCard] 负责后半部分的动画, 完成从小卡片到大卡片的放大动画.
     *
     * [Card] 完成前半部分动画后隐藏自己. [BigCard] 高度降低前隐藏其他 [Card].
     *
     * 动画时长: 2 * [ELEVATION_DURATION] + [rotationDuration].
     *
     * @param isHorizontal 水平方向或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param hasBack 卡片翻转时是否有背面 (空白面).
     *
     * @param rotationDuration 翻转动画时长 (两部分动画平分).
     *
     * @param onCardCut 卡片切换回调.
     */
    fun animateZoomIn(
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
            val rotationAnimatorSet = AnimatorSet().apply {
                val rotationAnimator = ObjectAnimator().apply {
                    propertyName = if (isHorizontal) "rotationY" else "rotationX"
                    val valueFrom = 0f
                    val valueTo = (if (isClockwise) -1 else 1) * (if (hasBack) 180f else 90f)
                    setFloatValues(valueFrom, valueTo)
                    val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                    addUpdateListener(animatorUpdateListener)
                }
                val translationXAnimator = ObjectAnimator().apply {
                    propertyName = "translationX"
                    val valueFrom = 0f
                    val valueTo = (card16Layout.bigCardCenterX - card16Layout.cardCenterXs[row][column]) / 2f
                    setFloatValues(valueFrom, valueTo)
                }
                val translationYAnimator = ObjectAnimator().apply {
                    propertyName = "translationY"
                    val valueFrom = 0f
                    val valueTo = (card16Layout.bigCardCenterY - card16Layout.cardCenterYs[row][column]) / 2f
                    setFloatValues(valueFrom, valueTo)
                }
                val scaleXAnimator = ObjectAnimator().apply {
                    propertyName = "scaleX"
                    val valueFrom = 1f
                    val valueTo = (1f + card16Layout.scaleIn) / 2f
                    setFloatValues(valueFrom, valueTo)
                }
                val scaleYAnimator = ObjectAnimator().apply {
                    propertyName = "scaleY"
                    val valueFrom = 1f
                    val valueTo = (1f + card16Layout.scaleIn) / 2f
                    setFloatValues(valueFrom, valueTo)
                }
                val radiusAnimator = ObjectAnimator().apply {
                    propertyName = "radius"
                    val valueFrom = defRadius
                    val valueTo = (defRadius + card16Layout.getBigCard().defRadius) / 2f
                    setFloatValues(valueFrom, valueTo)
                }
                val elevationAnimator = ObjectAnimator().apply {
                    propertyName = "elevation"
                    val valueFrom = maxElevation
                    val valueTo = (maxElevation + card16Layout.getBigCard().maxElevation) / 2f
                    setFloatValues(valueFrom, valueTo)
                }
                playTogether(rotationAnimator, translationXAnimator, translationYAnimator, scaleXAnimator,
                        scaleYAnimator, radiusAnimator, elevationAnimator)
                duration = rotationDuration / 2L
                interpolator = AccelerateInterpolator()
                val animatorListener = object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                        onCardCut?.invoke()
                    }
                }
                addListener(animatorListener)
            }
            playSequentially(elevationInAnimator, rotationAnimatorSet)
            val animatorListener = CardAnimatorListener(onEnd = {
                visibility = View.GONE

                card16Layout.getBigCard().animateZoomIn(row, column, isHorizontal, isClockwise, hasBack,
                        rotationDuration)
            })
            addListener(animatorListener)
            setTarget(this@Card)
        }.start()
    }

    /**
     * 只能由 [BigCard.animateZoomOut] 调用.
     */
    fun animateZoomOut(
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasBack: Boolean,
            rotationDuration: Long) {
        AnimatorSet().apply {
            val rotationAnimatorSet = AnimatorSet().apply {
                val rotationAnimator = ObjectAnimator().apply {
                    propertyName = if (isHorizontal) "rotationY" else "rotationX"
                    val valueFrom = (if (isClockwise) 1 else -1) * (if (hasBack) 180f else 90f)
                    val valueTo = 0f
                    setFloatValues(valueFrom, valueTo)
                    val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                    addUpdateListener(animatorUpdateListener)
                }
                val translationXAnimator = ObjectAnimator().apply {
                    propertyName = "translationX"
                    val valueFrom = (card16Layout.bigCardCenterX - card16Layout.cardCenterXs[row][column]) / 2f
                    val valueTo = 0f
                    setFloatValues(valueFrom, valueTo)
                }
                val translationYAnimator = ObjectAnimator().apply {
                    propertyName = "translationY"
                    val valueFrom = (card16Layout.bigCardCenterY - card16Layout.cardCenterYs[row][column]) / 2f
                    val valueTo = 0f
                    setFloatValues(valueFrom, valueTo)
                }
                val scaleXAnimator = ObjectAnimator().apply {
                    propertyName = "scaleX"
                    val valueFrom = (card16Layout.scaleIn + 1f) / 2f
                    val valueTo = 1f
                    setFloatValues(valueFrom, valueTo)
                }
                val scaleYAnimator = ObjectAnimator().apply {
                    propertyName = "scaleY"
                    val valueFrom = (card16Layout.scaleIn + 1f) / 2f
                    val valueTo = 1f
                    setFloatValues(valueFrom, valueTo)
                }
                val radiusAnimator = ObjectAnimator().apply {
                    propertyName = "radius"
                    val valueFrom = (card16Layout.getBigCard().defRadius + defRadius) / 2f
                    val valueTo = defRadius
                    setFloatValues(valueFrom, valueTo)
                }
                val elevationAnimator = ObjectAnimator().apply {
                    propertyName = "elevation"
                    val valueFrom = (card16Layout.getBigCard().maxElevation + maxElevation) / 2f
                    val valueTo = maxElevation
                    setFloatValues(valueFrom, valueTo)
                }
                playTogether(rotationAnimator, translationXAnimator, translationYAnimator, scaleXAnimator,
                        scaleYAnimator, radiusAnimator, elevationAnimator)
                duration = rotationDuration - rotationDuration / 2L
                interpolator = DecelerateInterpolator()
            }
            val elevationOutAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val valueFrom = maxElevation
                val valueTo = defElevation
                setFloatValues(valueFrom, valueTo)
                duration = ELEVATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }
            playSequentially(rotationAnimatorSet, elevationOutAnimator)
            val animatorListener = CardAnimatorListener(onEnd = {
                // TODO
            })
            addListener(animatorListener)
            setTarget(this@Card)
        }.start()
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

        /**
         * 默认圆角 dp.
         */
        private const val DEF_RADIUS_DP = 2f
    }
}
