package com.example.threeSet.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.threeSet.domain.logic.DeckCode
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
    var pendingDeckCode by remember { mutableStateOf<String?>(null) }

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
        if (pendingDeckCode != null) {
            CodeReadyView(
                code = pendingDeckCode!!,
                onStart = {
                    viewModel.startGameWithCode(pendingDeckCode!!)
                    pendingDeckCode = null
                }
            )
        } else if (uiState.isPaused) {
            PausedView(
                isZenMode = uiState.isZenMode,
                onZenModeToggle = { viewModel.toggleZenMode(it) },
                onDismiss = { viewModel.togglePause() },
                onCodeConfirmed = { code ->
                    if (DeckCode.decodeOrNull(code) != null) {
                        pendingDeckCode = code
                        true
                    } else {
                        false
                    }
                }
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
    onDismiss: () -> Unit,
    onCodeConfirmed: (String) -> Boolean
) {
    var showCodeDialog by remember { mutableStateOf(false) }

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

        // Drawn last so it sits above the dismiss overlay and stays tappable
        Text(
            text = "custom",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 16.dp, top = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showCodeDialog = true }
                )
        )
    }

    if (showCodeDialog) {
        DeckCodeDialog(
            onDismiss = { showCodeDialog = false },
            onConfirm = { code ->
                val success = onCodeConfirmed(code)
                if (success) showCodeDialog = false
                success
            }
        )
    }
}

@Composable
fun CodeReadyView(
    code: String,
    onStart: () -> Unit
) {
    val codeTextStyle = MaterialTheme.typography.displayLarge.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    )
    // Fixed slot per character so glyph shape (e.g. "1" vs "A") doesn't change spacing
    val charSlotWidth = 36.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onStart
            )
    ) {
        // Room code stays fixed at the screen center
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            code.forEach { char ->
                Box(
                    modifier = Modifier.width(charSlotWidth),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char.toString(),
                        style = codeTextStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        // Closer to the code without moving the code itself
        Text(
            text = "code:",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-40).dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "tap to start",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (48).dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun DeckCodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Boolean
) {
    var codeInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp
                )
            ) {
                Text(
                    text = "deck code",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "enter a 6 character code (0-9, A-Z)",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                var isInputFocused by remember { mutableStateOf(false) }
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = codeInput,
                            onValueChange = { input ->
                                codeInput = input
                                    .uppercase()
                                    .filter { it in '0'..'9' || it in 'A'..'Z' }
                                    .take(DeckCode.CODE_LENGTH)
                                showError = false
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .onFocusChanged { isInputFocused = it.isFocused },
                            decorationBox = { innerTextField ->
                                Box {
                                    if (codeInput.isEmpty()) {
                                        Text(
                                            text = "ABC123",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        IconButton(
                            onClick = {
                                codeInput = DeckCode.generateRandom()
                                showError = false
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Casino,
                                contentDescription = "Generate code"
                            )
                        }
                    }
                    HorizontalDivider(
                        thickness = if (isInputFocused || showError) 2.dp else 1.dp,
                        color = when {
                            showError -> MaterialTheme.colorScheme.error
                            isInputFocused -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    if (showError) {
                        Text(
                            text = "Invalid code — must be exactly 6 Base36 characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("cancel")
                    }
                    TextButton(
                        onClick = {
                            if (codeInput.length == DeckCode.CODE_LENGTH) {
                                if (!onConfirm(codeInput)) {
                                    showError = true
                                }
                            } else {
                                showError = true
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("play")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
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
        onDismiss = {},
        onCodeConfirmed = { false }
    )
}
