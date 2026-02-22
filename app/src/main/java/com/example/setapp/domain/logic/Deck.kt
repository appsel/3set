package com.example.setapp.domain.logic

import com.example.setapp.domain.model.Card

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
}