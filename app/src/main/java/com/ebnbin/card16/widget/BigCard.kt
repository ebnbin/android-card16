package com.ebnbin.card16.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.ebnbin.eb.util.dp
import com.ebnbin.eb.util.sp

/**
 * 大卡片.
 */
class BigCard(context: Context) : BaseCard(context, DEF_ELEVATION_DP.dp, DEF_RADIUS_DP.dp) {
    init {
        visibility = View.GONE
        elevation = DEF_ELEVATION_DP.dp
        radius = DEF_RADIUS_DP.dp

        cardFrontView = TextView(this.context).apply {
            text = "BigCard"
            textSize = 24f.sp
        }
    }

    /**
     * 只能由 [Card.animateZoomIn] 调用.
     */
    fun animateZoomIn(
            row: Int,
            column: Int,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasBack: Boolean,
            rotationDuration: Long) {
        AnimatorSet().apply {
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
                val valueFrom = (card16Layout.cardCenterXs[row][column] - card16Layout.bigCardCenterX) / 2f
                val valueTo = 0f
                setFloatValues(valueFrom, valueTo)
            }
            val translationYAnimator = ObjectAnimator().apply {
                propertyName = "translationY"
                val valueFrom = (card16Layout.cardCenterYs[row][column] - card16Layout.bigCardCenterY) / 2f
                val valueTo = 0f
                setFloatValues(valueFrom, valueTo)
            }
            val scaleXAnimator = ObjectAnimator().apply {
                propertyName = "scaleX"
                val valueFrom = (card16Layout.scaleOut + 1f) / 2f
                val valueTo = 1f
                setFloatValues(valueFrom, valueTo)
            }
            val scaleYAnimator = ObjectAnimator().apply {
                propertyName = "scaleY"
                val valueFrom = (card16Layout.scaleOut + 1f) / 2f
                val valueTo = 1f
                setFloatValues(valueFrom, valueTo)
            }
            val radiusAnimator = ObjectAnimator().apply {
                propertyName = "radius"
                val valueFrom = (card16Layout.cards[row][column].defRadius + defRadius) / 2f
                val valueTo = defRadius
                setFloatValues(valueFrom, valueTo)
            }
            val elevationAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val valueFrom = (card16Layout.cards[row][column].maxElevation + maxElevation) / 2f
                val valueTo = maxElevation
                setFloatValues(valueFrom, valueTo)
            }
            playTogether(rotationAnimator, translationXAnimator, translationYAnimator, scaleXAnimator,
                    scaleYAnimator, radiusAnimator, elevationAnimator)
            duration = rotationDuration - rotationDuration / 2L
            interpolator = DecelerateInterpolator()
            val animatorListener = CardAnimatorListener(onEnd = {
                card16Layout.cards(row, column) { it.visibility = View.GONE }

                // TODO
                setOnClickListener {
                    setOnClickListener(null)
                    animateZoomOut(row, column, isHorizontal, !isClockwise, hasBack, rotationDuration)
                }
            })
            addListener(animatorListener)
            setTarget(this@BigCard)
        }.start()
    }

    /**
     * 卡片缩小动画.
     *
     * 需要调用 [Card.animateZoomOut].
     *
     * [BigCard] 负责前半部分的动画, 衔接 [Card] 负责后半部分的动画, 完成从大卡片到小卡片的缩小动画.
     *
     * [BigCard] 完成前半部分动画后隐藏自己. [BigCard] 高度升高后显示其他 [Card].
     *
     * @param row [Card] 行.
     *
     * @param column [Card] 列.
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
    fun animateZoomOut(
            row: Int,
            column: Int,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasBack: Boolean,
            rotationDuration: Long,
            onCardCut: (() -> View)? = null) {
        AnimatorSet().apply {
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
                val valueTo = (card16Layout.cardCenterXs[row][column] - card16Layout.bigCardCenterX) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val translationYAnimator = ObjectAnimator().apply {
                propertyName = "translationY"
                val valueFrom = 0f
                val valueTo = (card16Layout.cardCenterYs[row][column] - card16Layout.bigCardCenterY) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val scaleXAnimator = ObjectAnimator().apply {
                propertyName = "scaleX"
                val valueFrom = 1f
                val valueTo = (1f + card16Layout.scaleOut) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val scaleYAnimator = ObjectAnimator().apply {
                propertyName = "scaleY"
                val valueFrom = 1f
                val valueTo = (1f + card16Layout.scaleOut) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val radiusAnimator = ObjectAnimator().apply {
                propertyName = "radius"
                val valueFrom = defRadius
                val valueTo = (defRadius + card16Layout.cards[row][column].defRadius) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val elevationAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val valueFrom = maxElevation
                val valueTo = (maxElevation + card16Layout.cards[row][column].maxElevation) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            playTogether(rotationAnimator, translationXAnimator, translationYAnimator, scaleXAnimator,
                    scaleYAnimator, radiusAnimator, elevationAnimator)
            duration = rotationDuration / 2L
            interpolator = AccelerateInterpolator()
            val animatorListener = CardAnimatorListener(onStart = {
                card16Layout.cards(row, column) { it.visibility = View.VISIBLE }
            }, onEnd = {
                visibility = View.GONE

                card16Layout.cards[row][column].animateZoomOut(isHorizontal, isClockwise, hasBack, rotationDuration)

                if (onCardCut != null) cardFrontView = onCardCut()
            })
            addListener(animatorListener)
            setTarget(this@BigCard)
        }.start()
    }

    companion object {
        /**
         * 默认高度 dp.
         */
        private const val DEF_ELEVATION_DP = 8f

        /**
         * 默认圆角 dp.
         */
        private const val DEF_RADIUS_DP = 8f
    }
}
