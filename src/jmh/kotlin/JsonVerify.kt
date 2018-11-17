package com.github.h0tk3y.betterParseBenchmark

import com.github.h0tk3y.betterParse.grammar.*
import jsonSample1K

fun main() {
    val parseToEnd = SimpleJsonGrammar.parseToEnd(jsonSample1K)
    println(parseToEnd)
}