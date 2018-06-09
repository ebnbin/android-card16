package com.ebnbin.card16.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.ebnbin.eb.util.dpInt
import kotlin.math.min

/**
 * 16 卡片布局.
 *
 * 包含 16 个 [Card] 和 1 个 [BigCard].
 *
 * 不要设置 padding.
 */
class Card16Layout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
        defStyleRes: Int = 0) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    /**
     * 16 张卡片.
     */
    val cards = Array(GRID) { row ->
        Array(GRID) { column ->
            Card(this.context, row, column).apply {
                this@Card16Layout.addView(this)
            }
        }
    }

    /**
     * 大卡片.
     */
    val bigCard = BigCard(this.context).apply {
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
     * [Card] 宽高.
     */
    var cardSize = 0
        private set

    /**
     * [BigCard] 宽高.
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
     * [Card] 左位置.
     */
    val cardLefts = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [Card] 上位置.
     */
    val cardTops = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [Card] 右位置.
     */
    val cardRights = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [Card] 下位置.
     */
    val cardBottoms = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [Card] 水平方向中心位置.
     */
    val cardCenterXs = Array(GRID) { Array(GRID) { 0 } }
    /**
     * [Card] 垂直方向中心位置.
     */
    val cardCenterYs = Array(GRID) { Array(GRID) { 0 } }

    /**
     * [BigCard] 左位置.
     */
    private var bigCardLeft = 0
    /**
     * [BigCard] 上位置.
     */
    private var bigCardTop = 0
    /**
     * [BigCard] 右位置.
     */
    private var bigCardRight = 0
    /**
     * [BigCard] 下位置.
     */
    private var bigCardBottom = 0
    /**
     * [BigCard] 水平方向中心位置.
     */
    var bigCardCenterX = 0
        private set
    /**
     * [BigCard] 垂直方向中心位置.
     */
    var bigCardCenterY = 0
        private set

    /**
     * 从 [Card] 放大到 [BigCard] 比例. 大等于 1f.
     */
    var scaleIn = 1f
        private set
    /**
     * 从 [BigCard] 缩小到 [Card] 比例. 小等于 1f.
     */
    var scaleOut = 1f
        private set

    /**
     * [Card] 测量宽高.
     */
    private var cardMeasureSpec = 0

    /**
     * [BigCard] 测量宽高.
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
            cardIndexes { row, column ->
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
        cards { it.measure(cardMeasureSpec, cardMeasureSpec) }
        bigCard.measure(bigCardMeasureSpec, bigCardMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 布局子视图.
        cardsIndexed { row, column, card ->
            card.layout(
                    cardLefts[row][column],
                    cardTops[row][column],
                    cardRights[row][column],
                    cardBottoms[row][column])
        }
        bigCard.layout(bigCardLeft, bigCardTop, bigCardRight, bigCardBottom)
    }

    /**
     * 遍历全部卡片索引.
     *
     * @param rowExcept 除外的行.
     *
     * @param columnExcept 除外的列.
     */
    fun cardIndexes(rowExcept: Int? = null, columnExcept: Int? = null, action: (row: Int, column: Int) -> Unit) {
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
    fun cards(rowExcept: Int? = null, columnExcept: Int? = null, action: (Card) -> Unit) {
        cardIndexes(rowExcept, columnExcept) { row, column -> action(cards[row][column]) }
    }

    /**
     * 遍历全部卡片, 带索引.
     *
     * @param rowExcept 除外的行.
     *
     * @param columnExcept 除外的列.
     */
    fun cardsIndexed(rowExcept: Int? = null, columnExcept: Int? = null,
            action: (row: Int, column: Int, Card) -> Unit) {
        cardIndexes(rowExcept, columnExcept) { row, column -> action(row, column, cards[row][column]) }
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
