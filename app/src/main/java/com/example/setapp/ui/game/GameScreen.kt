package com.example.threeSet.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.threeSet.ui.components.CardView
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Observe wrongSetTrigger to perform vibrations
    LaunchedEffect(uiState.wrongSetTrigger) {
        if (uiState.wrongSetTrigger > 0) {
            delay(50)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(127)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "SET",
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    actions = {
                        if (!uiState.isGameOver && !uiState.isPaused) {
                            IconButton(onClick = { viewModel.togglePause() }) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = uiState.cardsOnTable,
                            key = { card -> card.id }
                        ) { card ->
                            CardView(
                                card = card,
                                isSelected = uiState.selectedCards.contains(card.id),
                                onClick = { viewModel.onCardClicked(card.id) }
                            )
                        }
                    }

                    if (!uiState.isZenMode) {
                        val minutes = uiState.currentTimeSeconds / 60
                        val seconds = uiState.currentTimeSeconds % 60
                        val timeText = String.format(Locale.US, "%02d:%02d", minutes, seconds)
                        
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    val scoreText = if (uiState.isZenMode) {
                        "lifetime sets found: ${uiState.zenLifetimeScore}"
                    } else {
                        "sets found: ${uiState.score}"
                    }

                    Text(
                        text = scoreText,
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val remainingDisplay = if (uiState.isZenMode) "∞" else uiState.cardsRemainingInDeck.toString()
                    Text(
                        text = "cards remaining: $remainingDisplay",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Overlays outside Scaffold to cover the entire screen including TopBar
        if (uiState.isPaused) {
            PausedView(
                isZenMode = uiState.isZenMode,
                onZenModeToggle = { viewModel.toggleZenMode(it) },
                onDismiss = { viewModel.togglePause() }
            )
        }

        if (uiState.isGameOver) {
            GameOverView(
                score = uiState.score,
                finalTimeSeconds = uiState.currentTimeSeconds,
                onRestart = { viewModel.startNewGame() }
            )
        }
    }
}

@Composable
fun PausedView(
    isZenMode: Boolean,
    onZenModeToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        // Center text section
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "paused",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "tap anywhere to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Zen toggle section at the bottom center
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                // Consume clicks to prevent dismissing the pause screen when toggling
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Do nothing */ }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "normal",
                style = MaterialTheme.typography.bodyLarge,
                color = if (!isZenMode) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isZenMode,
                onCheckedChange = onZenModeToggle
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "zen",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isZenMode) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun GameOverView(
    score: Int,
    finalTimeSeconds: Long,
    onRestart: () -> Unit
) {
    val buttonColor = remember {
        listOf(
            Color(0xFFFF0101),
            Color(0xFF008002),
            Color(0xFF800080)
        ).random()
    }

    val minutes = finalTimeSeconds / 60
    val seconds = finalTimeSeconds % 60
    val finalTimeText = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "game over",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "sets found: $score",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "time: $finalTimeText",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White
            )
        ) {
            Text("play again")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PausedViewPreview() {
    PausedView(
        isZenMode = false,
        onZenModeToggle = {},
        onDismiss = {}
    )
}
