package com.globenews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globenews.ui.GlobeNewsScreen
import com.globenews.ui.GlobeNewsTheme
import com.globenews.viewmodel.GlobeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlobeNewsTheme {
                val viewModel: GlobeViewModel = viewModel()
                GlobeNewsScreen(viewModel = viewModel)
            }
        }
    }
}
