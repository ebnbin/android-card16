package com.ebnbin.card16.widget

import android.content.Context

/**
 * 基础卡片.
 */
abstract class BaseCard(context: Context) : BaseCardLayout(context) {
    protected val card16Layout get() = parent as Card16Layout
}
