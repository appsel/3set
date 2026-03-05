package com.example.setapp.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.setapp.domain.logic.Deck
import com.example.setapp.domain.logic.SetEvaluator
import com.example.setapp.domain.model.Card
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var deck: MutableList<Card> = savedStateHandle.get<List<Card>>(KEY_DECK)?.toMutableList() ?: mutableListOf()
    private var timerJob: Job? = null

    init {
        if (_uiState.value.cardsOnTable.isEmpty() && !_uiState.value.isGameOver) {
            startNewGame()
        } else if (!_uiState.value.isPaused && !_uiState.value.isGameOver) {
            startTimer()
        }
    }

    private fun loadInitialState(): GameUiState {
        return GameUiState(
            cardsOnTable = savedStateHandle[KEY_CARDS_ON_TABLE] ?: emptyList(),
            selectedCards = savedStateHandle[KEY_SELECTED_CARDS] ?: emptySet(),
            score = savedStateHandle[KEY_SCORE] ?: 0,
            cardsRemainingInDeck = savedStateHandle[KEY_REMAINING] ?: 0,
            isGameOver = savedStateHandle[KEY_GAME_OVER] ?: false,
            isPaused = savedStateHandle[KEY_PAUSED] ?: false,
            currentTimeSeconds = savedStateHandle[KEY_TIME] ?: 0
        )
    }

    private fun saveState() {
        savedStateHandle[KEY_CARDS_ON_TABLE] = _uiState.value.cardsOnTable
        savedStateHandle[KEY_SELECTED_CARDS] = _uiState.value.selectedCards
        savedStateHandle[KEY_SCORE] = _uiState.value.score
        savedStateHandle[KEY_REMAINING] = _uiState.value.cardsRemainingInDeck
        savedStateHandle[KEY_GAME_OVER] = _uiState.value.isGameOver
        savedStateHandle[KEY_PAUSED] = _uiState.value.isPaused
        savedStateHandle[KEY_TIME] = _uiState.value.currentTimeSeconds
        savedStateHandle[KEY_DECK] = deck.toList()
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

        _uiState.value = GameUiState(
            cardsOnTable = cardsWithSet,
            cardsRemainingInDeck = deck.size,
            currentTimeSeconds = 0
        )
        saveState()
        startTimer()
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(currentTimeSeconds = it.currentTimeSeconds + 1) }
                saveState()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
        saveState()
        if (_uiState.value.isPaused) {
            stopTimer()
        } else {
            startTimer()
        }
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
        saveState()
    }

    private fun calculateNewStateAfterSet(currentState: GameUiState, selectedIds: Set<Int>): GameUiState {
        val newCardsOnTable = currentState.cardsOnTable.toMutableList()
        val indicesToRemove = newCardsOnTable.indices.filter { newCardsOnTable[it].id in selectedIds }
        
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

        return currentState.copy(
            cardsOnTable = cardsWithSet,
            selectedCards = emptySet(),
            score = currentState.score + 1,
            cardsRemainingInDeck = deck.size,
            isGameOver = deck.isEmpty() && !setsAvailable
        )
    }

    private fun ensureSetOnTable(currentCards: List<Card>): List<Card> {
        val cards = currentCards.toMutableList()
        while (!hasSetOnTable(cards) && deck.isNotEmpty()) {
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

    companion object {
        private const val KEY_CARDS_ON_TABLE = "cards_on_table"
        private const val KEY_SELECTED_CARDS = "selected_cards"
        private const val KEY_SCORE = "score"
        private const val KEY_REMAINING = "remaining"
        private const val KEY_GAME_OVER = "game_over"
        private const val KEY_PAUSED = "paused"
        private const val KEY_TIME = "time"
        private const val KEY_DECK = "deck"
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
    val wrongSetTrigger: Int = 0
)
