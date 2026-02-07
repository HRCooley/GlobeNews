package com.globenews.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.globenews.data.GeocodingHelper
import com.globenews.data.NewsRepository
import com.globenews.data.NewsStory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GlobeUiState(
    val allStories: List<NewsStory> = emptyList(),
    val sheetStories: List<NewsStory> = emptyList(),
    val articleUrl: String? = null,
    val articleTitle: String = "",
    val isLoading: Boolean = true,
    val globeReady: Boolean = false,
    val searchQuery: String = "",
    val searchNotFound: Boolean = false,
    val flyToLat: Double? = null,
    val flyToLng: Double? = null,
    val flyToAltitude: Double? = null
)

class GlobeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NewsRepository(application)
    private val _uiState = MutableStateFlow(GlobeUiState())
    val uiState: StateFlow<GlobeUiState> = _uiState.asStateFlow()

    init {
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val stories = repository.fetchNews()
            _uiState.value = _uiState.value.copy(
                allStories = stories,
                isLoading = false
            )
        }
    }

    fun onZoomChanged(altitude: Double) {
        // Zoom tracking available for future use
    }

    fun onStorySelected(storyId: String) {
        val story = _uiState.value.allStories.find { it.id == storyId }
        if (story != null) {
            _uiState.value = _uiState.value.copy(sheetStories = listOf(story))
        }
    }

    fun onClusterTapped(storyIds: List<String>) {
        val stories = storyIds.mapNotNull { id ->
            _uiState.value.allStories.find { it.id == id }
        }
        if (stories.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(sheetStories = stories)
        }
    }

    fun dismissSheet() {
        _uiState.value = _uiState.value.copy(sheetStories = emptyList())
    }

    fun openArticle(story: NewsStory) {
        if (story.url.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                articleUrl = story.url,
                articleTitle = story.title,
                sheetStories = emptyList()
            )
        }
    }

    fun closeArticle() {
        _uiState.value = _uiState.value.copy(articleUrl = null, articleTitle = "")
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, searchNotFound = false)
    }

    fun onSearch() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return
        val location = GeocodingHelper.search(query)
        if (location != null) {
            _uiState.value = _uiState.value.copy(
                flyToLat = location.lat,
                flyToLng = location.lng,
                flyToAltitude = 0.4,
                searchNotFound = false
            )
        } else {
            _uiState.value = _uiState.value.copy(searchNotFound = true)
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", searchNotFound = false)
    }

    fun clearFlyTo() {
        _uiState.value = _uiState.value.copy(
            flyToLat = null,
            flyToLng = null,
            flyToAltitude = null
        )
    }

    fun onGlobeReady() {
        _uiState.value = _uiState.value.copy(globeReady = true)
    }
}
