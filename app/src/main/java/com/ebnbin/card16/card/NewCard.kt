package com.ebnbin.card16.card

import android.content.Context
import android.view.View
import android.widget.TextView

class NewCard(context: Context) : Card(context) {
    private val frontView = TextView(this.context).apply { text = "NewCard" }
    private val bigFrontView = TextView(this.context).apply { text = "BigNewCard" }

    override fun getFrontView() = frontView

    override fun getBackView(): View? = null

    override fun getBigFrontView() = bigFrontView

    override fun getBigBackView(): View? = null
}
