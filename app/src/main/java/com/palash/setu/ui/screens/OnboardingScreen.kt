package com.palash.setu.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.palash.setu.util.LocalPreferences
import com.palash.setu.data.dao.UserDao
import com.palash.setu.data.entity.UserEntity
import java.util.UUID
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(preferences: LocalPreferences, userDao: UserDao, onComplete: () -> Unit) {
    var district by remember { mutableStateOf("") }
    var block by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Santhali") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("PALASH-Setu", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Text("Set up your classroom", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Text("Choose a language for your offline teaching assistant.")
        OutlinedTextField(district, { district = it }, Modifier.fillMaxWidth(), label = { Text("District") }, singleLine = true)
        OutlinedTextField(block, { block = it }, Modifier.fillMaxWidth(), label = { Text("Block") }, singleLine = true)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = language,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                label = { Text("Target language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("Santhali", "Ho", "Mundari").forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { language = option; expanded = false })
                }
            }
        }
        Button(
            onClick = {
                val teacherId = UUID.randomUUID().toString()
                preferences.saveTeacherSetup(teacherId, district, block, language)
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    userDao.insert(UserEntity(teacherId, "LOCAL", "Teacher", district.trim(), block.trim(), language))
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onComplete() }
                }
            },
            enabled = district.isNotBlank() && block.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Continue offline") }
    }
}
