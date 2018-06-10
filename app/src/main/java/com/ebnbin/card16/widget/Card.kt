package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import com.ebnbin.eb.util.EBRuntimeException
import com.ebnbin.eb.util.RandomHelper
import com.ebnbin.eb.util.dp

/**
 * 卡片.
 *
 * @param row 初始化行.
 *
 * @param column 初始化列.
 */
@SuppressLint("ViewConstructor")
class Card(context: Context, row: Int, column: Int) : BaseCard(context, DEF_ELEVATION_DP.dp, DEF_RADIUS_DP.dp) {
    private val textView = TextView(this.context).apply {
        text = "$row-$column"
    }

    init {
        cardFrontView = textView

        setOnClickListener {
            if (row % 2 == column % 2) {
                animateZoomIn(
                        isHorizontal = false,
                        isClockwise = false,
                        hasCardBack = false,
                        duration = 300L,
                        startDelay = 0L,
                        onCut = null,
                        onStart = null,
                        onEnd = {
                            card16Layout.bigCard.setOnClickListener {
                                card16Layout.bigCard.setOnClickListener(null)
                                card16Layout.bigCard.animateZoomOut(
                                        row = row,
                                        column = column,
                                        isHorizontal = false,
                                        isClockwise = true,
                                        hasCardBack = false,
                                        duration = 300L,
                                        startDelay = 0L,
                                        onCut = null,
                                        onStart = null,
                                        onEnd = null)
                            }
                        })
            } else {
                animateCut(
                        isHorizontal = true,
                        isClockwise = false,
                        hasCardBack = false,
                        duration = 300L,
                        startDelay = 0L,
                        onCut = null,
                        onStart = null,
                        onEnd = null)
            }
        }

        // TODO
        animateInOut(
                isIn = true,
                isHorizontal = RandomHelper.nextBoolean(),
                isClockwise = RandomHelper.nextBoolean(),
                hasCardBack = RandomHelper.nextBoolean(),
                duration = RandomHelper.nextLong(450L, 1200L),
                startDelay = RandomHelper.nextLong(0L, 1000L),
                onStart = null,
                onEnd = null)
    }

    /**
     * 行.
     */
    var row = row
        private set
    /**
     * 列.
     */
    var column = column
        private set

    private fun setIndex(row: Int, column: Int) {
        this.row = row
        this.column = column

        textView.text = "$row-$column"
    }

    //*****************************************************************************************************************

    fun animateZoomIn(
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasCardBack: Boolean,
            duration: Long,
            startDelay: Long,
            onCut: (() -> View)?,
            onStart: ((Animator) -> Unit)?,
            onEnd: ((Animator) -> Unit)?) = internalAnimateZoomInOut(
            isBigCard = false,
            isIn = true,
            row = row,
            column = column,
            isHorizontal = isHorizontal,
            isClockwise = isClockwise,
            hasCardBack = hasCardBack,
            duration = duration,
            startDelay = startDelay,
            onCut = onCut,
            onStart = onStart,
            onEnd = onEnd)

    //*****************************************************************************************************************

    /**
     * 卡片左移动画.
     *
     * 开始状态: 位移为 0f.
     *
     * 动画过程: 高度加速减速升高, 然后加速减速左移, 然后高度加速减速降低.
     *
     * 结束状态: 列减 1, 重置位移.
     *
     * @param translateDuration 移动动画时长.
     */
    private fun animateMoveLeft(translateDuration: Long) {
        if (column <= 0) throw EBRuntimeException()
        AnimatorSet().apply {
            val translationAnimator = ObjectAnimator().apply {
                propertyName = "translationX"
                val valueFrom = 0f
                val valueTo = -(card16Layout.cardSize.toFloat() + card16Layout.spacing)
                setFloatValues(valueFrom, valueTo)
                duration = translateDuration
                interpolator = AccelerateDecelerateInterpolator()
            }
            play(translationAnimator)
            val animatorListener = CardAnimatorListener(onEnd = {
                setIndex(row, column - 1)
                invalidateLayout()
                translationX = 0f
            })
            addListener(animatorListener)
            setTarget(this@Card)
        }.start()
    }

    /**
     * 卡片右移动画.
     *
     * 开始状态: 位移为 0f.
     *
     * 动画过程: 高度加速减速升高, 然后加速减速右移, 然后高度加速减速降低.
     *
     * 结束状态: 列加 1, 重置位移.
     *
     * @param translateDuration 移动动画时长.
     */
    private fun animateMoveRight(translateDuration: Long) {
        if (column >= Card16Layout.GRID - 1) throw EBRuntimeException()
        AnimatorSet().apply {
            val translationAnimator = ObjectAnimator().apply {
                propertyName = "translationX"
                val valueFrom = 0f
                val valueTo = card16Layout.cardSize.toFloat() + card16Layout.spacing
                setFloatValues(valueFrom, valueTo)
                duration = translateDuration
                interpolator = AccelerateDecelerateInterpolator()
            }
            play(translationAnimator)
            val animatorListener = CardAnimatorListener(onEnd = {
                setIndex(row, column + 1)
                invalidateLayout()
                translationX = 0f
            })
            addListener(animatorListener)
            setTarget(this@Card)
        }.start()
    }

    /**
     * 卡片上移动画.
     *
     * 开始状态: 位移为 0f.
     *
     * 动画过程: 高度加速减速升高, 然后加速减速上移, 然后高度加速减速降低.
     *
     * 结束状态: 行减 1, 重置位移.
     *
     * @param translateDuration 移动动画时长.
     */
    private fun animateMoveTop(translateDuration: Long) {
        if (row <= 0) throw EBRuntimeException()
        AnimatorSet().apply {
            val translationAnimator = ObjectAnimator().apply {
                propertyName = "translationY"
                val valueFrom = 0f
                val valueTo = -(card16Layout.cardSize.toFloat() + card16Layout.spacing)
                setFloatValues(valueFrom, valueTo)
                duration = translateDuration
                interpolator = AccelerateDecelerateInterpolator()
            }
            play(translationAnimator)
            val animatorListener = CardAnimatorListener(onEnd = {
                setIndex(row - 1, column)
                invalidateLayout()
                translationY = 0f
            })
            addListener(animatorListener)
            setTarget(this@Card)
        }.start()
    }

    /**
     * 卡片下移动画.
     *
     * 开始状态: 位移为 0f.
     *
     * 动画过程: 高度加速减速升高, 然后加速减速下移, 然后高度加速减速降低.
     *
     * 结束状态: 行加 1, 重置位移.
     *
     * @param translateDuration 移动动画时长.
     */
    private fun animateMoveBottom(translateDuration: Long) {
        if (row >= Card16Layout.GRID - 1) throw EBRuntimeException()
        AnimatorSet().apply {
            val translationAnimator = ObjectAnimator().apply {
                propertyName = "translationY"
                val valueFrom = 0f
                val valueTo = card16Layout.cardSize.toFloat() + card16Layout.spacing
                setFloatValues(valueFrom, valueTo)
                duration = translateDuration
                interpolator = AccelerateDecelerateInterpolator()
            }
            play(translationAnimator)
            val animatorListener = CardAnimatorListener(onEnd = {
                setIndex(row + 1, column)
                invalidateLayout()
                translationY = 0f
            })
            addListener(animatorListener)
            setTarget(this@Card)
        }.start()
    }

    private fun invalidateLayout() {
        layout(card16Layout.cardLefts[row][column], card16Layout.cardTops[row][column],
                card16Layout.cardRights[row][column], card16Layout.cardBottoms[row][column])
    }

    companion object {
        /**
         * 默认高度 dp.
         */
        private const val DEF_ELEVATION_DP = 2f

        /**
         * 默认圆角 dp.
         */
        private const val DEF_RADIUS_DP = 2f
    }
}
