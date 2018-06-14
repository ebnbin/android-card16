package com.ebnbin.card16.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.ebnbin.eb.util.dpInt
import kotlin.math.min

/**
 * 16 卡片布局.
 *
 * 包含 16 个 [CardView] 和 1 个 [BigCardView].
 *
 * 不要设置 padding.
 */
class Card16Layout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
        defStyleRes: Int = 0) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    /**
     * 16 张卡片.
     */
    val cardViews = Array(GRID) { row ->
        Array(GRID) { column ->
            CardView(this.context, row, column).apply {
                this@Card16Layout.addView(this)
            }
        }
    }

    /**
     * 大卡片.
     */
    val bigCardView = BigCardView(this.context).apply {
        this@Card16Layout.addView(this)
    }

    /**
     * 宽测量大小.
     */
    private var widthMeasureSpecSize = 0
    /**
     * 高测量大小.
     */
    private var heightMeasureSpecSize = 0

    /**
     * 间距.
     */
    val spacing = SPACING_DP.dpInt
    /**
     * 边距.
     */
    private val edgeSpacing = EDGE_SPACING_DP.dpInt

    /**
     * 正方形宽高.
     */
    private var size = 0

    /**
     * [CardView] 宽高.
     */
    var cardViewSize = 0
        private set

    /**
     * [BigCardView] 宽高.
     */
    var bigCardViewSize = 0
        private set

    /**
     * 左边距.
     */
    private var spacingLeft = 0
    /**
     * 右边距.
     */
    private var spacingRight = 0
    /**
     * 上边距.
     */
    private var spacingTop = 0
    /**
     * 下边距.
     */
    private var spacingBottom = 0

    /**
     * [CardView] 左位置.
     */
    val cardViewLefts = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [CardView] 上位置.
     */
    val cardViewTops = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [CardView] 右位置.
     */
    val cardViewRights = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [CardView] 下位置.
     */
    val cardViewBottoms = Array(GRID) { Array(GRID) { 0 } }

    /**
     * [BigCardView] 左位置.
     */
    private var bigCardViewLeft = 0
    /**
     * [BigCardView] 上位置.
     */
    private var bigCardViewTop = 0
    /**
     * [BigCardView] 右位置.
     */
    private var bigCardViewRight = 0
    /**
     * [BigCardView] 下位置.
     */
    private var bigCardViewBottom = 0

    /**
     * [CardView] 测量宽高.
     */
    private var cardViewMeasureSpec = 0

    /**
     * [BigCardView] 测量宽高.
     */
    private var bigCardViewMeasureSpec = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 计算.
        val newWidthMeasureSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val newHeightMeasureSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMeasureSpecSize != newWidthMeasureSpecSize || heightMeasureSpecSize != newHeightMeasureSpecSize) {
            widthMeasureSpecSize = newWidthMeasureSpecSize
            heightMeasureSpecSize = newHeightMeasureSpecSize
            size = min(widthMeasureSpecSize, heightMeasureSpecSize)
            cardViewSize = (size - 2 * edgeSpacing - (GRID - 1) * spacing) / GRID
            bigCardViewSize = GRID * cardViewSize + (GRID - 1) * spacing
            spacingLeft = (widthMeasureSpecSize - bigCardViewSize) / 2
            spacingRight = widthMeasureSpecSize - spacingLeft - bigCardViewSize
            spacingTop = (heightMeasureSpecSize - bigCardViewSize) / 2
            spacingBottom = heightMeasureSpecSize - spacingTop - bigCardViewSize
            cardViewIndexes { row, column ->
                val cardLeft = spacingLeft + column * (cardViewSize + spacing)
                val cardTop = spacingTop + row * (cardViewSize + spacing)
                val cardRight = cardLeft + cardViewSize
                val cardBottom = cardTop + cardViewSize
                cardViewLefts[row][column] = cardLeft
                cardViewTops[row][column] = cardTop
                cardViewRights[row][column] = cardRight
                cardViewBottoms[row][column] = cardBottom
            }
            bigCardViewLeft = spacingLeft
            bigCardViewTop = spacingTop
            bigCardViewRight = bigCardViewLeft + bigCardViewSize
            bigCardViewBottom = bigCardViewTop + bigCardViewSize
            cardViewMeasureSpec = MeasureSpec.makeMeasureSpec(cardViewSize, MeasureSpec.EXACTLY)
            bigCardViewMeasureSpec = MeasureSpec.makeMeasureSpec(bigCardViewSize, MeasureSpec.EXACTLY)
        }

        // 测量子视图.
        cardViews { it.measure(cardViewMeasureSpec, cardViewMeasureSpec) }
        bigCardView.measure(bigCardViewMeasureSpec, bigCardViewMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 布局子视图.
        cardViewsIndexed { row, column, cardView ->
            cardView.layout(
                    cardViewLefts[row][column],
                    cardViewTops[row][column],
                    cardViewRights[row][column],
                    cardViewBottoms[row][column])
        }
        bigCardView.layout(bigCardViewLeft, bigCardViewTop, bigCardViewRight, bigCardViewBottom)
    }

    /**
     * 遍历全部卡片索引.
     *
     * @param rowExcept 除外的行.
     *
     * @param columnExcept 除外的列.
     */
    fun cardViewIndexes(rowExcept: Int? = null, columnExcept: Int? = null, action: (row: Int, column: Int) -> Unit) {
        for (row in 0 until GRID) {
            for (column in 0 until GRID) {
                if (row == rowExcept && column == columnExcept) continue
                action(row, column)
            }
        }
    }

    /**
     * 遍历全部卡片.
     *
     * @param rowExcept 除外的行.
     *
     * @param columnExcept 除外的列.
     */
    fun cardViews(rowExcept: Int? = null, columnExcept: Int? = null, action: (CardView) -> Unit) {
        cardViewIndexes(rowExcept, columnExcept) { row, column -> action(cardViews[row][column]) }
    }

    /**
     * 遍历全部卡片, 带索引.
     *
     * @param rowExcept 除外的行.
     *
     * @param columnExcept 除外的列.
     */
    fun cardViewsIndexed(rowExcept: Int? = null, columnExcept: Int? = null,
            action: (row: Int, column: Int, CardView) -> Unit) {
        cardViewIndexes(rowExcept, columnExcept) { row, column -> action(row, column, cardViews[row][column]) }
    }

    companion object {
        /**
         * 行列数.
         */
        const val GRID = 4

        /**
         * 间距 dp.
         */
        private const val SPACING_DP = 8f

        /**
         * 边距 dp.
         */
        private const val EDGE_SPACING_DP = 16f
    }
}
