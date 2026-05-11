package com.example.threeSet.ui.game

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.threeSet.domain.logic.Deck
import com.example.threeSet.domain.logic.SetEvaluator
import com.example.threeSet.domain.model.Card
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(GameUiState(
        zenLifetimeScore = prefs.getInt("zen_lifetime_score", 0)
    ))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var deck: MutableList<Card> = mutableListOf()
    private var timerJob: Job? = null

    init {
        startNewGame()
    }

    fun startNewGame() {
        stopTimer()
        deck = Deck.generateShuffledDeck().toMutableList()
        val initialCards = mutableListOf<Card>()

        repeat(12) {
            if (deck.isNotEmpty()) {
                initialCards.add(deck.removeAt(0))
            }
        }

        val cardsWithSet = ensureSetOnTable(initialCards)

        _uiState.update { currentState ->
            currentState.copy(
                cardsOnTable = cardsWithSet,
                selectedCards = emptySet(),
                score = 0,
                cardsRemainingInDeck = deck.size,
                isGameOver = false,
                currentTimeSeconds = 0
            )
        }
        // Keep isPaused state as is to prevent auto-closing pause screen
        if (!_uiState.value.isPaused) {
            startTimer()
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(currentTimeSeconds = it.currentTimeSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
        if (_uiState.value.isPaused) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    fun toggleZenMode(enabled: Boolean) {
        _uiState.update { it.copy(isZenMode = enabled) }
        startNewGame()
    }

    fun onCardClicked(cardId: Int) {
        if (_uiState.value.isGameOver || _uiState.value.isPaused) return

        _uiState.update { currentState ->
            val newSelected = if (currentState.selectedCards.contains(cardId)) {
                currentState.selectedCards - cardId
            } else {
                currentState.selectedCards + cardId
            }

            if (newSelected.size == 3) {
                val selectedCardsList = currentState.cardsOnTable.filter { it.id in newSelected }
                if (selectedCardsList.size == 3 && SetEvaluator.isSet(
                        selectedCardsList[0],
                        selectedCardsList[1],
                        selectedCardsList[2]
                    )
                ) {
                    val newState = calculateNewStateAfterSet(currentState, newSelected)
                    if (newState.isGameOver) {
                        stopTimer()
                    }
                    newState
                } else {
                    // Not a set - deselect all and increment wrongSetTrigger
                    currentState.copy(
                        selectedCards = emptySet(),
                        wrongSetTrigger = currentState.wrongSetTrigger + 1
                    )
                }
            } else if (newSelected.size > 3) {
                currentState.copy(selectedCards = setOf(cardId))
            } else {
                currentState.copy(selectedCards = newSelected)
            }
        }
    }

    private fun replenishDeckIfNeeded(currentCardsOnTable: List<Card>) {
        if (!_uiState.value.isZenMode) return
        if (deck.size + currentCardsOnTable.size <= 18) {
            val allCards = Deck.generateFullDeck()
            val inPlayIds = (currentCardsOnTable.map { it.id } + deck.map { it.id }).toSet()
            val freshCards = allCards.filter { it.id !in inPlayIds }.shuffled()
            deck.addAll(freshCards)
        }
    }

    private fun calculateNewStateAfterSet(currentState: GameUiState, selectedIds: Set<Int>): GameUiState {
        val newCardsOnTable = currentState.cardsOnTable.toMutableList()
        val indicesToRemove = newCardsOnTable.indices.filter { newCardsOnTable[it].id in selectedIds }
        
        // Temporarily remove cards to check for replenishment
        val cardsAfterRemoval = newCardsOnTable.filter { it.id !in selectedIds }
        replenishDeckIfNeeded(cardsAfterRemoval)

        if (newCardsOnTable.size <= 12 && deck.isNotEmpty()) {
            indicesToRemove.forEach { index ->
                if (deck.isNotEmpty()) {
                    newCardsOnTable[index] = deck.removeAt(0)
                }
            }
        } else {
            indicesToRemove.sortedDescending().forEach { index ->
                newCardsOnTable.removeAt(index)
            }
        }

        val cardsWithSet = ensureSetOnTable(newCardsOnTable)
        val setsAvailable = hasSetOnTable(cardsWithSet)

        val newZenLifetimeScore = if (currentState.isZenMode) {
            val updated = currentState.zenLifetimeScore + 1
            prefs.edit { putInt("zen_lifetime_score", updated) }
            updated
        } else {
            currentState.zenLifetimeScore
        }

        return currentState.copy(
            cardsOnTable = cardsWithSet,
            selectedCards = emptySet(),
            score = currentState.score + 1,
            zenLifetimeScore = newZenLifetimeScore,
            cardsRemainingInDeck = deck.size,
            isGameOver = if (currentState.isZenMode) false else (deck.isEmpty() && !setsAvailable)
        )
    }

    private fun ensureSetOnTable(currentCards: List<Card>): List<Card> {
        val cards = currentCards.toMutableList()
        while (!hasSetOnTable(cards)) {
            if (_uiState.value.isZenMode) {
                replenishDeckIfNeeded(cards)
            }
            
            if (deck.isEmpty()) break

            repeat(3) {
                if (deck.isNotEmpty()) {
                    cards.add(deck.removeAt(0))
                }
            }
        }
        return cards
    }

    private fun hasSetOnTable(cards: List<Card>): Boolean {
        if (cards.size < 3) return false
        for (i in 0 until cards.size) {
            for (j in i + 1 until cards.size) {
                for (k in j + 1 until cards.size) {
                    if (SetEvaluator.isSet(cards[i], cards[j], cards[k])) return true
                }
            }
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}

data class GameUiState(
    val cardsOnTable: List<Card> = emptyList(),
    val selectedCards: Set<Int> = emptySet(),
    val score: Int = 0,
    val cardsRemainingInDeck: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val currentTimeSeconds: Long = 0,
    val wrongSetTrigger: Int = 0,
    val isZenMode: Boolean = false,
    val zenLifetimeScore: Int = 0
)
