package com.ebnbin.card16.util

import android.util.DisplayMetrics

private val displayMetrics: DisplayMetrics get() = app.resources.displayMetrics

/**
 * Px 每 dp.
 */
private val density get() = displayMetrics.density

/**
 * Dp 转换 px.
 */
val Int?.dp get() = if (this == null) 0f else density * this

/**
 * Dp 转换 px, 并转换为 Int.
 */
val Int?.dpInt get() = dp.toInt()

/**
 * Dp 转换 px.
 */
val Float?.dp get() = if (this == null) 0f else density * this

/**
 * Dp 转换 px, 并转换为 Int.
 */
val Float?.dpInt get() = dp.toInt()

/**
 * Px 每 sp.
 */
private val scaledDensity get() = displayMetrics.scaledDensity

/**
 * Sp 转换 px.
 */
val Int?.sp get() = if (this == null) 0f else scaledDensity * this

/**
 * Sp 转换 px, 并转换为 Int.
 */
val Int?.spInt get() = sp.toInt()

/**
 * Sp 转换 px.
 */
val Float?.sp get() = if (this == null) 0f else scaledDensity * this

/**
 * Sp 转换 px, 并转换为 Int.
 */
val Float?.spInt get() = sp.toInt()
