package com.example.threeSet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.threeSet.ui.game.GameScreen
import com.example.threeSet.ui.theme.SETAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (throwable is IllegalArgumentException &&
                throwable.message?.contains("Activity client record must not be null") == true) {
                // Known Android OS bug, safe to ignore
                return@setDefaultUncaughtExceptionHandler
            }
            // Re-throw everything else
            throw throwable
        }
        enableEdgeToEdge()
        setContent {
            SETAppTheme {
                GameScreen()
            }
        }
    }

}
