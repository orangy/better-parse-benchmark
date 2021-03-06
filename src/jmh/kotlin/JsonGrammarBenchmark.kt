package com.github.h0tk3y.betterParseBenchmark

import SimpleJsonGrammar
import com.github.h0tk3y.betterParse.grammar.*
import jsonSample1K
import kotlinx.serialization.json.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.*

open class JsonGrammar {
    @Benchmark
    open fun jsonBetterParse(bh: Blackhole?) {
        val parseToEnd = SimpleJsonGrammar.parseToEnd(jsonSample1K)
        bh?.consume(parseToEnd)
    }
    
    @Benchmark
    open fun jsonSerializer(bh: Blackhole?) {
        val parseToEnd = JsonTreeParser(jsonSample1K).readFully()
        bh?.consume(parseToEnd)
    }
}

fun main(args: Array<String>) {
    val j = JsonGrammar()
    for (i in 1..30000) {
        j.jsonBetterParse(null)
    }
}