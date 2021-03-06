package com.ebnbin.card16.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.ebnbin.card16.card.Card
import com.ebnbin.card16.card.CharCard
import com.ebnbin.card16.util.RandomHelper
import com.ebnbin.card16.util.dp

/**
 * 卡片.
 *
 * @param row 行.
 *
 * @param column 列.
 */
@SuppressLint("ViewConstructor")
class CardView(context: Context, val row: Int, val column: Int) :
        BaseCardView(context, DEF_ELEVATION_DP.dp, DEF_RADIUS_DP.dp) {
    override fun getCardFrontView(card: Card) = card.getFrontView()

    override fun getCardBackView(card: Card) = card.getBackView()

    init {
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
                            card16Layout.bigCardView.setOnClickListener {
                                card16Layout.bigCardView.setOnClickListener(null)
                                card16Layout.bigCardView.animateZoomOut(
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
                        onEnd = null,
                        card = this.card)
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
                onEnd = null,
                card = run {
                    if (row == 1 && column == 0) CharCard(context).apply { setChar('C') }
                    else if (row == 1 && column == 1) CharCard(context).apply { setChar('A') }
                    else if (row == 1 && column == 2) CharCard(context).apply { setChar('R') }
                    else if (row == 1 && column == 3) CharCard(context).apply { setChar('D') }
                    else if (row == 2 && column == 1) CharCard(context).apply { setChar('1') }
                    else if (row == 2 && column == 2) CharCard(context).apply { setChar('6') }
                    else null
//                    IndexCard(context).apply {
//                        setIndex(row, column)
//                    }
                })
    }

    //*****************************************************************************************************************

    fun animateZoomIn(
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasCardBack: Boolean,
            duration: Long,
            startDelay: Long,
            onCut: (() -> Unit)?,
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
        if (grid == 0) throw RuntimeException()
        val newRow = row + if (isHorizontal) 0 else grid
        val newColumn = column + if (isHorizontal) grid else 0
        if (newRow < 0 || newRow >= Card16Layout.GRID || newColumn < 0 || newColumn >= Card16Layout.GRID) {
            throw RuntimeException()
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
                            card16Layout.cardViews[newRow][newColumn].visibility = View.VISIBLE
                            visibility = View.GONE
                            onEnd?.invoke(it)
                        }))
            }
            play(translationAnimator)
            this.startDelay = startDelay
            setTarget(this@CardView)
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
