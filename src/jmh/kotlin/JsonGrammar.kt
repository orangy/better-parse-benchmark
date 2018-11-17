import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

object SimpleJsonGrammar : Grammar<Any?>() {
    private fun tokenIdent(text: String) = token {
        if (!it.startsWith(text)) return@token 0
        if (it.length > text.length && it[text.length].isLetterOrDigit()) return@token 0
        text.length
    }

    private fun tokenNumber() = token {
        var index = 0
        val maybeSign = it[index]
        val sign = if (maybeSign == '+' || maybeSign == '-') {
            index++
            true
        } else
            false

        val length = it.length
        while (index < length && it[index].isDigit())
            index++

        if (index < length && it[index] == '.') { // decimal
            index++
            while (index < length && it[index].isDigit())
                index++
        }
        if (index == 0 || (index == 1 && sign)) return@token 0
        index
    }

    @Suppress("unused")
    private val whiteSpace by token(ignore = true) {
        var index = 0
        val length = it.length
        while (index < length && it[index].isWhitespace())
            index++
        index
    }

    /* the regex "[^\\"]*(\\["nrtbf\\][^\\"]*)*" matches:
     * "               – opening double quote,
     * [^\\"]*         – any number of not escaped characters, nor double quotes
     * (
     *   \\["nrtbf\\]  – backslash followed by special character (\", \n, \r, \\, etc.)
     *   [^\\"]*       – and any number of non-special characters
     * )*              – repeating as a group any number of times
     * "               – closing double quote
     */
    private val stringLiteral by token {
        var index = 0
        if (it[index++] != '"') return@token 0
        val length = it.length
        while (index < length && it[index] != '"') {
            if (it[index] == '\\') { // quote 
                index++
            }
            index++
        }
        if (index == length) return@token 0 // unclosed string
        index + 1
    }

    private val comma by tokenText(",")
    private val colon by tokenText(":")

    private val openingBrace by tokenText("{")
    private val closingBrace by tokenText("}")

    private val openingBracket by tokenText("[")
    private val closingBracket by tokenText("]")

    private val nullToken by tokenIdent("null")
    private val trueToken by tokenIdent("true")
    private val falseToken by tokenIdent("false")

    private val num by tokenNumber()

    private val jsonNull: Parser<Any?> by nullToken asJust null
    private val jsonBool: Parser<Boolean> by (trueToken asJust true) or (falseToken asJust false)
    private val string: Parser<String> by (stringLiteral use { input.substring(offset + 1, offset + length - 1) })

    private val number: Parser<Double> by num use { text.toDouble() }

    private val jsonPrimitiveValue: Parser<Any?> by jsonNull or jsonBool or string or number

    private val jsonObject: Parser<Map<String, Any?>> by
        (-openingBrace * separated(string * -colon * this, comma, true) * -closingBrace)
            .map { mutableMapOf<String, Any?>().apply { it.terms.forEach { put(it.t1, it.t2) } } }

    private val jsonArray: Parser<List<Any?>> by
        (-openingBracket * separated(this, comma, true) * -closingBracket)
            .map { it.terms }

    override val rootParser by jsonPrimitiveValue or jsonObject or jsonArray
}

