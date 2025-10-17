package com.audiofy.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.audiofy.app.data.AppConfig
import com.audiofy.app.repository.createConfigRepository
import com.audiofy.app.service.TTSServiceImpl
import com.audiofy.app.ui.components.AudiofyBottomNavigationBar
import com.audiofy.app.ui.navigation.NavigationRoutes
import com.audiofy.app.ui.screens.*
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
        
        // 获取当前路由
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Global config (will be loaded from SettingsViewModel)
        val config = remember { mutableStateOf(AppConfig()) }

        // Create ConfigRepository once
        val configRepository = remember { createConfigRepository() }
        
        // 判断是否显示底部导航栏
        val showBottomBar = currentRoute in listOf(
            NavigationRoutes.HOME,
            NavigationRoutes.LIBRARY,
            NavigationRoutes.AUDIOBOOKS,
            NavigationRoutes.PROFILE
        )
        
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AudiofyBottomNavigationBar(
                        currentRoute = currentRoute ?: NavigationRoutes.HOME,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(NavigationRoutes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = NavigationRoutes.HOME,
                modifier = Modifier.padding(paddingValues)
            ) {
            // 主页
            composable(NavigationRoutes.HOME) {
                HomeScreen()
            }
            
            // 书架
            composable(NavigationRoutes.LIBRARY) {
                LibraryScreen(
                    onNavigateToPodcastDetail = { podcastId ->
                        navController.navigate("podcast_detail/$podcastId")
                    },
                    onNavigateToPlayer = { podcastId ->
                        navController.navigate("player/$podcastId")
                    }
                )
            }
            
            // 有声书
            composable(NavigationRoutes.AUDIOBOOKS) {
                AudiobooksScreen()
            }
            
            // 我的
            composable(NavigationRoutes.PROFILE) {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(NavigationRoutes.SETTINGS)
                    }
                )
            }
            
            // 创建播客（旧的InputScreen）
            composable(NavigationRoutes.CREATE_PODCAST) {
                val inputViewModel: InputViewModel = viewModel { InputViewModel() }

                InputScreen(
                    viewModel = inputViewModel,
                    onNavigateToSettings = {
                        navController.navigate(NavigationRoutes.SETTINGS)
                    },
                    onNavigateToProcessing = { inputText, title, coverUrl ->
                        navController.navigate(NavigationRoutes.PROCESSING) {
                            navController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("inputText", inputText)
                                set("title", title)
                                set("coverUrl", coverUrl)
                            }
                        }
                    }
                )
            }

            // 生成进度
            composable(NavigationRoutes.PROCESSING) { backStackEntry ->
                // Get data from previous screen's saved state
                val previousBackStackEntry = navController.previousBackStackEntry
                val inputText = previousBackStackEntry?.savedStateHandle?.get<String>("inputText") ?: ""
                val title = previousBackStackEntry?.savedStateHandle?.get<String>("title") ?: ""
                val coverUrl = previousBackStackEntry?.savedStateHandle?.get<String>("coverUrl") ?: ""

                val processingViewModel: ProcessingViewModel = viewModel {
                    ProcessingViewModel(
                        ttsService = TTSServiceImpl(),
                        config = config.value
                    )
                }

                ProcessingScreen(
                    viewModel = processingViewModel,
                    inputText = inputText,
                    title = title,
                    coverUrl = coverUrl,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToLibrary = {
                        navController.navigate(NavigationRoutes.LIBRARY) {
                            popUpTo(NavigationRoutes.HOME) { inclusive = false }
                        }
                    }
                )
            }

            // 设置
            composable(NavigationRoutes.SETTINGS) {
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
            
            // 播放器
            composable("player/{podcastId}") { backStackEntry ->
                val podcastId = backStackEntry.arguments?.getString("podcastId") ?: return@composable
                
                PlayerScreen(
                    podcastId = podcastId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            }
        }
    }
}

/**
 * Convert SettingsUiState to AppConfig
 */
private fun com.audiofy.app.viewmodel.SettingsUiState.toAppConfig(): AppConfig {
    return AppConfig(
        qwen3ApiKey = qwen3ApiKey,
        qwen3Voice = qwen3Voice,
        qwen3LanguageType = qwen3LanguageType
    )
}
