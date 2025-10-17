package com.audiofy.app

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.audiofy.app.data.AppConfig
import com.audiofy.app.repository.createConfigRepository
import com.audiofy.app.service.GeminiServiceImpl
import com.audiofy.app.service.TTSServiceImpl
import com.audiofy.app.ui.screens.InputScreen
import com.audiofy.app.ui.screens.ProcessingScreen
import com.audiofy.app.ui.screens.SettingsScreen
import com.audiofy.app.ui.theme.AudiofyTheme
import com.audiofy.app.viewmodel.InputViewModel
import com.audiofy.app.viewmodel.ProcessingViewModel
import com.audiofy.app.viewmodel.SettingsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Main App Entry Point with Navigation
 */
@Composable
@Preview
fun App() {
    AudiofyTheme {
        val navController = rememberNavController()

        // Global config (will be loaded from SettingsViewModel)
        val config = remember { mutableStateOf(AppConfig()) }

        // Create ConfigRepository once
        val configRepository = remember { createConfigRepository() }

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            // Home Screen - Input Interface
            composable("home") {
                val inputViewModel: InputViewModel = viewModel { InputViewModel() }

                InputScreen(
                    viewModel = inputViewModel,
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToProcessing = { inputText ->
                        // Pass input text as navigation argument
                        navController.navigate("processing") {
                            // Store input text in saved state
                            navController.currentBackStackEntry?.savedStateHandle?.set("inputText", inputText)
                        }
                    }
                )
            }

            // Processing Screen - Translation + TTS
            composable("processing") { backStackEntry ->
                // Get input text from previous screen's saved state
                val previousBackStackEntry = navController.previousBackStackEntry
                val inputText = previousBackStackEntry?.savedStateHandle?.get<String>("inputText") ?: ""

                val processingViewModel: ProcessingViewModel = viewModel {
                    ProcessingViewModel(
                        geminiService = GeminiServiceImpl(),
                        ttsService = TTSServiceImpl(),
                        config = config.value
                    )
                }

                ProcessingScreen(
                    viewModel = processingViewModel,
                    inputText = inputText,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Settings Screen
            composable("settings") {
                val settingsViewModel: SettingsViewModel = viewModel {
                    SettingsViewModel(configRepository)
                }

                // Update global config when settings change
                LaunchedEffect(Unit) {
                    settingsViewModel.uiState.collect { uiState ->
                        config.value = uiState.toAppConfig()
                    }
                }

                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Convert SettingsUiState to AppConfig
 */
private fun com.audiofy.app.viewmodel.SettingsUiState.toAppConfig(): AppConfig {
    return AppConfig(
        geminiApiKey = geminiApiKey,
        geminiBaseUrl = geminiBaseUrl,
        geminiModelId = geminiModelId,
        qwen3ApiKey = qwen3ApiKey,
        qwen3Voice = qwen3Voice,
        qwen3LanguageType = qwen3LanguageType
    )
}
