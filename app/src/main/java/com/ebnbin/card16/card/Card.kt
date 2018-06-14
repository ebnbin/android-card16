package com.ebnbin.card16.card

import android.content.Context
import android.view.View

abstract class Card(protected val context: Context) {
    abstract fun getFrontView(): View?
    abstract fun getBackView(): View?
    abstract fun getBigFrontView(): View?
    abstract fun getBigBackView(): View?
}
