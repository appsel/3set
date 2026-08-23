package com.example.threeSet.domain.logic

object DeckCode {
    private const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val CODE_LENGTH = 6

    fun decode(code: String): Long {
        val normalized = code.uppercase().trim()
        require(normalized.length == CODE_LENGTH) { "Code must be exactly $CODE_LENGTH characters" }
        var value = 0L
        for (char in normalized) {
            val index = ALPHABET.indexOf(char)
            require(index >= 0) { "Invalid character: $char" }
            value = value * 36 + index
        }
        return value
    }

    fun decodeOrNull(code: String): Long? = runCatching { decode(code) }.getOrNull()

    fun encode(seed: Long): String {
        var value = seed
        val chars = CharArray(CODE_LENGTH)
        for (i in CODE_LENGTH - 1 downTo 0) {
            chars[i] = ALPHABET[(value % 36).toInt()]
            value /= 36
        }
        return String(chars)
    }
}
