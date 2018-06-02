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
    private val cardLayouts = Array(GRID) {
        Array(GRID) {
            CardLayout(this.context).apply {
                this@Card16Layout.addView(this)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMeasureSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasureSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val minMeasureSpecSize = min(widthMeasureSpecSize, heightMeasureSpecSize)
        setMeasuredDimension(minMeasureSpecSize, minMeasureSpecSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val size = r - l
        val spacing = SPACING_DP.dpInt
        val childSize = (size - (GRID + 1) * spacing) / GRID
        val firstSpacing = (size - (GRID - 1) * spacing - GRID * childSize) / 2

        cardLayouts.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, cardLayout ->
                val childL = firstSpacing + (childSize + spacing) * columnIndex
                val childT = firstSpacing + (childSize + spacing) * rowIndex
                val childR = childL + childSize
                val childB = childT + childSize
                cardLayout.layout(childL, childT, childR, childB)
            }
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
