package com.globenews.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.globenews.viewmodel.GlobeViewModel

@Composable
fun GlobeNewsScreen(viewModel: GlobeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0a0a1a))) {
        // Globe WebView - fullscreen
        GlobeWebView(
            stories = uiState.visibleStories,
            flyToLat = uiState.flyToLat,
            flyToLng = uiState.flyToLng,
            flyToAltitude = uiState.flyToAltitude,
            onZoomChanged = { viewModel.onZoomChanged(it) },
            onStoryTapped = { viewModel.onStorySelected(it) },
            onFlyToConsumed = { viewModel.clearFlyTo() },
            modifier = Modifier.fillMaxSize()
        )

        // Search bar at the top
        TextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search location...", color = Color(0xFF888888)) },
            leadingIcon = {
                IconButton(onClick = {
                    viewModel.onSearch()
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF888888))
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.onSearch()
                focusManager.clearFocus()
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xCC1a1a2e),
                unfocusedContainerColor = Color(0xCC1a1a2e),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF64b5f6),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 40.dp)
                .align(Alignment.TopCenter)
        )

        // Loading indicator
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(color = Color(0xFF64b5f6))
        }

        // Refresh FAB
        FloatingActionButton(
            onClick = { viewModel.loadNews() },
            containerColor = Color(0xFF1e88e5),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
        }

        // Story card overlay
        StoryCard(
            story = uiState.selectedStory,
            onDismiss = { viewModel.dismissStory() },
            modifier = Modifier.fillMaxSize()
        )
    }
}
