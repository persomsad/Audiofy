package com.audiofy.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.audiofy.app.ui.navigation.NavigationRoutes
import com.audiofy.app.ui.theme.AudiofyColors

/**
 * 底部导航栏
 * 参考设计: docs/prototype/ui.html - 底部导航栏
 * 
 * 4个Tab: 阅读、书架、有声书、我的（已移除"发现"Tab，专注个人化工具）
 */

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = NavigationRoutes.HOME,
        label = "阅读",
        icon = Icons.Outlined.MenuBook,
        selectedIcon = Icons.Filled.MenuBook
    ),
    BottomNavItem(
        route = NavigationRoutes.LIBRARY,
        label = "书架",
        icon = Icons.Outlined.Bookmarks,
        selectedIcon = Icons.Filled.Bookmarks
    ),
    BottomNavItem(
        route = NavigationRoutes.AUDIOBOOKS,
        label = "有声书",
        icon = Icons.Outlined.Headset,
        selectedIcon = Icons.Filled.Headset
    ),
    BottomNavItem(
        route = NavigationRoutes.PROFILE,
        label = "我的",
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person
    )
)

@Composable
fun AudiofyBottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(90.dp),
        containerColor = AudiofyColors.Neutral100,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AudiofyColors.Primary500,
                    selectedTextColor = AudiofyColors.Primary500,
                    unselectedIconColor = AudiofyColors.Neutral500,
                    unselectedTextColor = AudiofyColors.Neutral500,
                    indicatorColor = AudiofyColors.Primary100
                )
            )
        }
    }
}

