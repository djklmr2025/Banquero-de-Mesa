package com.example

import androidx.compose.ui.graphics.Color

data class Player(
    val id: String,
    val name: String,
    val balance: Long,
    val color: Int, // Hex value of Color
    val avatarEmoji: String
) {
    fun getComposeColor(): Color = Color(color)
}

data class GameTransaction(
    val id: String,
    val timestamp: String,
    val fromPlayerId: String?, // null represents Bank
    val fromPlayerName: String,
    val toPlayerId: String?, // null represents Bank
    val toPlayerName: String,
    val amount: Long,
    val concept: String,
    val isAiProcessed: Boolean = false
)

data class BillTemplate(
    val id: String,
    val denomination: Long,
    val colorHex: Int,
    val iconName: String, // "beach", "pyramid", "airplane", "hotel", "bus", "coin", "ticket"
    val label: String,
    val isCoin: Boolean = false,
    val frontDesignText: String = "REPÚBLICA TURISTA",
    val backDesignText: String = "BANCO CENTRAL",
    val patternType: String = "classic", // "classic", "stars", "stripes", "retro"
    val frontIcon: String = "star",
    val backIcon: String = "favorite"
)

data class BoardProperty(
    val id: String,
    val name: String,
    val groupColor: Int, // Hex color of zone
    val groupName: String, // "Tomorrowland", "Frontierland", etc.
    val cost: Long,
    val mortgageValue: Long,
    val baseRent: Long,
    val rentWithBlock: Long,
    val rentWith1House: Long,
    val rentWith2Houses: Long,
    val rentWith3Houses: Long,
    val rentWith4Houses: Long,
    val rentWithCastle: Long,
    val houseCost: Long,
    val numHouses: Int = 0, // 0-4 houses, 5 means castle
    val isMortgaged: Boolean = false,
    val ownerId: String? = null // null means bank
)

data class GameRule(
    val id: String,
    val name: String,
    val description: String,
    val value: Long,
    val isEnabled: Boolean = true
)

data class SurpriseCard(
    val id: String,
    val title: String,
    val description: String,
    val amount: Long, // Positive is player receives, negative is player pays
    val isFromBank: Boolean = true, // true means bank pays/receives, false means between other players or free
    val category: String // "adventure", "tax", "bonus", "accident"
)

data class GameState(
    val isStarted: Boolean = false,
    val selectedMode: String = "disney", // "disney" or "classic"
    val players: List<Player> = emptyList(),
    val bankBalance: Long = 1000000L,
    val transactions: List<GameTransaction> = emptyList(),
    val rules: List<GameRule> = emptyList(),
    val bills: List<BillTemplate> = emptyList(),
    val surpriseCards: List<SurpriseCard> = emptyList(),
    val activeDrawnCard: SurpriseCard? = null,
    val activeDrawnPlayerId: String? = null,
    val properties: List<BoardProperty> = emptyList(),
    val dice1: Int = 1,
    val dice2: Int = 1,
    val consecutiveDoubles: Int = 0,
    val isDiceRolling: Boolean = false,
    val activeDiceOverlay: Boolean = false
)

