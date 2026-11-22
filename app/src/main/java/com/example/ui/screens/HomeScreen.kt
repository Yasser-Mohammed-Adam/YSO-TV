package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.VideoPlayerScreen
import com.example.viewmodel.TvViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TvViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val playingMedia by viewModel.playingMedia.collectAsState()
    val watchlistIds by viewModel.watchlistIds.collectAsState()

    if (playingMedia != null) {
        val mediaId = when (playingMedia) {
            is com.example.model.Channel -> (playingMedia as com.example.model.Channel).id
            is com.example.model.VodItem -> (playingMedia as com.example.model.VodItem).id
            else -> ""
        }
        val isFav = watchlistIds.contains(mediaId)

        VideoPlayerScreen(
            media = playingMedia!!,
            isFavorite = isFav,
            onToggleFavorite = { viewModel.toggleWatchlist(mediaId) },
            onBack = { viewModel.playMedia(null) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            0 -> "Global TV Elite"
                            1 -> "On-Demand Cinema"
                            2 -> "AI TV Concierge"
                            3 -> "My Watchlist"
                            else -> "Settings"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.Tv, contentDescription = "Live TV") },
                    label = { Text("Live TV") },
                    modifier = Modifier.testTag("nav_live_tv")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.Movie, contentDescription = "Movies") },
                    label = { Text("Movies") },
                    modifier = Modifier.testTag("nav_movies")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Guide") },
                    label = { Text("AI Guide") },
                    modifier = Modifier.testTag("nav_ai_guide")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Watchlist") },
                    label = { Text("Watchlist") },
                    modifier = Modifier.testTag("nav_watchlist")
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { viewModel.setTab(4) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                0 -> LiveTvScreen(
                    viewModel = viewModel,
                    onChannelClick = { channel -> viewModel.playMedia(channel) }
                )
                1 -> MoviesScreen(
                    viewModel = viewModel,
                    onVodClick = { vod -> viewModel.playMedia(vod) }
                )
                2 -> AiAdvisorScreen(viewModel = viewModel)
                3 -> WatchlistScreen(
                    viewModel = viewModel,
                    onChannelClick = { channel -> viewModel.playMedia(channel) },
                    onVodClick = { vod -> viewModel.playMedia(vod) }
                )
                4 -> SettingsScreen()
            }
        }
    }
}
