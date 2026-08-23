package com.example.threeSet.domain.logic

import com.example.threeSet.domain.model.Card

object Deck {
    fun generateFullDeck(): List<Card> {
        val deck = mutableListOf<Card>()
        var id = 0
        for (shape in 0..2) {
            for (color in 0..2) {
                for (shading in 0..2) {
                    for (number in 0..2) {
                        deck.add(Card(id++, shape, color, shading, number))
                    }
                }
            }
        }
        return deck
    }

    fun generateShuffledDeck(): List<Card> {
        return generateFullDeck().shuffled()
    }

    fun generateShuffledDeck(seed: Long): List<Card> {
        val deck = generateFullDeck().toMutableList()
        val random = java.util.Random(seed)
        for (i in deck.lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            deck[i] = deck[j].also { deck[j] = deck[i] }
        }
        return deck
    }
}