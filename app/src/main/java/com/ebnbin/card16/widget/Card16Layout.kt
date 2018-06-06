package com.ebnbin.card16.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.ebnbin.eb.util.dpInt
import kotlin.math.min

/**
 * 16 卡片布局.
 *
 * 忽略边距.
 */
class Card16Layout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
        defStyleRes: Int = 0) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    init {
        for (row in 0 until GRID) {
            for (column in 0 until GRID) {
                val cardLayout = CardLayout(this.context)
                cardLayout.setIndex(row, column)
                addView(cardLayout)
            }
        }
        val bigCardLayout = BigCardLayout(this.context)
        addView(bigCardLayout)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMeasureSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasureSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val minMeasureSpecSize = min(widthMeasureSpecSize, heightMeasureSpecSize)
        val spacing = SPACING_DP.dpInt
        val childSize = (minMeasureSpecSize - (GRID + 1) * spacing) / GRID
        val childMeasureSpec = MeasureSpec.makeMeasureSpec(childSize, MeasureSpec.EXACTLY)
        val bigChildSize = (GRID - 1) * spacing + GRID * childSize
        val bigChildMeasureSpec = MeasureSpec.makeMeasureSpec(bigChildSize, MeasureSpec.EXACTLY)
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            when (child) {
                is CardLayout -> child.measure(childMeasureSpec, childMeasureSpec)
                is BigCardLayout -> child.measure(bigChildMeasureSpec, bigChildMeasureSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t
        val minSize = min(width, height)
        val spacing = SPACING_DP.dpInt
        val childSize = (minSize - (GRID + 1) * spacing) / GRID
        val bigChildSize = (GRID - 1) * spacing + GRID * childSize
        cardScale = ((GRID - 1) * spacing + GRID * childSize).toFloat() / childSize
        cardScaleInverse = 1f / cardScale
        val leftSpacing = (width - (GRID - 1) * spacing - GRID * childSize) / 2
        val topSpacing = (height - (GRID - 1) * spacing - GRID * childSize) / 2
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            when (child) {
                is CardLayout -> {
                    val row = child.row
                    val column = child.column
                    val childL = leftSpacing + (childSize + spacing) * column
                    val childT = topSpacing + (childSize + spacing) * row
                    val childR = childL + childSize
                    val childB = childT + childSize
                    child.layout(childL, childT, childR, childB)
                }
                is BigCardLayout -> {
                    val childL = leftSpacing
                    val childT = topSpacing
                    val childR = childL + bigChildSize
                    val childB = childT + bigChildSize
                    child.layout(childL, childT, childR, childB)
                }
            }
        }
    }

    var cardScale = 0f
    var cardScaleInverse = 0f

    fun setOtherCardsVisibility(row: Int, column: Int, visibility: Int) {
        for (index in 0 until childCount) {
            val cardLayout = getChildAt(index) as? CardLayout ?: continue
            if (cardLayout.row == row && cardLayout.column == column) continue
            cardLayout.visibility = visibility
        }
    }

    fun setAllCardsClickable(isClickable: Boolean) {
        for (index in 0 until childCount) {
            val cardLayout = getChildAt(index) as? CardLayout ?: continue
            cardLayout.isClickable = isClickable
        }
    }

    fun getBigCardLayout(): BigCardLayout? {
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child is BigCardLayout) return child
        }
        return null
    }

    companion object {
        /**
         * 行列数.
         */
        const val GRID = 4

        /**
         * 间距 dp.
         */
        const val SPACING_DP = 8f
    }
}
