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
        if (this.row == row && this.column == column) return
        this.row = row
        this.column = column
        // TODO
        card16Layout.cards[this.row][this.column] = this
        card16Layout.layoutCard(this)

        textView.text = "$row-$column"
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

    /**
     * 卡片移动动画.
     *
     * @param isHorizontal 水平方向或垂直方向移动.
     *
     * @param grid 移动行列数. 不能为 0, 移动后卡片必须在行列范围内.
     *
     * @param duration 动画时长.
     *
     * @param startDelay 动画延时.
     *
     * @param onStart 动画开始回调.
     *
     * @param onEnd 动画结束回调.
     */
    private fun animateMove(
            isHorizontal: Boolean,
            grid: Int,
            duration: Long,
            startDelay: Long,
            onStart: ((Animator) -> Unit)?,
            onEnd: ((Animator) -> Unit)?): Animator {
        if (grid == 0) throw EBRuntimeException()
        val newRow = row + if (isHorizontal) 0 else grid
        val newColumn = column + if (isHorizontal) grid else 0
        if (newRow < 0 || newRow >= Card16Layout.GRID || newColumn < 0 || newColumn >= Card16Layout.GRID) {
            throw EBRuntimeException()
        }
        return AnimatorSet().apply {
            val translationAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "translationX" else "translationY"
                val valueFrom = 0f
                val valueTo = ((size + card16Layout.spacing) * grid).toFloat()
                setFloatValues(valueFrom, valueTo)
                this.duration = duration
                interpolator = AccelerateDecelerateInterpolator()
                addListener(CardAnimatorListener(
                        onStart = onStart,
                        onEnd = {
                            setIndex(newRow, newColumn)
                            if (isHorizontal) translationX = 0f else translationY = 0f
                            onEnd?.invoke(it)
                        }))
            }
            play(translationAnimator)
            this.startDelay = startDelay
            setTarget(this@Card)
            start()
        }
    }

    //*****************************************************************************************************************

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
