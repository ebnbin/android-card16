package com.ebnbin.card16.util

import java.util.Random

/**
 * 随机数帮助类.
 */
object RandomHelper {
    private val random = Random()

    fun nextBoolean() = random.nextBoolean()

    fun next() = random.nextDouble()

    /**
     * 从 [from] (包括) 到 [to] (不包括) 的随机数.
     */
    fun nextLong(from: Long, to: Long) = from + ((to - from) * next()).toLong()
}
