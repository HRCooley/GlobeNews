package com.globenews.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.globenews.data.NewsStory
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GlobeWebView(
    stories: List<NewsStory>,
    flyToLat: Double?,
    flyToLng: Double?,
    flyToAltitude: Double?,
    onZoomChanged: (Double) -> Unit,
    onStoryTapped: (String) -> Unit,
    onFlyToConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            setBackgroundColor(0xFF0a0a1a.toInt())
        }
    }

    val bridge = remember {
        object {
            @JavascriptInterface
            fun onZoomChanged(altitude: String) {
                try {
                    onZoomChanged(altitude.toDouble())
                } catch (_: Exception) {}
            }

            @JavascriptInterface
            fun onMarkerTapped(storyId: String) {
                onStoryTapped(storyId)
            }
        }
    }

    DisposableEffect(Unit) {
        webView.addJavascriptInterface(bridge, "Android")
        webView.loadUrl("file:///android_asset/globe.html")
        onDispose {
            webView.removeJavascriptInterface("Android")
            webView.destroy()
        }
    }

    // Update stories whenever they change
    LaunchedEffect(stories) {
        if (stories.isNotEmpty()) {
            val jsonArray = JSONArray()
            for (story in stories) {
                val obj = JSONObject()
                obj.put("id", story.id)
                obj.put("title", story.title)
                obj.put("source", story.source)
                obj.put("lat", story.latitude)
                obj.put("lng", story.longitude)
                obj.put("scope", story.scope.name)
                jsonArray.put(obj)
            }
            val escaped = jsonArray.toString().replace("\\", "\\\\").replace("'", "\\'")
            webView.post {
                webView.evaluateJavascript("updateMarkers('$escaped')", null)
            }
        }
    }

    // Fly to location
    LaunchedEffect(flyToLat, flyToLng, flyToAltitude) {
        if (flyToLat != null && flyToLng != null && flyToAltitude != null) {
            webView.post {
                webView.evaluateJavascript(
                    "flyTo($flyToLat, $flyToLng, $flyToAltitude)",
                    null
                )
            }
            onFlyToConsumed()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
