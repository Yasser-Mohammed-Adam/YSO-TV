package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AiUiState
import com.example.model.Channel
import com.example.model.VodItem
import com.google.firebase.Firebase
import com.google.firebase.ai.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "TvViewModel"

class TvViewModel(application: Application) : AndroidViewModel(application) {

    private val aiModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val _currentTab = MutableStateFlow(0) // 0: Live TV, 1: Movies, 2: AI Guide, 3: Watchlist, 4: Settings
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _playingMedia = MutableStateFlow<Any?>(null) // Can be Channel or VodItem
    val playingMedia: StateFlow<Any?> = _playingMedia.asStateFlow()

    private val _watchlistIds = MutableStateFlow<Set<String>>(emptySet())
    val watchlistIds: StateFlow<Set<String>> = _watchlistIds.asStateFlow()

    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    // Sample robust channels with public sample HLS/MP4 streams
    val channels = listOf(
        Channel(
            id = "c1",
            name = "Global News 24/7",
            category = "News",
            currentShow = "World Today Live: Global Election Special",
            viewers = "142K watching",
            logoUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=400",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ),
        Channel(
            id = "c2",
            name = "Elite Sports HD",
            category = "Sports",
            currentShow = "Champions League Final Replay & Analysis",
            viewers = "310K watching",
            logoUrl = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=400",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        ),
        Channel(
            id = "c3",
            name = "CineMax Premier",
            category = "Movies",
            currentShow = "Interstellar Odyssey (Sci-Fi Blockbuster)",
            viewers = "89K watching",
            logoUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=400",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        ),
        Channel(
            id = "c4",
            name = "Discovery Planet",
            category = "Documentary",
            currentShow = "Secrets of the Deep Amazon Rainforest",
            viewers = "54K watching",
            logoUrl = "https://images.unsplash.com/photo-1516026662326-177d85a1a6b4?w=400",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        ),
        Channel(
            id = "c5",
            name = "Kids Cartoon Hub",
            category = "Kids",
            currentShow = "Space Adventures: The Robot Quest",
            viewers = "78K watching",
            logoUrl = "https://images.unsplash.com/photo-1560169897-fc0cdbdfa4d5?w=400",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        ),
        Channel(
            id = "c6",
            name = "Melody Hits TV",
            category = "Music",
            currentShow = "Top 40 Global Countdown 2026",
            viewers = "95K watching",
            logoUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
        )
    )

    val vodItems = listOf(
        VodItem(
            id = "v1",
            title = "Cyberpunk Genesis",
            category = "Sci-Fi",
            duration = "2h 15m",
            rating = "4.8/5",
            posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500",
            description = "In a neon-drenched metropolis, a rogue hacker uncovers a corporate conspiracy that threatens the global consciousness.",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        ),
        VodItem(
            id = "v2",
            title = "Summit of Titans",
            category = "Documentary",
            duration = "1h 48m",
            rating = "4.9/5",
            posterUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=500",
            description = "Follow world-class mountaineers as they attempt the first winter ascent of K2 without supplemental oxygen.",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        ),
        VodItem(
            id = "v3",
            title = "Midnight Heist",
            category = "Action",
            duration = "1h 58m",
            rating = "4.6/5",
            posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500",
            description = "An elite crew of thieves assemble in Zurich for the most audacious digital currency vault robbery in history.",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ),
        VodItem(
            id = "v4",
            title = "Echoes of Silence",
            category = "Drama",
            duration = "2h 04m",
            rating = "4.7/5",
            posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500",
            description = "A heartwarming tale of a retired lighthouse keeper who discovers a mysterious encrypted radio signal from the past.",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        )
    )

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playMedia(media: Any?) {
        _playingMedia.value = media
    }

    fun toggleWatchlist(id: String) {
        val current = _watchlistIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _watchlistIds.value = current
    }

    fun askAiRecommendations(moodOrPrompt: String) {
        if (moodOrPrompt.isBlank()) return
        viewModelScope.launch {
            _aiState.value = AiUiState.Loading
            try {
                val prompt = """
                    You are Global TV Elite's AI Concierge. The user is looking for TV channels or movies with this request/mood: "$moodOrPrompt".
                    Recommend 3 tailored picks from our catalog (such as Global News 24/7, Elite Sports HD, CineMax Premier, Discovery Planet, Kids Cartoon Hub, Melody Hits TV, Cyberpunk Genesis, Summit of Titans, Midnight Heist, Echoes of Silence).
                    Format your response cleanly with enthusiasm, detailing why each pick matches their mood, suggested watch time, and genre tags.
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    aiModel.generateContent(prompt)
                }
                val text = response.text
                if (!text.isNullOrBlank()) {
                    _aiState.value = AiUiState.Success(text)
                } else {
                    _aiState.value = AiUiState.Error("AI returned an empty recommendation.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI request timed out", e)
                _aiState.value = AiUiState.Error("AI request timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI generation failed", e)
                _aiState.value = AiUiState.Error(e.localizedMessage ?: "Failed to generate AI recommendations.")
            }
        }
    }
}
