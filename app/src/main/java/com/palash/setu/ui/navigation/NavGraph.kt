package com.palash.setu.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.palash.setu.data.dao.FLNDictionaryDao
import com.palash.setu.data.dao.GeneratedWorksheetDao
import com.palash.setu.data.dao.UserDao
import com.palash.setu.ui.screens.LiveTranslatorScreen
import com.palash.setu.ui.screens.OnboardingScreen
import com.palash.setu.ui.screens.FLNScriptsScreen
import com.palash.setu.ui.screens.WorksheetScreen
import com.palash.setu.util.LocalPreferences

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun PalashNavGraph(preferences: LocalPreferences, dictionaryDao: FLNDictionaryDao, worksheetDao: GeneratedWorksheetDao, userDao: UserDao) {
    val navController = rememberNavController()
    val start = if (preferences.isOnboarded) "live-translator" else "onboarding"
    val tabs = listOf(
        Tab("live-translator", "Translate", Icons.Default.Translate),
        Tab("fln-scripts", "FLN Scripts", Icons.Default.Book),
        Tab("worksheet-generator", "Worksheets", Icons.Default.Description)
    )
    NavHost(navController, startDestination = start) {
        composable("onboarding") { OnboardingScreen(preferences, userDao) { navController.navigate("live-translator") { popUpTo("onboarding") { inclusive = true } } } }
        composable("live-translator") { MainTabLayout(navController, tabs) { LiveTranslatorScreen(preferences.targetLanguage, dictionaryDao) } }
        composable("fln-scripts") { MainTabLayout(navController, tabs) { FLNScriptsScreen(preferences.targetLanguage) } }
        composable("worksheet-generator") { MainTabLayout(navController, tabs) { WorksheetScreen(preferences.teacherId, preferences.targetLanguage, worksheetDao) } }
    }
}

@Composable
private fun MainTabLayout(navController: NavHostController, tabs: List<Tab>, content: @Composable () -> Unit) {
    Scaffold(bottomBar = {
        NavigationBar {
            val current = navController.currentBackStackEntryAsState().value?.destination?.route
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = current == tab.route,
                    onClick = { navController.navigate(tab.route) { launchSingleTop = true } },
                    icon = { Icon(tab.icon, tab.label) },
                    label = { Text(tab.label) }
                )
            }
        }
    }) { padding -> androidx.compose.foundation.layout.Box(Modifier.padding(padding)) { content() } }
}

@Composable
private fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(Modifier.padding(24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}