package com.ebnbin.card16.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.ebnbin.eb.util.dpInt
import kotlin.math.min

/**
 * 16 卡片布局.
 *
 * 限制宽高. 忽略边距.
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
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMeasureSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasureSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val minMeasureSpecSize = min(widthMeasureSpecSize, heightMeasureSpecSize)
        setMeasuredDimension(minMeasureSpecSize, minMeasureSpecSize)

        val spacing = SPACING_DP.dpInt
        val childSize = (minMeasureSpecSize - (GRID + 1) * spacing) / GRID
        val childMeasureSpec = MeasureSpec.makeMeasureSpec(childSize, MeasureSpec.EXACTLY)
        for (index in 0 until childCount) {
            val cardLayout = getChildAt(index) as? CardLayout ?: continue
            cardLayout.measure(childMeasureSpec, childMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val size = r - l
        val spacing = SPACING_DP.dpInt
        val childSize = (size - (GRID + 1) * spacing) / GRID
        val firstSpacing = (size - (GRID - 1) * spacing - GRID * childSize) / 2
        for (index in 0 until childCount) {
            val cardLayout = getChildAt(index) as? CardLayout ?: continue
            val row = cardLayout.row
            val column = cardLayout.column
            val childL = firstSpacing + (childSize + spacing) * column
            val childT = firstSpacing + (childSize + spacing) * row
            val childR = childL + childSize
            val childB = childT + childSize
            cardLayout.layout(childL, childT, childR, childB)
        }
    }

    companion object {
        /**
         * 行列数.
         */
        private const val GRID = 4

        /**
         * 间距 dp.
         */
        private const val SPACING_DP = 8f
    }
}
