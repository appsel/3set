package com.example.setapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a SET card.
 * Each attribute is stored as 0, 1, or 2 to allow for the mathematical 
 * "remainder" set check (sum % 3 == 0) and minimize storage.
 */
@Parcelize
data class Card(
    val id: Int,
    val shape: Int,   // 0, 1, 2
    val color: Int,   // 0, 1, 2
    val shading: Int, // 0, 1, 2
    val number: Int   // 0, 1, 2
) : Parcelable
