package com.ebnbin.card16.widget

import android.animation.Animator
import android.content.Context
import com.ebnbin.card16.card.Card
import com.ebnbin.card16.card.NewCard
import com.ebnbin.card16.util.dp

/**
 * 大卡片.
 */
class BigCardView(context: Context) : BaseCardView(context, DEF_ELEVATION_DP.dp, DEF_RADIUS_DP.dp) {
    override fun getCardFrontView(card: Card) = card.getBigFrontView()

    override fun getCardBackView(card: Card) = card.getBigBackView()

    init {
        setOnLongClickListener {
            animateCut(
                    isHorizontal = true,
                    isClockwise = false,
                    hasCardBack = false,
                    duration = 300L,
                    startDelay = 0L,
                    onCut = null,
                    onStart = null,
                    onEnd = null,
                    card = NewCard(this.context))
            true
        }
    }

    //*****************************************************************************************************************

    fun animateZoomOut(
            row: Int,
            column: Int,
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasCardBack: Boolean,
            duration: Long,
            startDelay: Long,
            onCut: (() -> Unit)?,
            onStart: ((Animator) -> Unit)?,
            onEnd: ((Animator) -> Unit)?) = internalAnimateZoomInOut(
            isBigCard = true,
            isIn = false,
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
