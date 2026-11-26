package com.placefinder.directory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.placefinder.directory.ui.navigation.MainScreen
import com.placefinder.directory.ui.theme.LocalPlaceFinderTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalPlaceFinderTheme {
                MainScreen()
            }
        }
    }
}
