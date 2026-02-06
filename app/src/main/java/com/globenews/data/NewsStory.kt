package com.globenews.data

enum class StoryScope { LOCAL, NATIONAL, INTERNATIONAL }

data class NewsStory(
    val title: String,
    val source: String,
    val summary: String,
    val url: String,
    val latitude: Double,
    val longitude: Double,
    val scope: StoryScope,
    val id: String = "$latitude,$longitude,${title.hashCode()}"
)
