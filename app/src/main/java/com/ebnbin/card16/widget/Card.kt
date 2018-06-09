package com.ebnbin.card16.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import com.ebnbin.eb.util.EBRuntimeException
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
    private val button = Button(this.context).apply {
        this@Card.addView(this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    init {
        setOnClickListener {
            if (row % 2 == column % 2) {
                animateZoomIn(
                        isHorizontal = false,
                        isClockwise = false,
                        hasBack = false,
                        rotationDuration = 300L)
            } else {
                animateCut(
                        isHorizontal = true,
                        isClockwise = false,
                        hasBack = false,
                        rotationDuration = 300L)
            }
        }
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

        button.text = "$row-$column"
    }

    /**
     * 卡片放大动画.
     *
     * 需要调用 [BigCard.animateZoomIn].
     *
     * [Card] 负责前半部分的动画, 衔接 [BigCard] 负责后半部分的动画, 完成从小卡片到大卡片的放大动画.
     *
     * [Card] 完成前半部分动画后隐藏自己. [BigCard] 高度降低前隐藏其他 [Card].
     *
     * @param isHorizontal 水平方向或垂直方向翻转.
     *
     * @param isClockwise 从上往下或从左往右视角, 顺时针或逆时针翻转.
     *
     * @param hasBack 卡片翻转时是否有背面 (空白面).
     *
     * @param rotationDuration 翻转动画时长 (两部分动画平分).
     *
     * @param onCardCut 卡片切换回调.
     */
    fun animateZoomIn(
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasBack: Boolean,
            rotationDuration: Long,
            onCardCut: (() -> Unit)? = null) {
        AnimatorSet().apply {
            val rotationAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "rotationY" else "rotationX"
                val valueFrom = 0f
                val valueTo = (if (isClockwise) -1 else 1) * (if (hasBack) 180f else 90f)
                setFloatValues(valueFrom, valueTo)
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            val translationXAnimator = ObjectAnimator().apply {
                propertyName = "translationX"
                val valueFrom = 0f
                val valueTo = (card16Layout.bigCardCenterX - card16Layout.cardCenterXs[row][column]) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val translationYAnimator = ObjectAnimator().apply {
                propertyName = "translationY"
                val valueFrom = 0f
                val valueTo = (card16Layout.bigCardCenterY - card16Layout.cardCenterYs[row][column]) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val scaleXAnimator = ObjectAnimator().apply {
                propertyName = "scaleX"
                val valueFrom = 1f
                val valueTo = (1f + card16Layout.scaleIn) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val scaleYAnimator = ObjectAnimator().apply {
                propertyName = "scaleY"
                val valueFrom = 1f
                val valueTo = (1f + card16Layout.scaleIn) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val radiusAnimator = ObjectAnimator().apply {
                propertyName = "radius"
                val valueFrom = defRadius
                val valueTo = (defRadius + card16Layout.bigCard.defRadius) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            val elevationAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val valueFrom = maxElevation
                val valueTo = (maxElevation + card16Layout.bigCard.maxElevation) / 2f
                setFloatValues(valueFrom, valueTo)
            }
            playTogether(rotationAnimator, translationXAnimator, translationYAnimator, scaleXAnimator,
                    scaleYAnimator, radiusAnimator, elevationAnimator)
            duration = rotationDuration / 2L
            interpolator = AccelerateInterpolator()
            val animatorListener = CardAnimatorListener(onEnd = {
                visibility = View.GONE

                card16Layout.bigCard.animateZoomIn(row, column, isHorizontal, isClockwise, hasBack, rotationDuration)

                onCardCut?.invoke()
            })
            addListener(animatorListener)
            setTarget(this@Card)
        }.start()
    }

    /**
     * 只能由 [BigCard.animateZoomOut] 调用.
     */
    fun animateZoomOut(
            isHorizontal: Boolean,
            isClockwise: Boolean,
            hasBack: Boolean,
            rotationDuration: Long) {
        AnimatorSet().apply {
            val rotationAnimator = ObjectAnimator().apply {
                propertyName = if (isHorizontal) "rotationY" else "rotationX"
                val valueFrom = (if (isClockwise) 1 else -1) * (if (hasBack) 180f else 90f)
                val valueTo = 0f
                setFloatValues(valueFrom, valueTo)
                val animatorUpdateListener = CardFrontBackAnimatorUpdateListener(isClockwise)
                addUpdateListener(animatorUpdateListener)
            }
            val translationXAnimator = ObjectAnimator().apply {
                propertyName = "translationX"
                val valueFrom = (card16Layout.bigCardCenterX - card16Layout.cardCenterXs[row][column]) / 2f
                val valueTo = 0f
                setFloatValues(valueFrom, valueTo)
            }
            val translationYAnimator = ObjectAnimator().apply {
                propertyName = "translationY"
                val valueFrom = (card16Layout.bigCardCenterY - card16Layout.cardCenterYs[row][column]) / 2f
                val valueTo = 0f
                setFloatValues(valueFrom, valueTo)
            }
            val scaleXAnimator = ObjectAnimator().apply {
                propertyName = "scaleX"
                val valueFrom = (card16Layout.scaleIn + 1f) / 2f
                val valueTo = 1f
                setFloatValues(valueFrom, valueTo)
            }
            val scaleYAnimator = ObjectAnimator().apply {
                propertyName = "scaleY"
                val valueFrom = (card16Layout.scaleIn + 1f) / 2f
                val valueTo = 1f
                setFloatValues(valueFrom, valueTo)
            }
            val radiusAnimator = ObjectAnimator().apply {
                propertyName = "radius"
                val valueFrom = (card16Layout.bigCard.defRadius + defRadius) / 2f
                val valueTo = defRadius
                setFloatValues(valueFrom, valueTo)
            }
            val elevationAnimator = ObjectAnimator().apply {
                propertyName = "elevation"
                val valueFrom = (card16Layout.bigCard.maxElevation + maxElevation) / 2f
                val valueTo = maxElevation
                setFloatValues(valueFrom, valueTo)
            }
            playTogether(rotationAnimator, translationXAnimator, translationYAnimator, scaleXAnimator,
                    scaleYAnimator, radiusAnimator, elevationAnimator)
            duration = rotationDuration - rotationDuration / 2L
            interpolator = DecelerateInterpolator()
            val animatorListener = CardAnimatorListener(onEnd = {
                // TODO
            })
            addListener(animatorListener)
            setTarget(this@Card)
        }.start()
    }

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
