package com.ebnbin.card16.card

import android.content.Context
import android.view.View
import android.widget.TextView

class IndexCard(context: Context) : Card(context) {
    private val frontView = TextView(this.context)
    private val bigFrontView = TextView(this.context)

    override fun getFrontView() = frontView

    override fun getBackView(): View? = null

    override fun getBigFrontView() = bigFrontView

    override fun getBigBackView(): View? = null

    private var row = -1
    private var column = -1

    fun setIndex(row: Int, column: Int) {
        if (this.row == row && this.column == column) return
        this.row = row
        this.column = column

        frontView.text = "$row-$column"
        bigFrontView.text = "big-$row-$column"
    }
}
