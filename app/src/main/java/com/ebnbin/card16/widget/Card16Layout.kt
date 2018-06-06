package com.ebnbin.card16.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.ebnbin.eb.util.dpInt
import kotlin.math.min

/**
 * 16 卡片布局.
 *
 * 包含 16 个 [CardLayout] 和 1 个 [BigCardLayout].
 *
 * 不要设置 padding.
 */
class Card16Layout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
        defStyleRes: Int = 0) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    init {
        // 添加 16 个 CardLayout.
        for (row in 0 until GRID) {
            for (column in 0 until GRID) {
                val cardLayout = CardLayout(this.context)
                cardLayout.setIndex(row, column)
                addView(cardLayout)
            }
        }

        // 添加 BigCardLayout.
        val bigCardLayout = BigCardLayout(this.context)
        addView(bigCardLayout)
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
    private val spacing = SPACING_DP.dpInt
    /**
     * 边距.
     */
    private val edgeSpacing = EDGE_SPACING_DP.dpInt

    /**
     * 正方形宽高.
     */
    private var size = 0

    /**
     * [CardLayout] 宽高.
     */
    private var cardSize = 0

    /**
     * [BigCardLayout] 宽高.
     */
    private var bigCardSize = 0

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
     * [CardLayout] 左位置.
     */
    private val cardLefts = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [CardLayout] 上位置.
     */
    private val cardTops = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [CardLayout] 右位置.
     */
    private val cardRights = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [CardLayout] 下位置.
     */
    private val cardBottoms = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [CardLayout] 水平方向中心位置.
     */
    private val cardCenterXs = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [CardLayout] 垂直方向中心位置.
     */
    private val cardCenterYs = Array(GRID) { Array(GRID) { 0 } }

    /**
     * [BigCardLayout] 左位置.
     */
    var bigCardLeft = 0
        private set
    /**
     * [BigCardLayout] 上位置.
     */
    var bigCardTop = 0
        private set
    /**
     * [BigCardLayout] 右位置.
     */
    var bigCardRight = 0
        private set
    /**
     * [BigCardLayout] 下位置.
     */
    var bigCardBottom = 0
        private set
    /**
     * [BigCardLayout] 水平方向中心位置.
     */
    var bigCardCenterX = 0
        private set
    /**
     * [BigCardLayout] 垂直方向中心位置.
     */
    var bigCardCenterY = 0
        private set

    /**
     * 从 [CardLayout] 放大到 [BigCardLayout] 比例. 大等于 1f.
     */
    var scaleIn = 1f
        private set
    /**
     * 从 [BigCardLayout] 缩小到 [CardLayout] 比例. 小等于 1f.
     */
    var scaleOut = 1f
        private set

    /**
     * [CardLayout] 测量宽高.
     */
    private var cardMeasureSpec = 0

    /**
     * [BigCardLayout] 测量宽高.
     */
    private var bigCardMeasureSpec = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 计算.
        val newWidthMeasureSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val newHeightMeasureSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMeasureSpecSize != newWidthMeasureSpecSize || heightMeasureSpecSize != newHeightMeasureSpecSize) {
            widthMeasureSpecSize = newWidthMeasureSpecSize
            heightMeasureSpecSize = newHeightMeasureSpecSize
            size = min(widthMeasureSpecSize, heightMeasureSpecSize)
            cardSize = (size - 2 * edgeSpacing - (GRID - 1) * spacing) / GRID
            bigCardSize = GRID * cardSize + (GRID - 1) * spacing
            spacingLeft = (widthMeasureSpecSize - bigCardSize) / 2
            spacingRight = widthMeasureSpecSize - spacingLeft - bigCardSize
            spacingTop = (heightMeasureSpecSize - bigCardSize) / 2
            spacingBottom = heightMeasureSpecSize - spacingTop - bigCardSize
            for (row in 0 until GRID) {
                for (column in 0 until GRID) {
                    val cardLeft = spacingLeft + column * (cardSize + spacing)
                    val cardTop = spacingTop + row * (cardSize + spacing)
                    val cardRight = cardLeft + cardSize
                    val cardBottom = cardTop + cardSize
                    val cardCenterX = (cardLeft + cardRight) / 2
                    val cardCenterY = (cardTop + cardBottom) / 2
                    cardLefts[row][column] = cardLeft
                    cardTops[row][column] = cardTop
                    cardRights[row][column] = cardRight
                    cardBottoms[row][column] = cardBottom
                    cardCenterXs[row][column] = cardCenterX
                    cardCenterYs[row][column] = cardCenterY
                }
            }
            bigCardLeft = spacingLeft
            bigCardTop = spacingTop
            bigCardRight = bigCardLeft + bigCardSize
            bigCardBottom = bigCardTop + bigCardSize
            bigCardCenterX = (bigCardLeft + bigCardRight) / 2
            bigCardCenterY = (bigCardTop + bigCardBottom) / 2
            scaleIn = if (cardSize == 0 || bigCardSize == 0) 1f else bigCardSize.toFloat() / cardSize
            scaleOut = 1f / scaleIn
            cardMeasureSpec = MeasureSpec.makeMeasureSpec(cardSize, MeasureSpec.EXACTLY)
            bigCardMeasureSpec = MeasureSpec.makeMeasureSpec(bigCardSize, MeasureSpec.EXACTLY)
        }

        // 测量子视图.
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            when (child) {
                is CardLayout -> child.measure(cardMeasureSpec, cardMeasureSpec)
                is BigCardLayout -> child.measure(bigCardMeasureSpec, bigCardMeasureSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 布局子视图.
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            when (child) {
                is CardLayout -> {
                    val row = child.row
                    val column = child.column
                    if (isIndexValid(row, column)) {
                        child.layout(cardLefts[row][column], cardTops[row][column], cardRights[row][column],
                                cardBottoms[row][column])
                    }
                }
                is BigCardLayout -> {
                    child.layout(bigCardLeft, bigCardTop, bigCardRight, bigCardBottom)
                }
            }
        }
    }

    /**
     * 返回行列是否有效.
     */
    private fun isIndexValid(row: Int, column: Int) = row in 0 until GRID && column in 0 until GRID

    /**
     * 根据行列返回 [CardLayout] 左位置. 如果行列无效则返回 0.
     */
    fun getCardLeft(row: Int, column: Int) = cardLefts.getOrNull(row)?.getOrNull(column) ?: 0

    /**
     * 根据行列返回 [CardLayout] 上位置. 如果行列无效则返回 0.
     */
    fun getCardTop(row: Int, column: Int) = cardTops.getOrNull(row)?.getOrNull(column) ?: 0

    /**
     * 根据行列返回 [CardLayout] 右位置. 如果行列无效则返回 0.
     */
    fun getCardRight(row: Int, column: Int) = cardRights.getOrNull(row)?.getOrNull(column) ?: 0

    /**
     * 根据行列返回 [CardLayout] 下位置. 如果行列无效则返回 0.
     */
    fun getCardBottom(row: Int, column: Int) = cardBottoms.getOrNull(row)?.getOrNull(column) ?: 0

    /**
     * 根据行列返回 [CardLayout] 水平方向中心位置. 如果行列无效则返回 0.
     */
    fun getCardCenterX(row: Int, column: Int) = cardCenterXs.getOrNull(row)?.getOrNull(column) ?: 0

    /**
     * 根据行列返回 [CardLayout] 垂直方向中心位置. 如果行列无效则返回 0.
     */
    fun getCardCenterY(row: Int, column: Int) = cardCenterYs.getOrNull(row)?.getOrNull(column) ?: 0

    /**
     * 设置全部 [CardLayout] 的可见性.
     *
     * @param rowExcept 除外的 [CardLayout] 的行.
     *
     * @param columnExcept 除外的 [CardLayout] 的列.
     */
    fun setAllCardLayoutsVisibility(visibility: Int, rowExcept: Int? = null, columnExcept: Int? = null) {
        for (index in 0 until childCount) {
            val cardLayout = getChildAt(index) as? CardLayout ?: continue
            if (rowExcept != null &&
                    columnExcept != null &&
                    cardLayout.row == rowExcept &&
                    cardLayout.column == columnExcept) continue
            cardLayout.visibility = visibility
        }
    }

    /**
     * 设置全部 [CardLayout] 的可点击性.
     *
     * @param rowExcept 除外的 [CardLayout] 的行.
     *
     * @param columnExcept 除外的 [CardLayout] 的列.
     */
    fun setAllCardLayoutsClickable(isClickable: Boolean, rowExcept: Int? = null, columnExcept: Int? = null) {
        for (index in 0 until childCount) {
            val cardLayout = getChildAt(index) as? CardLayout ?: continue
            if (rowExcept != null &&
                    columnExcept != null &&
                    cardLayout.row == rowExcept &&
                    cardLayout.column == columnExcept) continue
            cardLayout.isClickable = isClickable
        }
    }

    /**
     * 根据行列返回 [CardLayout].
     */
    fun getCardLayout(row: Int, column: Int): CardLayout? {
        if (!isIndexValid(row, column)) return null
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child is CardLayout && child.row == row && child.column == column) return child
        }
        return null
    }

    /**
     * 返回 [BigCardLayout].
     */
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
        private const val GRID = 4

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
