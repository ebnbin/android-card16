package com.ebnbin.card16.util

import android.view.View

/**
 * 返回水平方向边距, 即 [View.getPaddingLeft] + [View.getPaddingRight].
 */
val View?.paddingHorizontal get() = if (this == null) 0 else paddingLeft + paddingRight

/**
 * 返回垂直方向边距, 即 [View.getPaddingTop] + [View.getPaddingBottom].
 */
val View?.paddingVertical get() = if (this == null) 0 else paddingTop + paddingBottom

/**
 * 返回水平方向中心点位置, 忽略边距, 即 ([View.getLeft] + [View.getRight]) / 2f.
 */
val View?.centerX get() = if (this == null) 0f else (left + right) / 2f

/**
 * 返回垂直方向中心点位置, 忽略边距, 即 ([View.getTop] + [View.getBottom]) / 2f.
 */
val View?.centerY get() = if (this == null) 0f else (top + bottom) / 2f
