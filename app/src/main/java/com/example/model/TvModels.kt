package com.example.model

data class Channel(
    val id: String,
    val name: String,
    val category: String,
    val currentShow: String,
    val viewers: String,
    val logoUrl: String,
    val streamUrl: String,
    val isLive: Boolean = true
)

data class VodItem(
    val id: String,
    val title: String,
    val category: String,
    val duration: String,
    val rating: String,
    val posterUrl: String,
    val description: String,
    val streamUrl: String
)

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Success(val recommendationsText: String) : AiUiState
    data class Error(val message: String) : AiUiState
}
