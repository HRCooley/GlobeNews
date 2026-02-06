package com.globenews.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NewsRepository(private val context: Context) {

    private val apiKey = "0689e03db8a6390e29c7b52175b6c850"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchNews(): List<NewsStory> {
        return try {
            val stories = fetchFromApi()
            if (stories.isNotEmpty()) stories else loadFallbackNews()
        } catch (e: Exception) {
            loadFallbackNews()
        }
    }

    private suspend fun fetchFromApi(): List<NewsStory> = withContext(Dispatchers.IO) {
        val url = "https://gnews.io/api/v4/top-headlines?category=general&lang=en&max=50&apikey=$apiKey"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext emptyList()

        val body = response.body?.string() ?: return@withContext emptyList()
        val json = JSONObject(body)
        val articles = json.optJSONArray("articles") ?: return@withContext emptyList()

        val stories = mutableListOf<NewsStory>()
        for (i in 0 until articles.length()) {
            val article = articles.getJSONObject(i)
            // GNews doesn't always provide geo data, so we skip articles without it
            // In a real app you'd geocode the source location
            stories.add(
                NewsStory(
                    title = article.optString("title", ""),
                    source = article.optJSONObject("source")?.optString("name", "Unknown") ?: "Unknown",
                    summary = article.optString("description", ""),
                    url = article.optString("url", ""),
                    latitude = 0.0,
                    longitude = 0.0,
                    scope = StoryScope.INTERNATIONAL
                )
            )
        }
        stories
    }

    fun loadFallbackNews(): List<NewsStory> {
        val json = context.assets.open("fallback_news.json").bufferedReader().use { it.readText() }
        return parseNewsJson(json)
    }

    companion object {
        fun parseNewsJson(json: String): List<NewsStory> {
            val array = JSONArray(json)
            val stories = mutableListOf<NewsStory>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                stories.add(
                    NewsStory(
                        title = obj.getString("title"),
                        source = obj.getString("source"),
                        summary = obj.getString("summary"),
                        url = obj.getString("url"),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        scope = StoryScope.valueOf(obj.getString("scope").uppercase())
                    )
                )
            }
            return stories
        }
    }
}
