# SET App

A modern, high-performance Android implementation of the classic **SET** card game, built from the ground up using **Jetpack Compose** and **Material 3**.
## 🚀 Key Features

*   **Dual Game Modes**:
    *   **Normal Mode**: The classic experience. Race against the clock to find as many sets as possible before the 81-card deck is exhausted.
    *   **Zen Mode**: An infinite, relaxed gameplay loop. The game never ends, and the deck automatically recycles cards to provide a seamless experience.
*   **Smart Deck Replenishment**: In Zen Mode, the app implements a "cool-down" logic. When only 18 cards remain in play, the deck is replenished with the full 81-card set, excluding the 18 cards currently on the table or in the buffer. This ensures **zero duplicate cards** ever appear on the table.
*   **Intelligent Table Management**: The game automatically detects when no sets are possible on the table and draws additional cards to ensure the game never soft-locks.
*   **Modern UI/UX**:
    *   **Immersive Pause Screen**: Features a custom-centered "Paused" state with a mode toggle anchored at the bottom for easy thumb access.
    *   **Visual State**: In Zen Mode, the cards remaining counter displays the infinity symbol (**∞**), and the timer is hidden to encourage relaxed play.
    *   **Haptic Feedback**: Tactile vibrations provide immediate feedback for incorrect set selections.
*   **Clean Architecture**: Built using MVVM patterns and reactive state management with Kotlin `StateFlow`.

## 🛠 Tech Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **State Management**: Kotlin Coroutines & StateFlow
*   **Build System**: Gradle Kotlin DSL (.kts)

## 🎮 How to Play

1.  **Objective**: Find a "Set" of three cards.
2.  **What defines a Set?**: For each of the four attributes (Color, Shape, Number, and Shading), the three cards must be **either all the same OR all different**.
3.  **Selection**: Tap three cards to check for a set.
4.  **Modes**: Open the Pause menu to toggle between **Normal** (Competitive) and **Zen** (Infinite) modes. Toggling modes will reset the board for a fresh start.

## 🛠 Setup & Installation

1.  Clone the repository.
2.  Open in **Android Studio**.
3.  Ensure you are using **JDK 11** or higher.
4.  Build and deploy to any device running **Android 7.0 (API 24)** or newer.

## 📄 License

Open-source project created for the love of SET. Feel free to fork and modify!
