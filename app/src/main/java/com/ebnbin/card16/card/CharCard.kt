package com.ebnbin.card16.card

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.ebnbin.card16.util.dp

class CharCard(context: Context) : Card(context) {
    private val frontView = TextView(this.context).apply {
        setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = 24.dp
        gravity = Gravity.CENTER
    }
    private val bigFrontView = TextView(this.context).apply {
        setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = 96.dp
        gravity = Gravity.CENTER
    }

    fun setChar(char: Char) {
        frontView.text = char.toString()
        bigFrontView.text = char.toString()
    }

    override fun getFrontView() = frontView

    override fun getBackView(): View? = null

    override fun getBigFrontView() = bigFrontView

    override fun getBigBackView(): View? = null
}
