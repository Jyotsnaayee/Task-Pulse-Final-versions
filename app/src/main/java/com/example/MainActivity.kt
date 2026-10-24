package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.TaskPulseMainScreen
import com.example.ui.theme.TaskPulseTheme
import com.example.viewmodel.TaskPulseViewModel

import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    private val viewModel: TaskPulseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            TaskPulseTheme(darkTheme = isDarkTheme) {
                TaskPulseMainScreen(viewModel = viewModel)
            }
        }
    }
}
