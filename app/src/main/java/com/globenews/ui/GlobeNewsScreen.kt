package com.globenews.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globenews.viewmodel.GlobeViewModel

@Composable
fun GlobeNewsScreen(viewModel: GlobeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0a0a1a))) {
        // Globe WebView - fullscreen
        GlobeWebView(
            stories = uiState.allStories,
            flyToLat = uiState.flyToLat,
            flyToLng = uiState.flyToLng,
            flyToAltitude = uiState.flyToAltitude,
            onZoomChanged = { viewModel.onZoomChanged(it) },
            onStoryTapped = { viewModel.onStorySelected(it) },
            onClusterTapped = { viewModel.onClusterTapped(it) },
            onGlobeReady = { viewModel.onGlobeReady() },
            onFlyToConsumed = { viewModel.clearFlyTo() },
            modifier = Modifier.fillMaxSize()
        )

        // Search bar at the top (only visible after globe loads)
        AnimatedVisibility(
            visible = uiState.globeReady,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
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
            )
        }

        // Refresh FAB (only visible after globe loads)
        AnimatedVisibility(
            visible = uiState.globeReady,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.loadNews() },
                containerColor = Color(0xFF1e88e5),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        // Story list bottom sheet overlay
        StoryListSheet(
            stories = uiState.sheetStories,
            onStoryClick = { viewModel.openArticle(it) },
            onDismiss = { viewModel.dismissSheet() },
            modifier = Modifier.fillMaxSize()
        )

        // In-app article browser
        if (uiState.articleUrl != null) {
            ArticleBrowser(
                url = uiState.articleUrl!!,
                title = uiState.articleTitle,
                onDismiss = { viewModel.closeArticle() },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Splash / loading overlay - covers everything until globe is ready
        AnimatedVisibility(
            visible = !uiState.globeReady,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(800)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0a0a1a)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "GlobeNews",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Explore the world's stories",
                        color = Color(0xFF64b5f6),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator(
                        color = Color(0xFF1e88e5),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading globe...",
                        color = Color(0xFF666666),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
