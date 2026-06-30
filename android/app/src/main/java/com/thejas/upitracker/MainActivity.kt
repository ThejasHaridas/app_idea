package com.thejas.upitracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thejas.upitracker.navigation.NavGraph
import com.thejas.upitracker.navigation.SCREENS
import com.thejas.upitracker.ui.theme.UPITrackerTheme
import com.thejas.upitracker.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions granted or denied — app works either way for manual entry */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSMSPermissions()

        setContent {
            UPITrackerTheme {
                val navController = rememberNavController()
                val currentEntry by navController.currentBackStackEntryAsState()

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            SCREENS.forEach { screen ->
                                val selected = currentEntry?.destination?.route == screen.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Text(screen.icon, fontSize = 20.sp, textAlign = TextAlign.Center) },
                                    label = { Text(screen.label, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        NavGraph(navController, viewModel)
                    }
                }
            }
        }
    }

    private fun requestSMSPermissions() {
        val perms = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        if (perms.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            permissionLauncher.launch(perms)
        }
    }
}
