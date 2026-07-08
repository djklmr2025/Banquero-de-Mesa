package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Helper data class for payment animations
data class MoneyAnimation(
    val id: String = UUID.randomUUID().toString(),
    val amount: Long,
    val fromName: String,
    val toName: String,
    val isIncome: Boolean,
    val colorHex: Int = 0xFF4CAF50.toInt()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    // Screen navigation state within MainActivity
    private val _currentScreen = MutableStateFlow("login") // "login", "preset_selection", "setup", "dashboard", "bills_config"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Live animation trigger
    private val _activeAnimation = MutableStateFlow<MoneyAnimation?>(null)
    val activeAnimation: StateFlow<MoneyAnimation?> = _activeAnimation.asStateFlow()

    // Selected game preset mode
    val selectedMode = MutableStateFlow("disney") // "disney" or "classic"

    // Temporary list of setup players
    val setupPlayers = MutableStateFlow(
        listOf(
            Player("1", "Mickey Aventurero", 134500L, 0xFF6750A4.toInt(), "🐭"),
            Player("2", "Donald Explorador", 134500L, 0xFF381E72.toInt(), "🦆"),
            Player("3", "Goofy Viajero", 134500L, 0xFF21005D.toInt(), "🐶")
        )
    )

    // Starting settings before game begins
    val startingCapital = MutableStateFlow(134500L)
    val bankCapital = MutableStateFlow(1500000L)

    // Preconfigured rules
    val setupRules = MutableStateFlow<List<GameRule>>(emptyList())

    // Manual rules prompt text (for the AI bank)
    val customRulesPrompt = MutableStateFlow(
        "Si un jugador cae en la casilla de SALIDA (Estación de Santa Fe), cobra $20,000. " +
        "Si cae en DEPORTADO, no cobra nada y se queda como visita a menos que sea deportado formalmente. " +
        "Para salir de deportación paga $5,000 multa al banco o saca dobles."
    )

    // Custom bill templates with initial values
    val billTemplates = MutableStateFlow<List<BillTemplate>>(emptyList())

    // Full tourist deck of surprise cards
    private val initialSurpriseCards = listOf(
        SurpriseCard("c1", "Genie: ¡Vuelo Mágico!", "Felicidades, frotaste la lámpara. Recibes un bono mágico de $5,000 del Banco.", 5000L, true, "bonus"),
        SurpriseCard("c2", "Pasaporte Mágico: Entrada VIP", "Obtienes un boleto de fila preferencial para las principales atracciones. Recibes $8,000 del Banco.", 8000L, true, "bonus"),
        SurpriseCard("c3", "Genie: Impuesto de Souvenirs", "Compraste demasiadas orejitas de Mickey y playeras. Paga $3,000 al Banco.", -3000L, true, "tax"),
        SurpriseCard("c4", "Pasaporte Mágico: Multa por Comportamiento", "Te saltaste la fila en Space Mountain. Paga una multa de $2,000 al Banco.", -2000L, true, "tax"),
        SurpriseCard("c5", "Invitación Real", "Mickey te invita un almuerzo en el Castillo. Cada jugador te regala $1,500 para souvenirs.", 1500L, false, "bonus"),
        SurpriseCard("c6", "Genie: Tormenta de Fantasía", "Un hechizo te transporta directo al hotel. Paga $1,000 al Banco por transporte.", -1000L, true, "accident"),
        SurpriseCard("c7", "Pasaporte Mágico: ¡Premio Lotería!", "Ganas la trivia de trivia de princesas en Fantasyland. Recibes $10,000 del Banco.", 10000L, true, "bonus")
    )

    init {
        // Prepare initial states with the default "disney" mode
        selectGameMode("disney")

        // Sync state to Firebase Realtime Database in real-time
        viewModelScope.launch(Dispatchers.IO) {
            _gameState.collect { state ->
                if (state.isStarted) {
                    try {
                        val database = FirebaseDatabase.getInstance()
                        val reference = database.getReference("game_session")
                        
                        val stateMap = mapOf(
                            "isStarted" to state.isStarted,
                            "selectedMode" to state.selectedMode,
                            "bankBalance" to state.bankBalance,
                            "dice1" to state.dice1,
                            "dice2" to state.dice2,
                            "isDiceRolling" to state.isDiceRolling,
                            "players" to state.players.map { player ->
                                mapOf(
                                    "id" to player.id,
                                    "name" to player.name,
                                    "balance" to player.balance,
                                    "color" to player.color,
                                    "avatarEmoji" to player.avatarEmoji
                                )
                            },
                            "transactions" to state.transactions.map { tx ->
                                mapOf(
                                    "id" to tx.id,
                                    "timestamp" to tx.timestamp,
                                    "fromPlayerId" to tx.fromPlayerId,
                                    "fromPlayerName" to tx.fromPlayerName,
                                    "toPlayerId" to tx.toPlayerId,
                                    "toPlayerName" to tx.toPlayerName,
                                    "amount" to tx.amount,
                                    "concept" to tx.concept,
                                    "isAiProcessed" to tx.isAiProcessed
                                )
                            },
                            "properties" to state.properties.map { prop ->
                                mapOf(
                                    "id" to prop.id,
                                    "name" to prop.name,
                                    "groupColor" to prop.groupColor,
                                    "groupName" to prop.groupName,
                                    "cost" to prop.cost,
                                    "mortgageValue" to prop.mortgageValue,
                                    "baseRent" to prop.baseRent,
                                    "rentWithBlock" to prop.rentWithBlock,
                                    "rentWith1House" to prop.rentWith1House,
                                    "rentWith2Houses" to prop.rentWith2Houses,
                                    "rentWith3Houses" to prop.rentWith3Houses,
                                    "rentWith4Houses" to prop.rentWith4Houses,
                                    "rentWithCastle" to prop.rentWithCastle,
                                    "houseCost" to prop.houseCost,
                                    "numHouses" to prop.numHouses,
                                    "isMortgaged" to prop.isMortgaged,
                                    "ownerId" to prop.ownerId
                                )
                            }
                        )
                        reference.setValue(stateMap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- SETUP ACTIONS ---

    fun selectGameMode(mode: String) {
        selectedMode.value = mode
        when (mode) {
            "disney" -> {
                startingCapital.value = 134500L
                bankCapital.value = 1500000L
                setupPlayers.value = listOf(
                    Player("1", "Mickey Aventurero", 134500L, 0xFF6750A4.toInt(), "🐭"),
                    Player("2", "Donald Explorador", 134500L, 0xFF381E72.toInt(), "🦆"),
                    Player("3", "Goofy Viajero", 134500L, 0xFF21005D.toInt(), "🐶")
                )
                setupRules.value = listOf(
                    GameRule("rule_go", "Estación de Santa Fe (Salida)", "Premio del banco al pasar o caer en la salida.", 20000L),
                    GameRule("rule_jail", "Fianza de Deportado", "Costo al banco para salir del área de deportados.", 5000L),
                    GameRule("rule_magic", "Impuesto de Souvenirs", "Costo por souvenirs del parque cobrado por el banco.", 3000L)
                )
                billTemplates.value = listOf(
                    BillTemplate("bill_500", 500L, 0xFFFFD54F.toInt(), "coin", "Moneda Disney de Oro ($500)", isCoin = true, frontDesignText = "SOUVENIR GOLDEN COIN", backDesignText = "DISNEYLAND PARK", patternType = "stars", frontIcon = "star", backIcon = "favorite"),
                    BillTemplate("bill_1000", 1000L, 0xFF43A047.toInt(), "ticket", "Boleto Fantasyland ($1k)", isCoin = false, frontDesignText = "FANTASYLAND PASS", backDesignText = "FOTORAMA DE MÉXICO", patternType = "classic", frontIcon = "star", backIcon = "star"),
                    BillTemplate("bill_2000", 2000L, 0xFF00ACC1.toInt(), "ticket", "Pase Adventureland ($2k)", isCoin = false, frontDesignText = "ADVENTURELAND ADMISSION", backDesignText = "FOTORAMA DE MÉXICO", patternType = "stripes", frontIcon = "beach", backIcon = "beach"),
                    BillTemplate("bill_5000", 5000L, 0xFF29B6F6.toInt(), "bus", "Pase de Monorriel ($5k)", isCoin = false, frontDesignText = "DISNEYLAND MONORAIL", backDesignText = "FOTORAMA DE MÉXICO", patternType = "retro", frontIcon = "bus", backIcon = "bus"),
                    BillTemplate("bill_10000", 10000L, 0xFF8E24AA.toInt(), "airplane", "Boleto Tomorrowland ($10k)", isCoin = false, frontDesignText = "TOMORROWLAND RIDE", backDesignText = "FOTORAMA DE MÉXICO", patternType = "stars", frontIcon = "airplane", backIcon = "airplane"),
                    BillTemplate("bill_50000", 50000L, 0xFFFF5722.toInt(), "pyramid", "Bono Castillo ($50k)", isCoin = false, frontDesignText = "CINDERELLA CASTLE BOND", backDesignText = "FOTORAMA DE MÉXICO", patternType = "classic", frontIcon = "pyramid", backIcon = "pyramid")
                )
                customRulesPrompt.value = "Al dar la vuelta por la SALIDA (Estación de Santa Fe), se cobran $20,000.\nEl banco vende casas por $1,000/$2,000/$4,000 y castillos. Hipotecas pagan 10% adicional al levantar."
            }
            "classic" -> {
                startingCapital.value = 100000L
                bankCapital.value = 1000000L
                setupPlayers.value = listOf(
                    Player("1", "Turista Juan", 100000L, 0xFF00796B.toInt(), "🌴"),
                    Player("2", "Turista Sofía", 100000L, 0xFFE64A19.toInt(), "🎒"),
                    Player("3", "Turista Beto", 100000L, 0xFFD32F2F.toInt(), "📸")
                )
                setupRules.value = listOf(
                    GameRule("rule_go", "Vuelta por Salida", "Premio nacional que otorga el banco al dar la vuelta.", 20000L),
                    GameRule("rule_jail", "Fianza Federal (Cárcel)", "Fianza para salir de prisión.", 5000L),
                    GameRule("rule_tax", "Impuestos de Aduana", "Costo general de aduana cobrado por el banco.", 3000L)
                )
                billTemplates.value = listOf(
                    BillTemplate("bill_500", 500L, 0xFFFBC02D.toInt(), "coin", "Oro ($500)", isCoin = true, frontDesignText = "MONEDA DE ORO", backDesignText = "BANCO CENTRAL MEXICANO", patternType = "classic"),
                    BillTemplate("bill_1000", 1000L, 0xFF43A047.toInt(), "ticket", "Bono Playa ($1k)", isCoin = false, frontDesignText = "BONO VACACIONAL", backDesignText = "FOTORAMA"),
                    BillTemplate("bill_5000", 5000L, 0xFF00ACC1.toInt(), "bus", "Pase Bus ($5k)", isCoin = false, frontDesignText = "BOLETO TRANSPORTE", backDesignText = "SCT"),
                    BillTemplate("bill_10000", 10000L, 0xFF3949AB.toInt(), "airplane", "Boleto Avión ($10k)", isCoin = false, frontDesignText = "LINEA AEREA", backDesignText = "REPUBLICA MEXICANA"),
                    BillTemplate("bill_20000", 20000L, 0xFF8E24AA.toInt(), "hotel", "Bono Estancia ($20k)", isCoin = false, frontDesignText = "HOTEL RESORT", backDesignText = "ASOCIACION HOTELES"),
                    BillTemplate("bill_50000", 50000L, 0xFFE53935.toInt(), "pyramid", "Bono Arqueológico ($50k)", isCoin = false, frontDesignText = "ZONA ARQUEOLOGICA", backDesignText = "INAH")
                )
                customRulesPrompt.value = "Reglas estándar de Turista de México: El banco cobra aduanas y paga vueltas por la salida.\nPara salir de prisión paga $5,000 multa al banco."
            }
            "galactic" -> {
                startingCapital.value = 180000L
                bankCapital.value = 2500000L
                setupPlayers.value = listOf(
                    Player("1", "Mickey Cósmico", 180000L, 0xFF00E5FF.toInt(), "🚀"),
                    Player("2", "Donald Galáctico", 180000L, 0xFF76FF03.toInt(), "👾"),
                    Player("3", "Goofy Alien", 180000L, 0xFFD500F9.toInt(), "👽")
                )
                setupRules.value = listOf(
                    GameRule("rule_go", "Estación Espacial Alfa (Salida)", "Premio del banco al pasar o orbitar la salida interestelar.", 30000L),
                    GameRule("rule_jail", "Fianza de Cuarentena", "Costo al banco para salir del sector de cuarentena.", 8000L),
                    GameRule("rule_tax", "Peaje de Hiperespacio", "Costo por viajar a través del portal interestelar.", 4000L)
                )
                billTemplates.value = listOf(
                    BillTemplate("bill_500", 500L, 0xFF00E5FF.toInt(), "coin", "Crédito Galáctico ($500)", isCoin = true, frontDesignText = "INTERSTELLAR CREDIT", backDesignText = "ARKAIOS GALAXY", patternType = "stars", frontIcon = "star", backIcon = "star"),
                    BillTemplate("bill_2000", 2000L, 0xFF76FF03.toInt(), "ticket", "Bono Combustible ($2k)", isCoin = false, frontDesignText = "HYPERDRIVE FUEL", backDesignText = "ARKAIOS SECTOR", patternType = "stripes", frontIcon = "airplane", backIcon = "airplane"),
                    BillTemplate("bill_10000", 10000L, 0xFFD500F9.toInt(), "ticket", "Pasaporte Alien ($10k)", isCoin = false, frontDesignText = "ALIEN VISITOR PASS", backDesignText = "FEDERACION", patternType = "retro", frontIcon = "pyramid", backIcon = "pyramid"),
                    BillTemplate("bill_50000", 50000L, 0xFFFF3D00.toInt(), "pyramid", "Bono Nova ($50k)", isCoin = false, frontDesignText = "SUPERNOVA COIN", backDesignText = "FOTORAMA ESPACIAL", patternType = "stars", frontIcon = "star", backIcon = "star")
                )
                customRulesPrompt.value = "Reglas del Turista Galáctico: El banco opera con créditos holográficos.\nLas fianza de cuarentena cuesta $8,000 o dobles cósmicos. Edición de naves espaciales en el canvas."
            }
            "world" -> {
                startingCapital.value = 150000L
                bankCapital.value = 2000000L
                setupPlayers.value = listOf(
                    Player("1", "Turista Global", 150000L, 0xFFFFD600.toInt(), "🌍"),
                    Player("2", "Viajera Cosmopolita", 150000L, 0xFF00E676.toInt(), "✈️"),
                    Player("3", "Backpacker Pro", 150000L, 0xFF2979FF.toInt(), "🎒")
                )
                setupRules.value = listOf(
                    GameRule("rule_go", "Aeropuerto de Salida", "Subsidio de vuelo que otorga el banco al dar la vuelta al globo.", 25000L),
                    GameRule("rule_jail", "Fianza de Consulado (Migración)", "Pago consular para salir de detención migratoria.", 6000L),
                    GameRule("rule_tax", "Visa de Turismo Mundial", "Costo de aduana internacional cobrado por el banco.", 3500L)
                )
                billTemplates.value = listOf(
                    BillTemplate("bill_500", 500L, 0xFFFFD600.toInt(), "coin", "Euro de Oro ($500)", isCoin = true, frontDesignText = "GLOBAL COIN", backDesignText = "WORLD CENTRAL BANK", patternType = "classic", frontIcon = "star", backIcon = "coin"),
                    BillTemplate("bill_5000", 5000L, 0xFF00E676.toInt(), "bus", "Pase Tren Eurostar ($5k)", isCoin = false, frontDesignText = "EUROSTAR CONNECT", backDesignText = "TRANS-EUROPE", patternType = "stripes", frontIcon = "bus", backIcon = "bus"),
                    BillTemplate("bill_20000", 20000L, 0xFF2979FF.toInt(), "airplane", "Boleto Transatlántico ($20k)", isCoin = false, frontDesignText = "TRANSATLANTIC FLIGHT", backDesignText = "IATA SYSTEM", patternType = "retro", frontIcon = "airplane", backIcon = "airplane"),
                    BillTemplate("bill_100000", 100000L, 0xFFFF1744.toInt(), "pyramid", "Bono Global ($100k)", isCoin = false, frontDesignText = "CENTRAL WORLD BOND", backDesignText = "ARKAIOS ENTERTAINMENT", patternType = "stars", frontIcon = "pyramid", backIcon = "pyramid")
                )
                customRulesPrompt.value = "Reglas de Turista Mundial: Se viaja por las capitales del mundo.\nEl visado de turista mundial tiene tarifas aduaneras especiales en cada escala."
            }
        }
        _gameState.update {
            it.copy(
                selectedMode = mode,
                rules = setupRules.value,
                bills = billTemplates.value,
                bankBalance = bankCapital.value,
                players = setupPlayers.value
            )
        }
    }

    fun addSetupPlayer(name: String, color: Int, emoji: String) {
        val newList = setupPlayers.value.toMutableList()
        val id = (newList.size + 1).toString()
        newList.add(Player(id, name, startingCapital.value, color, emoji))
        setupPlayers.value = newList
    }

    fun removeSetupPlayer(id: String) {
        val newList = setupPlayers.value.toMutableList().filter { it.id != id }
        setupPlayers.value = newList
    }

    fun updateStartingCapital(capital: Long) {
        startingCapital.value = capital
        setupPlayers.value = setupPlayers.value.map { it.copy(balance = capital) }
    }

    fun updateBankCapital(capital: Long) {
        bankCapital.value = capital
    }

    fun updateBillDenomination(billId: String, newValue: Long) {
        val updated = billTemplates.value.map {
            if (it.id == billId) it.copy(denomination = newValue) else it
        }
        billTemplates.value = updated
        _gameState.update { it.copy(bills = updated) }
    }

    fun updateBillDesign(
        billId: String,
        label: String,
        isCoin: Boolean,
        frontDesignText: String,
        backDesignText: String,
        patternType: String,
        colorHex: Int,
        frontIcon: String,
        backIcon: String
    ) {
        val updated = billTemplates.value.map {
            if (it.id == billId) {
                it.copy(
                    label = label,
                    isCoin = isCoin,
                    frontDesignText = frontDesignText,
                    backDesignText = backDesignText,
                    patternType = patternType,
                    colorHex = colorHex,
                    frontIcon = frontIcon,
                    backIcon = backIcon
                )
            } else it
        }
        billTemplates.value = updated
        _gameState.update { it.copy(bills = updated) }
    }

    // --- CREATOR STUDIO: BILL EDITOR ---

    fun addBillTemplate(
        denomination: Long,
        colorHex: Int,
        iconName: String,
        label: String,
        isCoin: Boolean,
        frontDesignText: String,
        backDesignText: String,
        patternType: String,
        frontIcon: String,
        backIcon: String
    ) {
        val newBill = BillTemplate(
            id = "bill_custom_${'$'}{UUID.randomUUID()}",
            denomination = denomination,
            colorHex = colorHex,
            iconName = iconName,
            label = label,
            isCoin = isCoin,
            frontDesignText = frontDesignText,
            backDesignText = backDesignText,
            patternType = patternType,
            frontIcon = frontIcon,
            backIcon = backIcon
        )
        val updated = billTemplates.value + newBill
        billTemplates.value = updated
        _gameState.update { it.copy(bills = updated) }
    }

    fun removeBillTemplate(billId: String) {
        val updated = billTemplates.value.filter { it.id != billId }
        billTemplates.value = updated
        _gameState.update { it.copy(bills = updated) }
    }

    fun getInitialProperties(mode: String): List<BoardProperty> {
        return when (mode) {
            "disney" -> {
                listOf(
                    // Frontierland (Brownish Accent)
                    BoardProperty("p1", "Mansión Embrujada", 0xFF8B5A2B.toInt(), "Frontierland", 6000L, 3000L, 500L, 1000L, 2500L, 7000L, 15000L, 20000L, 25000L, 1000L),
                    BoardProperty("p2", "Piratas del Caribe", 0xFF8B5A2B.toInt(), "Frontierland", 6000L, 3000L, 500L, 1000L, 2500L, 7000L, 15000L, 20000L, 25000L, 1000L),
                    BoardProperty("p3", "Templo Prohibido", 0xFF8B5A2B.toInt(), "Frontierland", 8000L, 4000L, 800L, 1600L, 3500L, 10000L, 22000L, 26000L, 30000L, 1500L),
                    
                    // Tomorrowland (Sky Blue)
                    BoardProperty("p4", "Cohetes de la Órbita", 0xFF2196F3.toInt(), "Tomorrowland", 12000L, 6000L, 1200L, 2400L, 5000L, 14000L, 30000L, 35000L, 40000L, 2000L),
                    BoardProperty("p5", "Matterhorn Bobsleds", 0xFF2196F3.toInt(), "Tomorrowland", 14000L, 7000L, 1400L, 2800L, 6000L, 17000L, 35000L, 40000L, 45000L, 2500L),
                    BoardProperty("p6", "Submarinos de Nemo", 0xFF2196F3.toInt(), "Tomorrowland", 16000L, 8000L, 1600L, 3200L, 7000L, 20000L, 40000L, 45000L, 50000L, 3000L),
                    
                    // Adventureland (Green)
                    BoardProperty("p7", "Safari del Río", 0xFF4CAF50.toInt(), "Adventureland", 20000L, 10000L, 2000L, 4000L, 8000L, 22000L, 45000L, 50000L, 55000L, 4000L),
                    BoardProperty("p8", "Casa de Tarzán", 0xFF4CAF50.toInt(), "Adventureland", 22000L, 11000L, 2200L, 4400L, 9000L, 25000L, 50000L, 55000L, 60000L, 4500L),
                    BoardProperty("p9", "Caverna del Arcoíris", 0xFF4CAF50.toInt(), "Adventureland", 24000L, 12000L, 2400L, 4800L, 10000L, 28000L, 55000L, 60000L, 65000L, 5000L),
                    
                    // Transportation / Embassy equivalent (Purple)
                    BoardProperty("p10", "Monorriel Disneyland", 0xFF9C27B0.toInt(), "Monorrieles", 15000L, 7500L, 1500L, 3000L, 3000L, 3000L, 3000L, 3000L, 3000L, 0L),
                    BoardProperty("p11", "Ferrocarril de Santa Fe", 0xFF9C27B0.toInt(), "Monorrieles", 15000L, 7500L, 1500L, 3000L, 3000L, 3000L, 3000L, 3000L, 3000L, 0L)
                )
            }
            "classic" -> {
                listOf(
                    BoardProperty("p1", "Cancún", 0xFF00796B.toInt(), "Playas", 10000L, 5000L, 1000L, 2000L, 4000L, 10000L, 20000L, 25000L, 30000L, 2000L),
                    BoardProperty("p2", "Acapulco", 0xFF00796B.toInt(), "Playas", 12000L, 6000L, 1200L, 2400L, 5000L, 12000L, 22000L, 28000L, 32000L, 2000L),
                    BoardProperty("p3", "Los Cabos", 0xFF00796B.toInt(), "Playas", 15000L, 7500L, 1500L, 3000L, 6000L, 15000L, 25000L, 32000L, 38000L, 2500L),
                    
                    BoardProperty("p4", "Guadalajara", 0xFF3F51B5.toInt(), "Metrópolis", 20000L, 10000L, 2000L, 4000L, 8000L, 20000L, 35000L, 40000L, 45000L, 4000L),
                    BoardProperty("p5", "Monterrey", 0xFF3F51B5.toInt(), "Metrópolis", 22000L, 11000L, 2200L, 4400L, 9000L, 22000L, 38000L, 42000L, 48000L, 4000L),
                    BoardProperty("p6", "CDMX Centro", 0xFF3F51B5.toInt(), "Metrópolis", 26000L, 13000L, 2600L, 5200L, 11000L, 26000L, 42000L, 48000L, 55000L, 5000L)
                )
            }
            "galactic" -> {
                listOf(
                    BoardProperty("p1", "Estación Lunar", 0xFF8B5A2B.toInt(), "Sector Lunar", 12000L, 6000L, 1000L, 2000L, 5000L, 12000L, 25000L, 30000L, 35000L, 1500L),
                    BoardProperty("p2", "Cráter Copérnico", 0xFF8B5A2B.toInt(), "Sector Lunar", 12000L, 6000L, 1000L, 2000L, 5000L, 12000L, 25000L, 30000L, 35000L, 1500L),
                    BoardProperty("p3", "Base Apolo", 0xFF8B5A2B.toInt(), "Sector Lunar", 15000L, 7500L, 1200L, 2400L, 6000L, 15000L, 28000L, 33000L, 38000L, 2000L),
                    
                    BoardProperty("p4", "Planeta Marte", 0xFF2196F3.toInt(), "Sistema Solar", 22000L, 11000L, 2000L, 4000L, 9000L, 22000L, 45000L, 50000L, 55000L, 3000L),
                    BoardProperty("p5", "Anillos de Saturno", 0xFF2196F3.toInt(), "Sistema Solar", 24000L, 12000L, 2200L, 4400L, 10000L, 25000L, 48000L, 53000L, 58000L, 3500L),
                    BoardProperty("p6", "Nébula de Orión", 0xFF2196F3.toInt(), "Sistema Solar", 28000L, 14000L, 2500L, 5000L, 11000L, 28000L, 52000L, 58000L, 64000L, 4000L),
                    
                    BoardProperty("p7", "Planeta Gliese", 0xFF4CAF50.toInt(), "Exoplanetas", 35000L, 17500L, 3200L, 6400L, 15000L, 35000L, 70000L, 75000L, 80000L, 5000L),
                    BoardProperty("p8", "Estrella Betelgeuse", 0xFF4CAF50.toInt(), "Exoplanetas", 38000L, 19000L, 3500L, 7000L, 16000L, 38000L, 75000L, 80000L, 85000L, 5500L),
                    BoardProperty("p9", "Cúmulo de Pléyades", 0xFF4CAF50.toInt(), "Exoplanetas", 42000L, 21000L, 4000L, 8000L, 18000L, 42000L, 80000L, 85000L, 90000L, 6000L)
                )
            }
            "world" -> {
                listOf(
                    BoardProperty("p1", "Roma Coliseo", 0xFF00796B.toInt(), "Europa", 14000L, 7000L, 1200L, 2400L, 5500L, 14000L, 28000L, 34000L, 40000L, 2500L),
                    BoardProperty("p2", "Torre Eiffel", 0xFF00796B.toInt(), "Europa", 15000L, 7500L, 1300L, 2600L, 6000L, 15000L, 30000L, 36000L, 42000L, 2500L),
                    BoardProperty("p3", "Canales de Venecia", 0xFF00796B.toInt(), "Europa", 18000L, 9000L, 1500L, 3000L, 7000L, 18000L, 34000L, 40000L, 46000L, 3000L),
                    
                    BoardProperty("p4", "Tokio Shibuya", 0xFF3F51B5.toInt(), "Asia", 25000L, 12500L, 2200L, 4400L, 9500L, 25000L, 50000L, 56000L, 62000L, 4500L),
                    BoardProperty("p5", "Gran Muralla China", 0xFF3F51B5.toInt(), "Asia", 28000L, 14000L, 2500L, 5000L, 11000L, 28000L, 54000L, 60000L, 66000L, 4500L),
                    BoardProperty("p6", "Taj Mahal", 0xFF3F51B5.toInt(), "Asia", 32000L, 16000L, 3000L, 6000L, 13000L, 32000L, 60000L, 66000L, 72000L, 5000L)
                )
            }
            else -> emptyList()
        }
    }

    fun startGame() {
        val finalPlayers = setupPlayers.value.map { it.copy(balance = startingCapital.value) }
        val mode = selectedMode.value
        _gameState.value = GameState(
            isStarted = true,
            selectedMode = mode,
            players = finalPlayers,
            bankBalance = bankCapital.value,
            rules = setupRules.value,
            bills = billTemplates.value,
            surpriseCards = initialSurpriseCards,
            properties = getInitialProperties(mode),
            transactions = emptyList()
        )
        _currentScreen.value = "dashboard"
    }

    fun resetGame() {
        val mode = selectedMode.value
        _gameState.value = GameState(
            isStarted = false,
            selectedMode = mode,
            rules = setupRules.value,
            bills = billTemplates.value,
            surpriseCards = initialSurpriseCards
        )
        _currentScreen.value = "setup"
    }

    // --- PROPERTY MANAGEMENT ENGINE ---

    fun buyProperty(playerId: String, propertyId: String) {
        val state = _gameState.value
        val prop = state.properties.find { it.id == propertyId } ?: return
        if (prop.ownerId != null) return // Already owned
        
        val ok = performTransaction(
            fromPlayerId = playerId,
            toPlayerId = null, // Pay to Bank
            amount = prop.cost,
            concept = "Compra de ${prop.name} 🏰"
        )
        if (ok) {
            _gameState.update { s ->
                s.copy(
                    properties = s.properties.map { p ->
                        if (p.id == propertyId) p.copy(ownerId = playerId) else p
                    }
                )
            }
        }
    }

    fun payRent(visitorId: String, propertyId: String) {
        val state = _gameState.value
        val prop = state.properties.find { it.id == propertyId } ?: return
        val ownerId = prop.ownerId ?: return
        if (ownerId == visitorId) return // Cannot pay yourself
        if (prop.isMortgaged) return // Mortgaged property yields no rent

        // Rent calculation based on rules
        var rentAmount = prop.baseRent
        if (prop.numHouses == 0) {
            // Check full block ownership for doubling
            val block = state.properties.filter { it.groupName == prop.groupName }
            val ownsFullBlock = block.all { it.ownerId == ownerId }
            if (ownsFullBlock) {
                rentAmount = prop.baseRent * 2
            }
        } else {
            rentAmount = when (prop.numHouses) {
                1 -> prop.rentWith1House
                2 -> prop.rentWith2Houses
                3 -> prop.rentWith3Houses
                4 -> prop.rentWith4Houses
                5 -> prop.rentWithCastle
                else -> prop.baseRent
            }
        }

        performTransaction(
            fromPlayerId = visitorId,
            toPlayerId = ownerId,
            amount = rentAmount,
            concept = "Alquiler en ${prop.name} (${prop.groupName}) 🏡"
        )
    }

    fun buildOnProperty(propertyId: String) {
        val state = _gameState.value
        val prop = state.properties.find { it.id == propertyId } ?: return
        val ownerId = prop.ownerId ?: return

        // Verify full block ownership
        val block = state.properties.filter { it.groupName == prop.groupName }
        val ownsFullBlock = block.all { it.ownerId == ownerId }
        if (!ownsFullBlock) {
            GameSoundEffects.playError()
            return
        }

        // Limit levels (0-4 houses, 5 is castle)
        if (prop.numHouses >= 5) return

        // Uniform construction check
        val minHouses = block.minOf { it.numHouses }
        if (prop.numHouses > minHouses) {
            GameSoundEffects.playError()
            return
        }

        val cost = prop.houseCost
        val ok = performTransaction(
            fromPlayerId = ownerId,
            toPlayerId = null, // Pay to Bank
            amount = cost,
            concept = "Nivel de construcción en ${prop.name} (+1) 🛠️"
        )
        if (ok) {
            _gameState.update { s ->
                s.copy(
                    properties = s.properties.map { p ->
                        if (p.id == propertyId) p.copy(numHouses = p.numHouses + 1) else p
                    }
                )
            }
        }
    }

    fun toggleMortgage(propertyId: String) {
        val state = _gameState.value
        val prop = state.properties.find { it.id == propertyId } ?: return
        val ownerId = prop.ownerId ?: return

        if (!prop.isMortgaged) {
            // Refund houses first (half value)
            var housesRefund = 0L
            if (prop.numHouses > 0) {
                housesRefund = (prop.numHouses * prop.houseCost) / 2
                _gameState.update { s ->
                    s.copy(
                        properties = s.properties.map { p ->
                            if (p.id == propertyId) p.copy(numHouses = 0) else p
                        }
                    )
                }
            }

            // Receive mortgage capital from Bank
            performTransaction(
                fromPlayerId = null,
                toPlayerId = ownerId,
                amount = prop.mortgageValue + housesRefund,
                concept = "Hipotecar ${prop.name} 🏦"
            )
            _gameState.update { s ->
                s.copy(
                    properties = s.properties.map { p ->
                        if (p.id == propertyId) p.copy(isMortgaged = true) else p
                    }
                )
            }
        } else {
            // Lift mortgage (Pay value + 10% interest)
            val payAmt = (prop.mortgageValue * 1.10).toLong()
            val ok = performTransaction(
                fromPlayerId = ownerId,
                toPlayerId = null,
                amount = payAmt,
                concept = "Recuperar Hipoteca de ${prop.name} (10% extra) 🔓"
            )
            if (ok) {
                _gameState.update { s ->
                    s.copy(
                        properties = s.properties.map { p ->
                            if (p.id == propertyId) p.copy(isMortgaged = false) else p
                        }
                    )
                }
            }
        }
    }

    fun auctionProperty(propertyId: String, winnerId: String, bidAmount: Long) {
        val ok = performTransaction(
            fromPlayerId = winnerId,
            toPlayerId = null,
            amount = bidAmount,
            concept = "Subasta Ganada: Propiedad adquirida por \$${bidAmount} 🔨"
        )
        if (ok) {
            _gameState.update { s ->
                s.copy(
                    properties = s.properties.map { p ->
                        if (p.id == propertyId) p.copy(ownerId = winnerId) else p
                    }
                )
            }
        }
    }

    // --- DICE ROLLING CONTROLS ---

    fun toggleDiceOverlay(visible: Boolean) {
        _gameState.update { it.copy(activeDiceOverlay = visible) }
    }

    fun rollDice(currentPlayerId: String) {
        if (_gameState.value.isDiceRolling) return

        _gameState.update { it.copy(isDiceRolling = true) }

        viewModelScope.launch {
            var d1 = 1
            var d2 = 1
            for (i in 1..8) {
                d1 = (1..6).random()
                d2 = (1..6).random()
                _gameState.update { it.copy(dice1 = d1, dice2 = d2) }
                delay(100)
            }

            val isDoubles = d1 == d2
            val nextDoubles = if (isDoubles) _gameState.value.consecutiveDoubles + 1 else 0

            val finalD1 = d1
            val finalD2 = d2

            withContext(Dispatchers.Main) {
                _gameState.update {
                    it.copy(
                        isDiceRolling = false,
                        dice1 = finalD1,
                        dice2 = finalD2,
                        consecutiveDoubles = nextDoubles
                    )
                }
                GameSoundEffects.playDiceRoll()

                if (nextDoubles >= 3) {
                    // Send to deportado due to 3 doubles in a row
                    performTransaction(
                        fromPlayerId = currentPlayerId,
                        toPlayerId = null,
                        amount = 5000L,
                        concept = "¡3 DOBLES! Multa automática de Deportado 🚨"
                    )
                    _gameState.update { it.copy(consecutiveDoubles = 0) }
                }
            }
        }
    }

    // --- GAMEPLAY ACTIONS ---

    fun payGoAround(playerId: String) {
        val player = _gameState.value.players.find { it.id == playerId } ?: return
        val goRule = _gameState.value.rules.find { it.id == "rule_go" }
        val amount = goRule?.value ?: 20000L

        performTransaction(
            fromPlayerId = null, // Bank
            toPlayerId = playerId,
            amount = amount,
            concept = "Vuelta por Salida 🏁"
        )
    }

    fun drawSurpriseCard(playerId: String) {
        val deck = _gameState.value.surpriseCards
        if (deck.isEmpty()) return
        val card = deck.random()

        GameSoundEffects.playCardDraw()
        _gameState.update {
            it.copy(
                activeDrawnCard = card,
                activeDrawnPlayerId = playerId
            )
        }
    }

    fun applyDrawnCard() {
        val state = _gameState.value
        val card = state.activeDrawnCard ?: return
        val playerId = state.activeDrawnPlayerId ?: return
        val player = state.players.find { it.id == playerId } ?: return

        if (card.isFromBank) {
            if (card.amount > 0) {
                // Bank pays player
                performTransaction(
                    fromPlayerId = null,
                    toPlayerId = playerId,
                    amount = card.amount,
                    concept = "Carta Sorpresa: ${card.title} 🎟️"
                )
            } else {
                // Player pays bank
                performTransaction(
                    fromPlayerId = playerId,
                    toPlayerId = null,
                    amount = -card.amount,
                    concept = "Carta Sorpresa: ${card.title} 🎟️"
                )
            }
        } else {
            // Player to players transaction
            if (card.amount < 0) {
                // Active player pays everyone else
                val eachPay = -card.amount
                state.players.forEach { other ->
                    if (other.id != playerId) {
                        performTransaction(
                            fromPlayerId = playerId,
                            toPlayerId = other.id,
                            amount = eachPay,
                            concept = "Mariachi & Antojitos 🌮"
                        )
                    }
                }
            } else {
                // Everyone else pays active player
                // Let's say player receives 2,000 from everyone
                val eachReceive = 1500L
                state.players.forEach { other ->
                    if (other.id != playerId) {
                        performTransaction(
                            fromPlayerId = other.id,
                            toPlayerId = playerId,
                            amount = eachReceive,
                            concept = "Guía Turístico Estrella ⭐"
                        )
                    }
                }
            }
        }

        // Reset drawn state
        _gameState.update {
            it.copy(activeDrawnCard = null, activeDrawnPlayerId = null)
        }
    }

    fun dismissDrawnCard() {
        _gameState.update {
            it.copy(activeDrawnCard = null, activeDrawnPlayerId = null)
        }
    }

    // --- TRANSACTION HANDLING ENGINE ---

    fun performTransaction(
        fromPlayerId: String?,
        toPlayerId: String?,
        amount: Long,
        concept: String
    ): Boolean {
        if (amount <= 0) return false

        var success = false
        val state = _gameState.value

        // Names
        val fromName = if (fromPlayerId == null) "Banco 🏛️" else state.players.find { it.id == fromPlayerId }?.name ?: "Desconocido"
        val toName = if (toPlayerId == null) "Banco 🏛️" else state.players.find { it.id == toPlayerId }?.name ?: "Desconocido"

        // Verify balance
        if (fromPlayerId != null) {
            val playerBalance = state.players.find { it.id == fromPlayerId }?.balance ?: 0L
            if (playerBalance < amount) {
                GameSoundEffects.playError()
                return false
            }
        } else {
            if (state.bankBalance < amount) {
                GameSoundEffects.playError()
                return false
            }
        }

        // Apply changes
        val updatedPlayers = state.players.map { player ->
            when (player.id) {
                fromPlayerId -> player.copy(balance = player.balance - amount)
                toPlayerId -> player.copy(balance = player.balance + amount)
                else -> player
            }
        }

        val updatedBankBalance = when {
            fromPlayerId == null -> state.bankBalance - amount
            toPlayerId == null -> state.bankBalance + amount
            else -> state.bankBalance
        }

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())

        val newTransaction = GameTransaction(
            id = UUID.randomUUID().toString(),
            timestamp = timestamp,
            fromPlayerId = fromPlayerId,
            fromPlayerName = fromName,
            toPlayerId = toPlayerId,
            toPlayerName = toName,
            amount = amount,
            concept = concept
        )

        // Trigger animations
        val animationColor = if (toPlayerId == null) 0xFFE53935.toInt() else 0xFF4CAF50.toInt() // Red for outflow to bank, Green for receive
        _activeAnimation.value = MoneyAnimation(
            amount = amount,
            fromName = fromName,
            toName = toName,
            isIncome = fromPlayerId == null, // Income if from Bank or general
            colorHex = animationColor
        )

        // Sound trigger
        if (amount >= 20000) {
            GameSoundEffects.playKaching()
        } else {
            GameSoundEffects.playCoin()
        }

        _gameState.update {
            it.copy(
                players = updatedPlayers,
                bankBalance = updatedBankBalance,
                transactions = listOf(newTransaction) + it.transactions
            )
        }

        success = true

        // Dismiss animation after delay
        viewModelScope.launch {
            delay(1800)
            _activeAnimation.value = null
        }

        return success
    }

    // --- GEMINI AI AUTOMATIC BANK PROMPT PROCESSOR ---

    fun sendAiCommand(command: String) {
        if (command.isBlank()) return

        _isAiLoading.value = true
        _aiResult.value = null

        viewModelScope.launch {
            try {
                val state = _gameState.value
                val apiKey = BuildConfig.GEMINI_API_KEY

                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    withContext(Dispatchers.Main) {
                        _isAiLoading.value = false
                        _aiResult.value = "⚠️ Error: Falta configurar la GEMINI_API_KEY en los secretos de AI Studio."
                        GameSoundEffects.playError()
                    }
                    return@launch
                }

                // Construct system instruction
                val playersListStr = state.players.joinToString("\n") { "- ID: '${it.id}', Nombre: '${it.name}', Capital: \$${it.balance}" }
                val rulesListStr = state.rules.joinToString("\n") { "- ${it.name}: ${it.description} (\$\$${it.value})" }

                val systemInstructionText = """
                    Eres el Banquero Inteligente Automático de un juego de mesa de turistas mexicanos ("Banquero de Mesa").
                    Tu tarea es analizar comandos de voz o texto del juego, interpretar las reglas y balances actuales, y decidir si se realiza una transferencia de dinero.
                    
                    REGLAS CONFIGURADAS ACTUALES:
                    - Capital del Banco: $${state.bankBalance}
                    $rulesListStr
                    
                    REGLAS ADICIONALES MANUALES:
                    ${customRulesPrompt.value}
                    
                    JUGADORES EN LA PARTIDA:
                    - ID: 'banco', Nombre: 'Banco' (Este representa al Banco Central)
                    $playersListStr
                    
                    Debes responder EXCLUSIVAMENTE con un objeto JSON válido con la siguiente estructura:
                    {
                      "valid": true o false (true si pudiste interpretar con certeza una transferencia monetaria, de lo contrario false),
                      "fromPlayerId": "banco" o el ID exacto del jugador que paga,
                      "toPlayerId": "banco" o el ID exacto del jugador que recibe,
                      "amount": número entero positivo (la cantidad a transferir),
                      "message": "Mensaje alegre en español detallando qué ocurrió y por qué, usando un tono temático y folclórico de turismo mexicano",
                      "reason": "Categoría corta (ej: 'salida', 'multa', 'renta', 'peaje', 'error')"
                    }
                    
                    Si la acción no es una transacción de dinero (ej: preguntas sobre reglas, saludos, o acciones inválidas), responde con "valid": false y explica por qué con amabilidad turística en el campo "message".
                    ASEGÚRATE de no agregar bloques de código como ```json ``` en tu respuesta. Envía solo el texto plano del JSON.
                """.trimIndent()

                val requestBody = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = "Comando del juego: \"$command\""))
                        )
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemInstructionText))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseFormat = GeminiResponseFormat(
                            text = GeminiResponseFormatText(mimeType = "application/json")
                        ),
                        temperature = 0.2f
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiRetrofitClient.service.generateContent(apiKey, requestBody)
                }

                val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (rawJson != null) {
                    val adapter = GeminiRetrofitClient.jsonParser.adapter(AiBankTransaction::class.java)
                    val aiTx = withContext(Dispatchers.Default) {
                        adapter.fromJson(rawJson)
                    }

                    withContext(Dispatchers.Main) {
                        _isAiLoading.value = false
                        if (aiTx != null && aiTx.valid) {
                            // Translate "banco" ID string to null
                            val fromId = if (aiTx.fromPlayerId == "banco" || aiTx.fromPlayerId == null) null else aiTx.fromPlayerId
                            val toId = if (aiTx.toPlayerId == "banco" || aiTx.toPlayerId == null) null else aiTx.toPlayerId

                            val ok = performTransaction(
                                fromPlayerId = fromId,
                                toPlayerId = toId,
                                amount = aiTx.amount,
                                concept = "Banquero AI: ${aiTx.message}"
                            )

                            if (ok) {
                                _aiResult.value = "🤖 IA: ${aiTx.message}"
                            } else {
                                _aiResult.value = "❌ Fondos insuficientes para procesar: ${aiTx.message}"
                                GameSoundEffects.playError()
                            }
                        } else {
                            _aiResult.value = "🤖 IA: " + (aiTx?.message ?: "No entendí la acción de banco. Intenta ser más específico.")
                            GameSoundEffects.playError()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _isAiLoading.value = false
                        _aiResult.value = "⚠️ Error al recibir respuesta de la inteligencia artificial."
                        GameSoundEffects.playError()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isAiLoading.value = false
                    _aiResult.value = "⚠️ Error de red o formato de IA: ${e.localizedMessage}"
                    GameSoundEffects.playError()
                }
            }
        }
    }

    fun clearAiResult() {
        _aiResult.value = null
    }
}
