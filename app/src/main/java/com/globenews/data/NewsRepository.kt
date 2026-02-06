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

    // Map well-known news sources to approximate locations
    private val sourceLocations = mapOf(
        "bbc" to Pair(51.5074, -0.1278),
        "cnn" to Pair(33.7490, -84.3880),
        "reuters" to Pair(51.5074, -0.1278),
        "associated press" to Pair(40.7128, -74.0060),
        "ap news" to Pair(40.7128, -74.0060),
        "al jazeera" to Pair(25.2854, 51.5310),
        "the guardian" to Pair(51.5074, -0.1278),
        "nyt" to Pair(40.7128, -74.0060),
        "new york times" to Pair(40.7128, -74.0060),
        "washington post" to Pair(38.9072, -77.0369),
        "fox" to Pair(40.7128, -74.0060),
        "nbc" to Pair(40.7128, -74.0060),
        "abc news" to Pair(40.7128, -74.0060),
        "cbs" to Pair(40.7128, -74.0060),
        "cnbc" to Pair(40.7128, -74.0060),
        "bloomberg" to Pair(40.7128, -74.0060),
        "financial times" to Pair(51.5074, -0.1278),
        "the economist" to Pair(51.5074, -0.1278),
        "sky news" to Pair(51.5074, -0.1278),
        "deutsche welle" to Pair(52.5200, 13.4050),
        "dw" to Pair(52.5200, 13.4050),
        "france24" to Pair(48.8566, 2.3522),
        "france 24" to Pair(48.8566, 2.3522),
        "times of india" to Pair(19.0760, 72.8777),
        "ndtv" to Pair(28.6139, 77.2090),
        "hindustan times" to Pair(28.6139, 77.2090),
        "xinhua" to Pair(39.9042, 116.4074),
        "south china morning post" to Pair(22.3193, 114.1694),
        "nhk" to Pair(35.6762, 139.6503),
        "japan times" to Pair(35.6762, 139.6503),
        "yonhap" to Pair(37.5665, 126.9780),
        "korea herald" to Pair(37.5665, 126.9780),
        "abc australia" to Pair(-33.8688, 151.2093),
        "sydney morning herald" to Pair(-33.8688, 151.2093),
        "cbc" to Pair(43.6532, -79.3832),
        "globe and mail" to Pair(43.6532, -79.3832),
        "rt" to Pair(55.7558, 37.6173),
        "tass" to Pair(55.7558, 37.6173),
    )

    // Spread cities for API stories that don't match known sources
    private val fallbackCities = listOf(
        Pair(40.7128, -74.0060),   // New York
        Pair(51.5074, -0.1278),    // London
        Pair(35.6762, 139.6503),   // Tokyo
        Pair(48.8566, 2.3522),     // Paris
        Pair(-33.8688, 151.2093),  // Sydney
        Pair(52.5200, 13.4050),    // Berlin
        Pair(19.0760, 72.8777),    // Mumbai
        Pair(37.5665, 126.9780),   // Seoul
        Pair(-22.9068, -43.1729),  // Rio
        Pair(55.7558, 37.6173),    // Moscow
        Pair(1.3521, 103.8198),    // Singapore
        Pair(25.2048, 55.2708),    // Dubai
        Pair(-1.2921, 36.8219),    // Nairobi
        Pair(19.4326, -99.1332),   // Mexico City
        Pair(41.0082, 28.9784),    // Istanbul
    )

    suspend fun fetchNews(): List<NewsStory> {
        // Always load fallback stories (they have proper geo coordinates)
        val fallback = loadFallbackNews()

        val apiStories = try {
            fetchFromApi()
        } catch (e: Exception) {
            emptyList()
        }

        return if (apiStories.isNotEmpty()) {
            // Merge: API stories (with assigned locations) + fallback for coverage
            apiStories + fallback
        } else {
            fallback
        }
    }

    private suspend fun fetchFromApi(): List<NewsStory> = withContext(Dispatchers.IO) {
        val url = "https://gnews.io/api/v4/top-headlines?category=general&lang=en&max=10&apikey=$apiKey"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext emptyList()

        val body = response.body?.string() ?: return@withContext emptyList()
        val json = JSONObject(body)
        val articles = json.optJSONArray("articles") ?: return@withContext emptyList()

        val stories = mutableListOf<NewsStory>()
        for (i in 0 until articles.length()) {
            val article = articles.getJSONObject(i)
            val sourceName = article.optJSONObject("source")?.optString("name", "Unknown") ?: "Unknown"
            val coords = guessLocation(sourceName, i)
            stories.add(
                NewsStory(
                    title = article.optString("title", ""),
                    source = sourceName,
                    summary = article.optString("description", ""),
                    url = article.optString("url", ""),
                    latitude = coords.first,
                    longitude = coords.second,
                    scope = StoryScope.INTERNATIONAL
                )
            )
        }
        stories
    }

    private fun guessLocation(sourceName: String, index: Int): Pair<Double, Double> {
        val lower = sourceName.lowercase()
        for ((key, coords) in sourceLocations) {
            if (lower.contains(key)) return coords
        }
        // Spread unknown sources across world cities
        return fallbackCities[index % fallbackCities.size]
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
