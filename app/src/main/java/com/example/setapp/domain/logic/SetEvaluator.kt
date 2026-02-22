package com.example.setapp.domain.logic

import com.example.setapp.domain.model.Card

object SetEvaluator {

    /**
     * Checks if three cards form a 'Set'.
     * Using the property that for each attribute, the sum of values (0, 1, 2)
     * must be divisible by 3 to be a set (all same or all different).
     */
    fun isSet(c1: Card, c2: Card, c3: Card): Boolean {
        return (c1.shape + c2.shape + c3.shape) % 3 == 0 &&
               (c1.color + c2.color + c3.color) % 3 == 0 &&
               (c1.shading + c2.shading + c3.shading) % 3 == 0 &&
               (c1.number + c2.number + c3.number) % 3 == 0
    }
}
