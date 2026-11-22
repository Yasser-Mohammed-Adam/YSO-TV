package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.Channel
import com.example.model.VodItem
import com.example.viewmodel.TvViewModel

@Composable
fun WatchlistScreen(
    viewModel: TvViewModel,
    onChannelClick: (Channel) -> Unit,
    onVodClick: (VodItem) -> Unit
) {
    val watchlistIds by viewModel.watchlistIds.collectAsState()
    val favoriteChannels = viewModel.channels.filter { watchlistIds.contains(it.id) }
    val favoriteVod = viewModel.vodItems.filter { watchlistIds.contains(it.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Your Watchlist & Favorites",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (favoriteChannels.isEmpty() && favoriteVod.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved channels or movies yet. Tap the favorite heart icon while watching to add items here!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (favoriteChannels.isNotEmpty()) {
                    item {
                        Text(
                            text = "Live TV Channels",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(favoriteChannels) { channel ->
                        ChannelCard(channel = channel, onClick = { onChannelClick(channel) })
                    }
                }

                if (favoriteVod.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "On-Demand Movies",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(favoriteVod) { vod ->
                        VodCard(vod = vod, onClick = { onVodClick(vod) })
                    }
                }
            }
        }
    }
}
