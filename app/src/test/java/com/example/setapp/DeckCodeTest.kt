package com.example.threeSet.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DeckCodeTest {

    @Test
    fun encodeDecode_roundTrip() {
        val seed = 123456789L
        val code = DeckCode.encode(seed)
        assertEquals(DeckCode.CODE_LENGTH, code.length)
        assertEquals(seed, DeckCode.decode(code))
    }

    @Test
    fun decode_isCaseInsensitive() {
        assertEquals(DeckCode.decode("ABC123"), DeckCode.decode("abc123"))
    }

    @Test
    fun sameSeed_producesSameDeckOrder() {
        val deckA = Deck.generateShuffledDeck(42L).map { it.id }
        val deckB = Deck.generateShuffledDeck(42L).map { it.id }
        assertEquals(deckA, deckB)
    }

    @Test
    fun differentSeeds_produceDifferentDeckOrders() {
        val deckA = Deck.generateShuffledDeck(1L).map { it.id }
        val deckB = Deck.generateShuffledDeck(2L).map { it.id }
        assertNotEquals(deckA, deckB)
    }

    @Test
    fun codeMapsToDeterministicDeck() {
        val code = "000042"
        val seed = DeckCode.decode(code)
        val deckFromSeed = Deck.generateShuffledDeck(seed).map { it.id }
        val deckFromCode = Deck.generateShuffledDeck(DeckCode.decode(code)).map { it.id }
        assertEquals(deckFromSeed, deckFromCode)
    }
}
