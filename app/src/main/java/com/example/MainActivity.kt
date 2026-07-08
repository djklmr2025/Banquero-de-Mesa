package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    MainScreenContent(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreenContent(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val state by viewModel.gameState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameNavyDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (currentScreen != "login" && currentScreen != "preset_selection") {
                HeaderAppBar(viewModel = viewModel)
            }
            
            Crossfade(
                targetState = currentScreen,
                label = "ScreenTransition",
                modifier = Modifier.weight(1f)
            ) { screen ->
                when (screen) {
                    "login" -> GameLoginView(viewModel = viewModel)
                    "preset_selection" -> GamePresetSelectionView(viewModel = viewModel)
                    "setup" -> GameSetupView(viewModel = viewModel)
                    "dashboard" -> GameDashboardView(viewModel = viewModel)
                    "bills_config" -> GameBillsConfigView(viewModel = viewModel)
                }
            }
        }

        // Global interactive transaction overlay animation
        val activeAnim by viewModel.activeAnimation.collectAsStateWithLifecycle()
        AnimatedVisibility(
            visible = activeAnim != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .zIndex(10f)
        ) {
            activeAnim?.let { anim ->
                TransactionAnimationOverlay(anim = anim)
            }
        }
    }
}

@Composable
fun HeaderAppBar(viewModel: GameViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .drawBehind {
                // Subtle shadow bottom line
                drawLine(
                    color = Color(0xFFCAC4D0),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Icon Circle/Square
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF6750A4)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "FM",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Column {
                Text(
                    text = "Futurama de México",
                    color = Color(0xFF1D1B20),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "SESIÓN: PLAYA DEL CARMEN",
                    color = Color(0xFF49454F),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
        }

        // Live status indicator (Banco AI)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEADDFF))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Pulse dot
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF21005D).copy(alpha = alpha))
                )
                Text(
                    text = "BANCO AI",
                    color = Color(0xFF21005D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Shortcut to the visual bill designer (Creator Studio)
        IconButton(onClick = { viewModel.navigateTo("bills_config") }) {
            Icon(
                imageVector = Icons.Filled.Palette,
                contentDescription = "Diseñar billetes",
                tint = Color(0xFF6750A4)
            )
        }
    }
}

// --- TRANSACCIÓN ANIMACIÓN GLOBAL (REAL-TIME ANIMATION ON PAYMENT) ---
@Composable
fun TransactionAnimationOverlay(anim: MoneyAnimation) {
    val infiniteTransition = rememberInfiniteTransition(label = "notes")
    val translationX by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translationX"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = GameNavyCard),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .border(2.dp, Color(anim.colorHex), RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (anim.isIncome) "💸 ¡INGRESO EN CAMINO!" else "💸 ¡PAGO EN CURSO!",
                    color = Color(anim.colorHex),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${anim.fromName} ➡️ ${anim.toName}",
                    color = TextLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(anim.colorHex).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = 60.dp.toPx()
                    val height = 30.dp.toPx()
                    val x = (size.width - width) / 2 + translationX
                    val y = (size.height - height) / 2

                    drawRoundRect(
                        color = Color(anim.colorHex),
                        topLeft = Offset(x, y),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color(anim.colorHex),
                        radius = 6.dp.toPx(),
                        center = Offset(x + width / 2, y + height / 2)
                    )
                }

                Text(
                    text = "$${String.format("%,d", anim.amount)}",
                    color = Color(anim.colorHex),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// ==========================================
// 1. CONFIGURACIÓN INICIAL (GAME SETUP VIEW)
// ==========================================
@Composable
fun GameSetupView(viewModel: GameViewModel) {
    val players by viewModel.setupPlayers.collectAsStateWithLifecycle()
    val startingCap by viewModel.startingCapital.collectAsStateWithLifecycle()
    val bankCap by viewModel.bankCapital.collectAsStateWithLifecycle()
    val rules by viewModel.setupRules.collectAsStateWithLifecycle()
    val customRulesPrompt by viewModel.customRulesPrompt.collectAsStateWithLifecycle()
    val bills by viewModel.billTemplates.collectAsStateWithLifecycle()

    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var editingBillTemplate by remember { mutableStateOf<BillTemplate?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GameNavyDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 48.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .drawBehind {
                        val brush = Brush.verticalGradient(
                            colors = listOf(GameTerracotta, GameGold)
                        )
                        drawRoundRect(
                            brush = brush,
                            cornerRadius = CornerRadius(24.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f),
                            radius = 120.dp.toPx(),
                            center = Offset(size.width * 0.85f, size.height * 1.1f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            radius = 200.dp.toPx(),
                            center = Offset(size.width * 0.85f, size.height * 1.1f)
                        )
                    }
                    .padding(20.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = "BANQUERO DE MESA",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Asistente Virtual de Finanzas y Reglas IA 🌴",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "🎡 Selecciona tu preset de Juego",
                color = GameGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val currentMode by viewModel.selectedMode.collectAsStateWithLifecycle()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameNavyCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentMode == "disney") GameTeal else Color.Transparent)
                        .clickable { viewModel.selectGameMode("disney") }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🏰 Turista Disneyland",
                            color = if (currentMode == "disney") GameNavyDark else TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Preset Fotorama de México",
                            color = if (currentMode == "disney") GameNavyDark.copy(alpha = 0.7f) else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentMode == "classic") GameTeal else Color.Transparent)
                        .clickable { viewModel.selectGameMode("classic") }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🌴 Turista Clásico",
                            color = if (currentMode == "classic") GameNavyDark else TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Preset de Viaje Tradicional",
                            color = if (currentMode == "classic") GameNavyDark.copy(alpha = 0.7f) else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "💰 Ajustes Financieros Generales",
                color = GameGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Capital por Jugador:", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "$${String.format("%,d", startingCap)}",
                            color = GameTeal,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Slider(
                        value = startingCap.toFloat(),
                        onValueChange = { viewModel.updateStartingCapital(it.toLong()) },
                        valueRange = 10000f..300000f,
                        steps = 58,
                        colors = SliderDefaults.colors(
                            thumbColor = GameTeal,
                            activeTrackColor = GameTeal,
                            inactiveTrackColor = GameNavyDark
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Capital del Banco:", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "$${String.format("%,d", bankCap)}",
                            color = GameGold,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Slider(
                        value = bankCap.toFloat(),
                        onValueChange = { viewModel.updateBankCapital(it.toLong()) },
                        valueRange = 100000f..5000000f,
                        steps = 49,
                        colors = SliderDefaults.colors(
                            thumbColor = GameGold,
                            activeTrackColor = GameGold,
                            inactiveTrackColor = GameNavyDark
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💵 Billetes de Juego Custom",
                    color = GameGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca para Editar",
                    color = GameTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Personaliza la denominación de los billetes que se usarán en la partida.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Simple vertical column of customizable bills instead of Grid view to avoid nested scrolling constraints
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    bills.forEach { bill ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingBillTemplate = bill }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(bill.colorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when(bill.iconName) {
                                            "coin" -> "🪙"
                                            "ticket" -> "🎟️"
                                            "bus" -> "🚌"
                                            "airplane" -> "✈️"
                                            "hotel" -> "🏨"
                                            "pyramid" -> "🏛️"
                                            else -> "💵"
                                        },
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(bill.label, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Fijar denominación personalizada", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                            Text(
                                text = "$${String.format("%,d", bill.denomination)}",
                                color = Color(bill.colorHex),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        if (bill != bills.last()) {
                            HorizontalDivider(color = GameNavyDark, thickness = 1.dp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "📋 Reglas Automáticas del Tablero",
                color = GameGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    rules.forEach { rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rule.name, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(rule.description, color = TextMuted, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "$${String.format("%,d", rule.value)}",
                                color = GameTeal,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                        if (rule != rules.last()) {
                            HorizontalDivider(color = GameNavyDark, thickness = 1.dp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "🧠 Reglas y Criterio del Banquero IA",
                color = GameGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Configura reglas de negocio para el asistente de IA. El banco inteligente las consultará para calcular transacciones automáticamente.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = customRulesPrompt,
                onValueChange = { viewModel.customRulesPrompt.value = it },
                textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GameTeal,
                    unfocusedBorderColor = GameNavyCard,
                    focusedContainerColor = GameNavyCard,
                    unfocusedContainerColor = GameNavyCard
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 Turistas de la Partida (${players.size})",
                    color = GameGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddPlayerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GameTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = GameNavyDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir", color = GameNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (players.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no hay turistas. Registra al menos un jugador para comenzar.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(players) { player ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(player.color).copy(alpha = 0.2f))
                                    .border(2.dp, Color(player.color), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(player.avatarEmoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(player.name, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Billetera: $${String.format("%,d", player.balance)}", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        IconButton(
                            onClick = { viewModel.removeSetupPlayer(player.id) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = GameTerracotta)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { viewModel.startGame() },
                enabled = players.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GameTeal,
                    disabledContainerColor = GameNavyCard
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_game_button")
            ) {
                Text(
                    "INICIAR ASISTENTE DE BANCO 🏁",
                    color = if (players.isNotEmpty()) GameNavyDark else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    // Modal to Edit Bill Denomination & Design Canvas (Fijar su Valor + Personalizar)
    if (editingBillTemplate != null) {
        val bill = editingBillTemplate!!
        
        var labelText by remember { mutableStateOf(bill.label) }
        var valueText by remember { mutableStateOf(bill.denomination.toString()) }
        var isCoinState by remember { mutableStateOf(bill.isCoin) }
        var frontTextState by remember { mutableStateOf(bill.frontDesignText) }
        var backTextState by remember { mutableStateOf(bill.backDesignText) }
        var patternState by remember { mutableStateOf(bill.patternType) }
        var selectedColorState by remember { mutableIntStateOf(bill.colorHex) }
        var frontIconState by remember { mutableStateOf(bill.frontIcon) }
        var backIconState by remember { mutableStateOf(bill.backIcon) }
        var isEditingFront by remember { mutableStateOf(true) }

        val designColorOptions = listOf(
            0xFF00BFA5.toInt(), // Teal
            0xFFFF5722.toInt(), // Terracotta
            0xFFFFD54F.toInt(), // Gold
            0xFF29B6F6.toInt(), // Sky Blue
            0xFFAB47BC.toInt(), // Purple
            0xFF4CAF50.toInt(), // Green
            0xFFFF4081.toInt()  // Pink
        )

        val designIconOptions = listOf("star", "favorite", "beach", "pyramid", "airplane", "bus", "hotel", "coin")
        val designPatterns = listOf("classic", "stars", "stripes", "retro")

        Dialog(onDismissRequest = { editingBillTemplate = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(selectedColorState), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "🎨 DISEÑADOR DE CARTERA VIRTUAL",
                        color = GameGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Ajusta valores y diseña ambas caras del billete o moneda",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // LIVE INTERACTIVE CANVAS PREVIEW
                    Text(
                        text = if (isEditingFront) "Vista Previa: FRENTE" else "Vista Previa: REVERSO",
                        color = Color(selectedColorState),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    BillDesignCanvas(
                        bill = bill,
                        isCoin = isCoinState,
                        label = labelText,
                        denomination = valueText.toLongOrNull() ?: 0L,
                        colorHex = selectedColorState,
                        frontText = frontTextState,
                        backText = backTextState,
                        pattern = patternState,
                        frontIcon = frontIconState,
                        backIcon = backIconState,
                        isFront = isEditingFront
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Front / Back toggle selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GameNavyDark)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isEditingFront) GameTeal else Color.Transparent)
                                .clickable { isEditingFront = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Frente", color = if (isEditingFront) GameNavyDark else TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isEditingFront) GameTeal else Color.Transparent)
                                .clickable { isEditingFront = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Reverso", color = if (!isEditingFront) GameNavyDark else TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Basic Config fields
                    OutlinedTextField(
                        value = labelText,
                        onValueChange = { labelText = it },
                        label = { Text("Nombre del Billete", color = TextMuted) },
                        textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it.filter { char -> char.isDigit() } },
                        label = { Text("Valor Nominal ($)", color = TextMuted) },
                        textStyle = TextStyle(color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Format type switch: Billete or Moneda
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Formato Físico:", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row {
                            Button(
                                onClick = { isCoinState = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (!isCoinState) GameTeal else GameNavyDark),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Billete 💵", color = if (!isCoinState) GameNavyDark else TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { isCoinState = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isCoinState) GameTeal else GameNavyDark),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Moneda 🪙", color = if (isCoinState) GameNavyDark else TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Front & Back Custom Texts
                    OutlinedTextField(
                        value = frontTextState,
                        onValueChange = { frontTextState = it },
                        label = { Text("Texto Impreso Frente", color = TextMuted) },
                        textStyle = TextStyle(color = TextLight, fontSize = 12.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = backTextState,
                        onValueChange = { backTextState = it },
                        label = { Text("Texto Impreso Reverso", color = TextMuted) },
                        textStyle = TextStyle(color = TextLight, fontSize = 12.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Texture Pattern Selector
                    if (!isCoinState) {
                        Text("Diseño del Fondo (Textura):", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            designPatterns.forEach { pat ->
                                val isSel = patternState == pat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) GameTeal else GameNavyDark)
                                        .clickable { patternState = pat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(pat.uppercase(), color = if (isSel) GameNavyDark else TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Color Selector
                    Text("Color Principal:", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        designColorOptions.forEach { col ->
                            val isSel = selectedColorState == col
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .border(2.dp, if (isSel) Color.White else Color.Transparent, CircleShape)
                                    .clickable { selectedColorState = col },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = "Sel", tint = GameNavyDark, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Icon selections (Front vs Back)
                    Text("Iconografía Principal (Símbolos):", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        designIconOptions.take(4).forEach { ic ->
                            val isSel = if (isEditingFront) frontIconState == ic else backIconState == ic
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) GameTeal else GameNavyDark)
                                    .clickable {
                                        if (isEditingFront) frontIconState = ic else backIconState = ic
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(ic) {
                                        "star" -> "⭐"
                                        "favorite" -> "❤️"
                                        "beach" -> "🏖️"
                                        "pyramid" -> "🏛️"
                                        else -> "🎟️"
                                    },
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        designIconOptions.drop(4).forEach { ic ->
                            val isSel = if (isEditingFront) frontIconState == ic else backIconState == ic
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) GameTeal else GameNavyDark)
                                    .clickable {
                                        if (isEditingFront) frontIconState = ic else backIconState = ic
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(ic) {
                                        "airplane" -> "✈️"
                                        "bus" -> "🚌"
                                        "hotel" -> "🏨"
                                        else -> "🪙"
                                    },
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dialog Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingBillTemplate = null }) {
                            Text("Cancelar", color = TextLight)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val valLong = valueText.toLongOrNull() ?: bill.denomination
                                viewModel.updateBillDesign(
                                    billId = bill.id,
                                    label = labelText,
                                    isCoin = isCoinState,
                                    frontDesignText = frontTextState,
                                    backDesignText = backTextState,
                                    patternType = patternState,
                                    colorHex = selectedColorState,
                                    frontIcon = frontIconState,
                                    backIcon = backIconState
                                )
                                viewModel.updateBillDenomination(bill.id, valLong)
                                editingBillTemplate = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(selectedColorState))
                        ) {
                            Text("Aplicar Cambios 🎨", color = GameNavyDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal to Add Player
    if (showAddPlayerDialog) {
        var playerName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(0xFF00BFA5.toInt()) }
        var selectedEmoji by remember { mutableStateOf("🌴") }

        val colorOptions = listOf(
            0xFF00BFA5.toInt(), // Teal
            0xFFFF5722.toInt(), // Terracotta
            0xFFFFD54F.toInt(), // Gold
            0xFF29B6F6.toInt(), // Sky Blue
            0xFFAB47BC.toInt(), // Purple
            0xFFEC407A.toInt()  // Pink
        )
        val emojiOptions = listOf("🌴", "📸", "🎒", "🏖️", "✈️", "🗺️", "🚌", "🌮", "🏛️")

        Dialog(onDismissRequest = { showAddPlayerDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "👤 Añadir Nuevo Turista",
                        color = TextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = playerName,
                        onValueChange = { playerName = it },
                        label = { Text("Nombre del Jugador", color = TextMuted) },
                        singleLine = true,
                        textStyle = TextStyle(color = TextLight),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GameTeal,
                            unfocusedBorderColor = GameNavyDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Selecciona Avatar:", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        emojiOptions.take(6).forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedEmoji == emoji) GameNavyDark else Color.Transparent)
                                    .border(
                                        width = if (selectedEmoji == emoji) 2.dp else 0.dp,
                                        color = if (selectedEmoji == emoji) GameTeal else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Selecciona Color:", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorOptions.forEach { colorVal ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .border(
                                        width = if (selectedColor == colorVal) 3.dp else 0.dp,
                                        color = if (selectedColor == colorVal) TextLight else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorVal }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddPlayerDialog = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (playerName.isNotBlank()) {
                                    viewModel.addSetupPlayer(playerName, selectedColor, selectedEmoji)
                                    showAddPlayerDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GameTeal)
                        ) {
                            Text("Agregar", color = GameNavyDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. DASHBOARD PRINCIPAL DE JUEGO (GAME DASHBOARD)
// ==========================================
@Composable
fun GameDashboardView(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    var selectedTab by remember { mutableIntStateOf(0) }
    var showTransferModal by remember { mutableStateOf(false) }
    var activeQrPlayer by remember { mutableStateOf<Player?>(null) }
    var showScanModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameNavyDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavySurface),
                shape = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GameGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏛️", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("BANCO CENTRAL AUTOMÁTICO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "$${String.format("%,d", state.bankBalance)}",
                                    color = GameGold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = { showScanModal = true },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = GameTeal.copy(alpha = 0.15f),
                                    contentColor = GameTeal
                                )
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Scan")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showTransferModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GameTeal),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Manual",
                                    tint = GameNavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pago", color = GameNavyDark, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(0.8f)) {
                        Text(
                            text = "👥 Carteras de Jugadores",
                            color = GameGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        WalletsGrid(
                            players = state.players,
                            onGoClick = { viewModel.payGoAround(it) },
                            onSurpriseClick = { viewModel.drawSurpriseCard(it) },
                            onQrClick = { activeQrPlayer = it }
                        )
                    }

                    Column(modifier = Modifier.weight(0.9f)) {
                        InteractiveBoardView(state = state, viewModel = viewModel)
                    }

                    Column(modifier = Modifier.weight(0.8f)) {
                        Text(
                            text = "🤖 Operaciones de Mesa IA",
                            color = GameGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AiBankerConsole(viewModel = viewModel)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "📋 Historial de Pagos",
                            color = GameGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        TransactionHistoryTable(transactions = state.transactions)
                    }
                }
            } else {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = GameNavyDark,
                    contentColor = GameTeal
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🏖️ Carteras", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("🤖 Banquero IA", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("📋 Historial", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("🏰 Tablero", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> WalletsGrid(
                            players = state.players,
                            onGoClick = { viewModel.payGoAround(it) },
                            onSurpriseClick = { viewModel.drawSurpriseCard(it) },
                            onQrClick = { activeQrPlayer = it }
                        )
                        1 -> AiBankerConsole(viewModel = viewModel)
                        2 -> TransactionHistoryTable(transactions = state.transactions)
                        3 -> InteractiveBoardView(state = state, viewModel = viewModel)
                    }
                }
            }
        }

        // Floating Action Button for Dice Mode
        FloatingActionButton(
            onClick = { viewModel.toggleDiceOverlay(true) },
            containerColor = GameGold,
            contentColor = GameNavyDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("dice_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎲 DADOS", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }

    // Modal Dice Rolling overlay
    if (state.activeDiceOverlay) {
        DiceRollingOverlay(
            state = state,
            onRoll = { playerId -> viewModel.rollDice(playerId) },
            onDismiss = { viewModel.toggleDiceOverlay(false) }
        )
    }

    // Modal Surprise Card Dialog
    state.activeDrawnCard?.let { card ->
        val activePlayer = state.players.find { it.id == state.activeDrawnPlayerId }
        Dialog(onDismissRequest = { viewModel.dismissDrawnCard() }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        when (card.category) {
                            "bonus" -> GameTeal
                            "tax" -> GameTerracotta
                            "adventure" -> GameGold
                            else -> GameBlueAccent
                        },
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                when (card.category) {
                                    "bonus" -> GameTeal.copy(alpha = 0.2f)
                                    "tax" -> GameTerracotta.copy(alpha = 0.2f)
                                    "adventure" -> GameGold.copy(alpha = 0.2f)
                                    else -> GameBlueAccent.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (card.category) {
                                "bonus" -> "🏖️"
                                "tax" -> "🏛️"
                                "adventure" -> "🗺️"
                                else -> "🎟️"
                            },
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = card.title.uppercase(),
                        color = TextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Robada por: ${activePlayer?.name ?: "Turista"}",
                        color = GameGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = card.description,
                        color = TextLight,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val isReward = card.amount > 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isReward) GameTeal.copy(alpha = 0.15f)
                                else GameTerracotta.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (card.amount == 15000L && card.id == "c9") "Recibe $1,500 de c/u"
                                   else if (card.amount == -1000L && card.id == "c4") "Paga $1,000 a c/u"
                                   else if (isReward) "+$${String.format("%,d", card.amount)}"
                                   else "-$${String.format("%,d", -card.amount)}",
                            color = if (isReward) GameTeal else GameTerracotta,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { viewModel.dismissDrawnCard() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Descartar")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { viewModel.applyDrawnCard() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isReward) GameTeal else GameTerracotta
                            ),
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Aplicar",
                                color = GameNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal QR Wallet View
    if (activeQrPlayer != null) {
        val player = activeQrPlayer!!
        Dialog(onDismissRequest = { activeQrPlayer = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(player.color), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CARTERA TURISTA QR",
                        color = GameGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = player.name,
                        color = TextLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val dotSize = 10.dp.toPx()
                            val spacing = 14.dp.toPx()
                            val numDots = (size.width / spacing).toInt()

                            fun drawFinder(x: Float, y: Float) {
                                val size = 50.dp.toPx()
                                val border = 6.dp.toPx()
                                drawRect(Color(player.color), Offset(x, y), Size(size, size))
                                drawRect(Color.White, Offset(x + border, y + border), Size(size - border * 2, size - border * 2))
                                drawRect(Color(player.color), Offset(x + border * 2, y + border * 2), Size(size - border * 4, size - border * 4))
                            }

                            drawFinder(0f, 0f)
                            drawFinder(size.width - 50.dp.toPx(), 0f)
                            drawFinder(0f, size.height - 50.dp.toPx())

                            for (row in 0 until numDots) {
                                for (col in 0 until numDots) {
                                    if (row < 4 && col < 4) continue
                                    if (row < 4 && col > numDots - 5) continue
                                    if (row > numDots - 5 && col < 4) continue

                                    if ((row * col + row + col) % 3 == 0 || (row + col) % 5 == 0) {
                                        drawRoundRect(
                                            color = Color(player.color),
                                            topLeft = Offset(col * spacing, row * spacing),
                                            size = Size(dotSize, dotSize),
                                            cornerRadius = CornerRadius(2.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Saldo: $${String.format("%,d", player.balance)}",
                        color = Color(player.color),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Escanea este código para realizar un cobro o transferencia inmediata.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { activeQrPlayer = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(player.color)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar", color = GameNavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Manual Pay/Charge Transaction Form
    if (showTransferModal) {
        var selectedFromIndex by remember { mutableIntStateOf(0) }
        var selectedToIndex by remember { mutableIntStateOf(1) }
        var inputAmount by remember { mutableStateOf("") }
        var inputConcept by remember { mutableStateOf("") }

        val transferOptions = listOf("Banco Central 🏛️") + state.players.map { "${it.avatarEmoji} ${it.name}" }

        Dialog(onDismissRequest = { showTransferModal = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "💸 Transferencia Manual",
                        color = TextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("De (Paga):", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    DropdownSelector(
                        options = transferOptions,
                        selectedIndex = selectedFromIndex,
                        onSelectionChange = { selectedFromIndex = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Para (Recibe):", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    DropdownSelector(
                        options = transferOptions,
                        selectedIndex = selectedToIndex,
                        onSelectionChange = { selectedToIndex = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Monto a Transferir ($):", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it.filter { c -> c.isDigit() } },
                        textStyle = TextStyle(color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Black),
                        placeholder = { Text("0", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GameTeal,
                            unfocusedBorderColor = GameNavyDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Accesos Rápidos de Billetes:", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.bills.take(4).forEach { bill ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(bill.colorHex).copy(alpha = 0.15f))
                                    .border(1.dp, Color(bill.colorHex), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cur = inputAmount.toLongOrNull() ?: 0L
                                        inputAmount = (cur + bill.denomination).toString()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$${bill.denomination / 1000}k",
                                    color = Color(bill.colorHex),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = inputConcept,
                        onValueChange = { inputConcept = it },
                        label = { Text("Concepto", color = TextMuted) },
                        textStyle = TextStyle(color = TextLight),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GameTeal,
                            unfocusedBorderColor = GameNavyDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTransferModal = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val amountVal = inputAmount.toLongOrNull() ?: 0L
                                if (amountVal > 0 && selectedFromIndex != selectedToIndex) {
                                    val fromId = if (selectedFromIndex == 0) null else state.players[selectedFromIndex - 1].id
                                    val toId = if (selectedToIndex == 0) null else state.players[selectedToIndex - 1].id
                                    val con = inputConcept.ifBlank { "Transferencia Manual 💸" }

                                    val ok = viewModel.performTransaction(
                                        fromPlayerId = fromId,
                                        toPlayerId = toId,
                                        amount = amountVal,
                                        concept = con
                                    )
                                    if (ok) {
                                        showTransferModal = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GameTeal)
                        ) {
                            Text("Transferir", color = GameNavyDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Scan QR/NFC Simulator
    if (showScanModal) {
        var scannerStep by remember { mutableIntStateOf(0) }
        var scannedPlayer by remember { mutableStateOf<Player?>(null) }
        var scanAmount by remember { mutableStateOf("") }

        LaunchedEffect(scannerStep) {
            if (scannerStep == 0) {
                delay(1500)
                scannedPlayer = state.players.randomOrNull()
                scannerStep = 1
            }
        }

        Dialog(onDismissRequest = { showScanModal = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (scannerStep == 0) {
                        Text(
                            text = "📷 ESCANEANDO CARTERA...",
                            color = GameTeal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .border(2.dp, GameTeal, RoundedCornerShape(12.dp))
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "scan")
                            val lineY by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 220f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1800, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "lineY"
                            )

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = GameTeal,
                                    start = Offset(0f, lineY),
                                    end = Offset(size.width, lineY),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }
                            Text("Apunta al código QR del celular", color = TextMuted, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Simulando conexión inalámbrica cifrada...",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "✅ CARTERA VINCULADA",
                            color = GameTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = scannedPlayer?.name ?: "Turista",
                            color = TextLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Saldo Actual: $${String.format("%,d", scannedPlayer?.balance ?: 0L)}",
                            color = scannedPlayer?.getComposeColor() ?: GameTeal,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = scanAmount,
                            onValueChange = { scanAmount = it.filter { c -> c.isDigit() } },
                            label = { Text("Monto a Transferir", color = TextMuted) },
                            textStyle = TextStyle(color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GameTeal,
                                unfocusedBorderColor = GameNavyDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = { showScanModal = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", color = TextMuted)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    val amt = scanAmount.toLongOrNull() ?: 0L
                                    val targetPlayer = scannedPlayer
                                    if (amt > 0 && targetPlayer != null) {
                                        val ok = viewModel.performTransaction(
                                            fromPlayerId = null,
                                            toPlayerId = targetPlayer.id,
                                            amount = amt,
                                            concept = "Pago por Transferencia QR 📷"
                                        )
                                        if (ok) {
                                            showScanModal = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GameTeal),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text("Pagar al Instante", color = GameNavyDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB COMPONENT: WALLETS GRID ---
@Composable
fun WalletsGrid(
    players: List<Player>,
    onGoClick: (String) -> Unit,
    onSurpriseClick: (String) -> Unit,
    onQrClick: (Player) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(players) { player ->
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(player.color).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(player.color).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(player.avatarEmoji, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = player.name,
                                color = TextLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 80.dp)
                            )
                        }

                        IconButton(
                            onClick = { onQrClick(player) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("📱", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$${String.format("%,d", player.balance)}",
                        color = Color(player.color),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { onGoClick(player.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = GameNavyDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🏁 +20k", color = GameTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onSurpriseClick(player.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = GameNavyDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎟️ Carta", color = GameTerracotta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB COMPONENT: AI BANKER CONSOLE ---
@Composable
fun AiBankerConsole(viewModel: GameViewModel) {
    var aiPrompt by remember { mutableStateOf("") }
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiResult by viewModel.aiResult.collectAsStateWithLifecycle()

    val suggestions = listOf(
        "Pagar vuelta por salida a Turista Juan",
        "Turista Sofía paga multa de aduana de $4,000",
        "Turista Beto paga renta a Turista Juan de $5,000 por Cancún",
        "Paga $6,000 a Turista Sofía por seguro de viaje"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = GameNavyCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🤖 Banquero Inteligente Automático",
                color = GameTeal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Describe la acción en lenguaje natural. El banco procesará las reglas, debitará/acreditará y reproducirá el sonido.",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = aiPrompt,
                onValueChange = { aiPrompt = it },
                placeholder = { Text("Ej: Paga $20,000 a Juan por vuelta...", color = TextMuted, fontSize = 13.sp) },
                textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                singleLine = false,
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GameTeal,
                    unfocusedBorderColor = GameNavyDark
                ),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    Text("🎙️", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Comandos sugeridos:", color = TextMuted, fontSize = 10.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.forEach { sug ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GameNavyDark)
                            .clickable { aiPrompt = sug }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(sug, color = GameTeal, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (aiPrompt.isNotBlank() && !isAiLoading) {
                        viewModel.sendAiCommand(aiPrompt)
                        aiPrompt = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GameTeal),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isAiLoading) {
                    CircularProgressIndicator(
                        color = GameNavyDark,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Procesando Reglas IA...", color = GameNavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else {
                    Text("✨ Ejecutar con Inteligencia Artificial", color = GameNavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            aiResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                        .background(GameNavyDark)
                        .border(1.dp, GameGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🤖", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = result,
                                color = TextLight,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Limpiar aviso",
                                color = GameGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { viewModel.clearAiResult() }
                                    .padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SUB COMPONENT: TRANSACTION HISTORY TABLE ---
@Composable
fun TransactionHistoryTable(transactions: List<GameTransaction>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GameNavyCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No se han realizado pagos en la partida aún.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hora", color = GameGold, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        Text("De ➡️ Para", color = GameGold, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.8f))
                        Text("Monto", color = GameGold, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = GameNavyDark, thickness = 2.dp)
                }

                items(transactions) { tx ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tx.timestamp,
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(0.7f)
                            )
                            Column(modifier = Modifier.weight(1.8f)) {
                                Text(
                                    text = "${tx.fromPlayerName} ➡️ ${tx.toPlayerName}",
                                    color = TextLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tx.concept,
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "$${String.format("%,d", tx.amount)}",
                                color = if (tx.toPlayerId == null) GameTerracotta else GameTeal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider(color = GameNavyDark.copy(alpha = 0.5f), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

// --- DROPDOWN SELECTOR HELPER ---
@Composable
fun DropdownSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GameNavyDark)
            .border(1.dp, GameNavyCard, RoundedCornerShape(10.dp))
            .clickable { expanded = true }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = options[selectedIndex], color = TextLight, fontSize = 13.sp)
            Text(if (expanded) "▲" else "▼", color = TextMuted, fontSize = 10.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(GameNavyCard)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, color = TextLight, fontSize = 13.sp) },
                    onClick = {
                        onSelectionChange(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ==========================================
// 3. SECCIÓN DE DISEÑO Y COMPONENTES CUSTOM
// ==========================================

@Composable
fun BillDesignCanvas(
    bill: BillTemplate,
    isCoin: Boolean,
    label: String,
    denomination: Long,
    colorHex: Int,
    frontText: String,
    backText: String,
    pattern: String,
    frontIcon: String,
    backIcon: String,
    isFront: Boolean
) {
    val mainColor = Color(colorHex)
    val textToShow = if (isFront) frontText else backText
    val iconToShow = if (isFront) frontIcon else backIcon

    val emojiIcon = when (iconToShow) {
        "star" -> "⭐"
        "favorite" -> "❤️"
        "beach" -> "🏖️"
        "pyramid" -> "🏛️"
        "airplane" -> "✈️"
        "bus" -> "🚌"
        "hotel" -> "🏨"
        "coin" -> "🪙"
        else -> "💵"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GameNavyDark)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isCoin) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(mainColor.copy(alpha = 0.9f), mainColor.copy(alpha = 0.4f))
                        )
                    )
                    .border(4.dp, mainColor, CircleShape)
                    .border(6.dp, mainColor.copy(alpha = 0.3f), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = textToShow.uppercase(),
                        color = TextLight,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = emojiIcon,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$$denomination",
                        color = GameGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(mainColor.copy(alpha = 0.15f))
                    .border(2.dp, mainColor, RoundedCornerShape(8.dp))
                    .drawBehind {
                        when (pattern) {
                            "stars" -> {
                                for (i in 1..8) {
                                    drawCircle(
                                        color = mainColor.copy(alpha = 0.2f),
                                        radius = 4f,
                                        center = Offset(
                                            x = (i * 45f) % size.width,
                                            y = (i * 30f) % size.height
                                        )
                                    )
                                }
                            }
                            "stripes" -> {
                                val strokeWidth = 2f
                                val spacing = 20f
                                var x = 0f
                                while (x < size.width + size.height) {
                                    drawLine(
                                        color = mainColor.copy(alpha = 0.1f),
                                        start = Offset(x, 0f),
                                        end = Offset(x - size.height, size.height),
                                        strokeWidth = strokeWidth
                                    )
                                    x += spacing
                                }
                            }
                            "retro" -> {
                                val spacing = 15f
                                var x = 5f
                                while (x < size.width) {
                                    var y = 5f
                                    while (y < size.height) {
                                        drawCircle(
                                            color = mainColor.copy(alpha = 0.15f),
                                            radius = 2f,
                                            center = Offset(x, y)
                                        )
                                        y += spacing
                                    }
                                    x += spacing
                                }
                            }
                            else -> {
                                drawRect(
                                    color = mainColor.copy(alpha = 0.3f),
                                    topLeft = Offset(8f, 8f),
                                    size = Size(size.width - 16f, size.height - 16f),
                                    style = Stroke(width = 1f)
                                )
                            }
                        }
                    }
                    .padding(12.dp)
            ) {
                Text(
                    text = "$$denomination",
                    color = mainColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                Text(
                    text = "$$denomination",
                    color = mainColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                Text(
                    text = label.uppercase(),
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.BottomStart)
                )

                Text(
                    text = textToShow.uppercase(),
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(mainColor.copy(alpha = 0.2f))
                        .border(1.dp, mainColor, CircleShape)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = emojiIcon,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "$$denomination",
                            color = TextLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiceRollingOverlay(
    state: GameState,
    onRoll: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPlayerId by remember { mutableStateOf(state.players.firstOrNull()?.id ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GameNavyCard),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GameGold, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎲 LANZAMIENTO DADOS",
                        color = GameGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Selecciona el jugador activo:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.players.forEach { p ->
                        val isSelected = p.id == selectedPlayerId
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(p.color).copy(alpha = 0.25f) else GameNavyDark)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(p.color) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedPlayerId = p.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.avatarEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                p.name,
                                color = if (isSelected) Color(p.color) else TextLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DieView(value = state.dice1, isRolling = state.isDiceRolling)
                    DieView(value = state.dice2, isRolling = state.isDiceRolling)
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!state.isDiceRolling) {
                    val total = state.dice1 + state.dice2
                    Text(
                        text = "Suma Rolada: $total 🌟",
                        color = TextLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    if (state.dice1 == state.dice2) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¡DOBLES! (${state.consecutiveDoubles} consecutivos) ⚄⚄",
                            color = GameTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Girando dados...",
                        color = GameTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onRoll(selectedPlayerId) },
                    enabled = !state.isDiceRolling && selectedPlayerId.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameGold,
                        disabledContainerColor = GameNavyDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        "TIRAR DADOS 🎲",
                        color = if (state.isDiceRolling) TextMuted else GameNavyDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DieView(value: Int, isRolling: Boolean) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isRolling) GameTeal.copy(alpha = 0.2f) else GameNavyDark)
            .border(2.dp, if (isRolling) GameTeal else GameGold, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isRolling) {
            CircularProgressIndicator(
                color = GameTeal,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = when (value) {
                    1 -> "⚀"
                    2 -> "⚁"
                    3 -> "⚂"
                    4 -> "⚃"
                    5 -> "⚄"
                    6 -> "⚅"
                    else -> "⚀"
                },
                color = GameGold,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InteractiveBoardView(
    state: GameState,
    viewModel: GameViewModel
) {
    var selectedProperty by remember { mutableStateOf<BoardProperty?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "🏰 Propiedades del Tablero",
            color = GameGold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val grouped = state.properties.groupBy { it.groupName }
            grouped.forEach { (zone, props) ->
                item {
                    Text(
                        text = "📍 $zone",
                        color = GameTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(props) { prop ->
                    val owner = state.players.find { it.id == prop.ownerId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProperty = prop }
                            .border(
                                width = 1.dp,
                                color = if (prop.isMortgaged) GameTerracotta.copy(alpha = 0.5f) else Color(prop.groupColor).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp, 40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(prop.groupColor))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = prop.name,
                                        color = TextLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (prop.isMortgaged) "HIPOTECADA 🏦" else "Valor: $${String.format("%,d", prop.cost)}",
                                            color = if (prop.isMortgaged) GameTerracotta else TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (prop.numHouses > 0 && !prop.isMortgaged) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (prop.numHouses == 5) "🏰 Castillo" else "🏡 x${prop.numHouses}",
                                                color = GameGold,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            if (owner != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(owner.color).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(owner.avatarEmoji, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        owner.name,
                                        color = Color(owner.color),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 60.dp)
                                    )
                                }
                            } else {
                                Text(
                                    "BANCARIO 🏦",
                                    color = GameTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GameTeal.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedProperty?.let { currentProp ->
        val prop = state.properties.find { it.id == currentProp.id } ?: currentProp
        val owner = state.players.find { it.id == prop.ownerId }

        var showAuctionSection by remember { mutableStateOf(false) }
        var auctionWinnerId by remember { mutableStateOf(state.players.firstOrNull()?.id ?: "") }
        var auctionBidText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { selectedProperty = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(prop.groupColor), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(prop.groupColor))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = prop.groupName.uppercase(),
                                color = GameNavyDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = prop.name,
                                color = GameNavyDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("TABLA DE ALQUILERES 💰", color = GameGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GameNavyDark)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RentRow(label = "Alquiler Base", value = prop.baseRent, isCurrent = prop.numHouses == 0 && prop.ownerId != null)
                        RentRow(label = "Con 1 Casa 🏡", value = prop.rentWith1House, isCurrent = prop.numHouses == 1)
                        RentRow(label = "Con 2 Casas 🏡🏡", value = prop.rentWith2Houses, isCurrent = prop.numHouses == 2)
                        RentRow(label = "Con 3 Casas 🏡🏡🏡", value = prop.rentWith3Houses, isCurrent = prop.numHouses == 3)
                        RentRow(label = "Con 4 Casas 🏡🏡🏡🏡", value = prop.rentWith4Houses, isCurrent = prop.numHouses == 4)
                        RentRow(label = "Con Castillo 🏰", value = prop.rentWithCastle, isCurrent = prop.numHouses == 5)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Valor Hipotecario: $${String.format("%,d", prop.mortgageValue)}", color = TextMuted, fontSize = 11.sp)
                        Text("Costo Construcción: $${String.format("%,d", prop.houseCost)}", color = TextMuted, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = GameNavyDark)

                    Spacer(modifier = Modifier.height(12.dp))

                    if (owner == null) {
                        Text("PROPIEDAD DISPONIBLE 🏦", color = GameTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Nadie posee esta propiedad aún. Puedes comprarla directamente o subastarla.", color = TextMuted, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!showAuctionSection) {
                            Text("Comprar directamente para:", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.players.forEach { p ->
                                    Button(
                                        onClick = {
                                            viewModel.buyProperty(p.id, prop.id)
                                            selectedProperty = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(p.color)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("${p.avatarEmoji} ${p.name}", color = GameNavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showAuctionSection = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GameNavyDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🔨 Abrir Subasta Pública", color = GameGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("SUBASTA PÚBLICA 🔨", color = GameGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Ganador de la Subasta:", color = TextMuted, fontSize = 11.sp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.players.forEach { p ->
                                    val isSel = p.id == auctionWinnerId
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) Color(p.color).copy(alpha = 0.25f) else GameNavyDark)
                                            .border(1.dp, if (isSel) Color(p.color) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { auctionWinnerId = p.id }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text("${p.avatarEmoji} ${p.name}", color = if (isSel) Color(p.color) else TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = auctionBidText,
                                onValueChange = { auctionBidText = it.filter { c -> c.isDigit() } },
                                label = { Text("Oferta Final ($)", color = TextMuted) },
                                textStyle = TextStyle(color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GameGold, unfocusedBorderColor = GameNavyDark),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { showAuctionSection = false }, modifier = Modifier.weight(1f)) {
                                    Text("Volver", color = TextMuted)
                                }
                                Button(
                                    onClick = {
                                        val bid = auctionBidText.toLongOrNull() ?: 0L
                                        if (bid > 0 && auctionWinnerId.isNotEmpty()) {
                                            viewModel.auctionProperty(prop.id, auctionWinnerId, bid)
                                            selectedProperty = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GameGold),
                                    modifier = Modifier.weight(1.5f)
                                ) {
                                    Text("Confirmar Venta 🔨", color = GameNavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(owner.color).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(owner.avatarEmoji, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dueño actual: ${owner.name}",
                                color = Color(owner.color),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!prop.isMortgaged) {
                            Text("Cobrar Alquiler a un Visitante:", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.players.filter { it.id != owner.id }.forEach { p ->
                                    Button(
                                        onClick = {
                                            viewModel.payRent(p.id, prop.id)
                                            selectedProperty = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GameNavyDark),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Cobrar a ${p.name}", color = GameTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("Construcción 🛠️", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Nivel actual: ${if (prop.numHouses == 5) "Castillo" else "${prop.numHouses} Casas"}", color = TextMuted, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.buildOnProperty(prop.id) },
                                    enabled = prop.numHouses < 5,
                                    colors = ButtonDefaults.buttonColors(containerColor = GameTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Construir (+\$${prop.houseCost})", color = GameNavyDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Hipoteca Bancaria 🏦", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (prop.isMortgaged) "Recuperar (+\$${(prop.mortgageValue * 1.1).toLong()})" else "Hipotecar (Obtienes \$${prop.mortgageValue})",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = { viewModel.toggleMortgage(prop.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (prop.isMortgaged) GameTeal else GameTerracotta),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (prop.isMortgaged) "Recuperar 🔓" else "Hipotecar 🏦",
                                    color = GameNavyDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { selectedProperty = null },
                        colors = ButtonDefaults.buttonColors(containerColor = GameNavyDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar Detalle", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RentRow(label: String, value: Long, isCurrent: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isCurrent) GameTeal.copy(alpha = 0.2f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isCurrent) GameTeal else TextLight,
            fontSize = 11.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = "$${String.format("%,d", value)}",
            color = if (isCurrent) GameTeal else GameGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==========================================
// 4. PORTAL DE ACCESO Y SELECTOR DE PRESETS ARKAIOS
// ==========================================

@Composable
fun GameLoginView(viewModel: GameViewModel) {
    var email by remember { mutableStateOf("admin@arkaios.net") }
    var password by remember { mutableStateOf("tomorrowland2026") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var authTerminalText by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GameNavyDark, Color(0xFF0F172A), GameNavyDark)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Pulsing Holographic Core
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(2.dp, GameTeal, CircleShape)
                    .drawBehind {
                        drawCircle(
                            color = GameTeal.copy(alpha = 0.15f),
                            radius = size.width / 2 + 15f,
                            style = Stroke(width = 2f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = GameGold,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PORTAL ARKAIOS",
                color = GameGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Text(
                text = "Ecosistema de Entretenimiento de Próxima Generación",
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (!isAuthenticating) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GameNavyCard, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "ACCESO SEGURO FIREBASE",
                            color = GameTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo del Agente", color = TextMuted) },
                            singleLine = true,
                            textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GameTeal,
                                unfocusedBorderColor = GameNavyDark,
                                focusedContainerColor = GameNavyDark,
                                unfocusedContainerColor = GameNavyDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña de Enlace", color = TextMuted) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GameTeal,
                                unfocusedBorderColor = GameNavyDark,
                                focusedContainerColor = GameNavyDark,
                                unfocusedContainerColor = GameNavyDark
                            ),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(if (passwordVisible) "👁️" else "🙈", fontSize = 12.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                isAuthenticating = true
                                scope.launch {
                                    authTerminalText = listOf("🤖 Iniciando Handshake Arkaios Core...")
                                    delay(400)
                                    authTerminalText = authTerminalText + "🔥 Conectando con Firebase Auth..."
                                    delay(400)
                                    try {
                                        val auth = FirebaseAuth.getInstance()
                                        authTerminalText = authTerminalText + "🔐 Intentando iniciar sesión para: $email..."
                                        delay(400)
                                        auth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val user = auth.currentUser
                                                    authTerminalText = authTerminalText + "✅ Sesión Autorizada con Éxito!"
                                                    authTerminalText = authTerminalText + "👤 UID: ${user?.uid}"
                                                    authTerminalText = authTerminalText + "📧 Email: ${user?.email}"
                                                    scope.launch {
                                                        delay(1000)
                                                        viewModel.navigateTo("preset_selection")
                                                    }
                                                } else {
                                                    val exception = task.exception
                                                    authTerminalText = authTerminalText + "⚠️ Acceso denegado: ${exception?.localizedMessage}"
                                                    authTerminalText = authTerminalText + "⚡ Intentando registrar nuevo agente..."
                                                    
                                                    auth.createUserWithEmailAndPassword(email, password)
                                                        .addOnCompleteListener { regTask ->
                                                            if (regTask.isSuccessful) {
                                                                val user = auth.currentUser
                                                                authTerminalText = authTerminalText + "🎉 ¡Cuenta de Agente Creada con Éxito!"
                                                                authTerminalText = authTerminalText + "👤 UID Nuevo: ${user?.uid}"
                                                                scope.launch {
                                                                    delay(1500)
                                                                    viewModel.navigateTo("preset_selection")
                                                                }
                                                            } else {
                                                                authTerminalText = authTerminalText + "❌ Registro fallido: ${regTask.exception?.localizedMessage}"
                                                                scope.launch {
                                                                    delay(3000)
                                                                    isAuthenticating = false
                                                                }
                                                            }
                                                        }
                                                }
                                            }
                                    } catch (e: Exception) {
                                        authTerminalText = authTerminalText + "❌ Error del sistema Firebase: ${e.localizedMessage}"
                                        scope.launch {
                                            delay(3000)
                                            isAuthenticating = false
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GameTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "INICIAR SESIÓN CON FIREBASE 🔒",
                                color = GameNavyDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Google Sign In button representation
                        Button(
                            onClick = {
                                isAuthenticating = true
                                scope.launch {
                                    authTerminalText = listOf("🌐 Conectando con Google Play Services...")
                                    delay(400)
                                    authTerminalText = authTerminalText + "🔑 Intercambiando tokens OAuth2..."
                                    delay(400)
                                    authTerminalText = authTerminalText + "🔥 Iniciando sesión de Google en Firebase..."
                                    try {
                                        val auth = FirebaseAuth.getInstance()
                                        auth.signInAnonymously()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val user = auth.currentUser
                                                    authTerminalText = authTerminalText + "✅ Enlace de Google Autorizado!"
                                                    authTerminalText = authTerminalText + "👤 UID Anónimo: ${user?.uid}"
                                                    scope.launch {
                                                        delay(1000)
                                                        viewModel.navigateTo("preset_selection")
                                                    }
                                                } else {
                                                    val errorMsg = task.exception?.localizedMessage ?: "Error desconocido"
                                                    authTerminalText = authTerminalText + "❌ Falló Enlace de Google: $errorMsg"
                                                    authTerminalText = authTerminalText + "💡 Consejo: Activa 'Anonymous Authentication' en Firebase Console o ingresa en Modo Offline."
                                                }
                                            }
                                    } catch (e: Exception) {
                                        authTerminalText = authTerminalText + "❌ Error de inicialización: ${e.localizedMessage}"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🔴🔵🟢🟡 ", fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "INICIAR SESIÓN CON GOOGLE",
                                    color = Color(0xFF1D1B20),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Guest / Offline Mode Bypass Button
                        Button(
                            onClick = {
                                viewModel.navigateTo("preset_selection")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(1.dp, GameTeal.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🎮 ", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "MODO INVITADO (SIN CONEXIÓN)",
                                    color = GameTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Interactive loading terminal
                Card(
                    colors = CardDefaults.cardColors(containerColor = GameNavyDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .border(1.dp, GameTeal, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "ARKAIOS_CORE TERMINAL_V1.0",
                                color = GameTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            authTerminalText.forEach { line ->
                                Text(
                                    text = line,
                                    color = TextLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        val hasError = authTerminalText.any { it.contains("❌") || it.contains("⚠️") }
                        if (hasError) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { isAuthenticating = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("REGRESAR ↩️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { 
                                        isAuthenticating = false
                                        viewModel.navigateTo("preset_selection") 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GameTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Text("OMITIR (OFFLINE) 🎮", color = GameNavyDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = GameTeal,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Estableciendo conexión en la nube...",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "© 2026 Arkaios Entertainment Group. Todos los derechos reservados.",
                color = TextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GamePresetSelectionView(viewModel: GameViewModel) {
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GameNavyDark, Color(0xFF0F172A))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CONFIGURACIÓN DE ENTORNOS",
                color = GameGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = "Selecciona el Simulador Turista",
                color = TextLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Carga presets dinámicos desde la red central de juegos de mesa Arkaios.",
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Airtight Hermetic Expandable Button Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isMenuExpanded = !isMenuExpanded }
                    .border(
                        2.dp,
                        if (isMenuExpanded) GameTeal else GameNavyCard,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (selectedMode) {
                                    "disney" -> "🏰"
                                    "classic" -> "🌴"
                                    "galactic" -> "🚀"
                                    "world" -> "🌍"
                                    else -> "🎮"
                                },
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SIMULADOR ACTIVO",
                                    color = GameGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = when (selectedMode) {
                                        "disney" -> "Turista Disney Park"
                                        "classic" -> "Turista México Clásico"
                                        "galactic" -> "Turista Galáctico Cósmico"
                                        "world" -> "Turista Mundial Global"
                                        else -> "Personalizado"
                                    },
                                    color = TextLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Expand indicator icon
                        Text(
                            text = if (isMenuExpanded) "▲ Cerrar" else "▼ Seleccionar",
                            color = GameTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Hidden list section inside the hermetic capsule
                    AnimatedVisibility(
                        visible = isMenuExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = GameNavyDark)

                            // 1. Disney
                            PresetDropdownItem(
                                title = "Turista Disney Park 🏰",
                                subtitle = "Fantasyland, Tomorrowland, Orejitas de Mickey. Estilo Mágico.",
                                isSelected = selectedMode == "disney",
                                onClick = {
                                    viewModel.selectGameMode("disney")
                                    isMenuExpanded = false
                                }
                            )

                            // 2. Classic
                            PresetDropdownItem(
                                title = "Turista México Clásico 🌴",
                                subtitle = "Cancún, Los Cabos, Acapulco, Metrópolis Centrales.",
                                isSelected = selectedMode == "classic",
                                onClick = {
                                    viewModel.selectGameMode("classic")
                                    isMenuExpanded = false
                                }
                            )

                            // 3. Galactic
                            PresetDropdownItem(
                                title = "Turista Galáctico Cósmico 🚀",
                                subtitle = "Estaciones lunares, peajes espaciales, créditos intergalácticos.",
                                isSelected = selectedMode == "galactic",
                                onClick = {
                                    viewModel.selectGameMode("galactic")
                                    isMenuExpanded = false
                                }
                            )

                            // 4. World
                            PresetDropdownItem(
                                title = "Turista Mundial Global 🌍",
                                subtitle = "Roma, Tokio Shibuya, Torre Eiffel, euros de oro y visas globales.",
                                isSelected = selectedMode == "world",
                                onClick = {
                                    viewModel.selectGameMode("world")
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Details Panel of Selected Preset
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GameNavyCard, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FICHA TÉCNICA DEL MAPA",
                        color = GameGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Capital Inicial", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = when (selectedMode) {
                                    "disney" -> "$134,500"
                                    "classic" -> "$100,000"
                                    "galactic" -> "$180,000"
                                    "world" -> "$150,000"
                                    else -> "$100,000"
                                },
                                color = TextLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Fianza Salida Prisión", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = when (selectedMode) {
                                    "disney" -> "$5,000"
                                    "classic" -> "$5,000"
                                    "galactic" -> "$8,000"
                                    "world" -> "$6,000"
                                    else -> "$5,000"
                                },
                                color = TextLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Propiedades Principales:", color = TextMuted, fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        when (selectedMode) {
                            "disney" -> {
                                TextBadge("🏰 Mansión Embrujada", color = Color(0xFF8B5A2B))
                                TextBadge("🚀 Tomorrowland Ride", color = Color(0xFF2196F3))
                                TextBadge("🦁 Safari del Río", color = Color(0xFF4CAF50))
                            }
                            "classic" -> {
                                TextBadge("🏖️ Cancún", color = Color(0xFF00796B))
                                TextBadge("🌆 Guadalajara", color = Color(0xFF3F51B5))
                                TextBadge("🌇 CDMX Centro", color = Color(0xFF3F51B5))
                            }
                            "galactic" -> {
                                TextBadge("🛰️ Estación Lunar", color = Color(0xFF8B5A2B))
                                TextBadge("🪐 Saturno Anillos", color = Color(0xFF2196F3))
                                TextBadge("⭐️ Betelgeuse", color = Color(0xFF4CAF50))
                            }
                            "world" -> {
                                TextBadge("🏛️ Roma Coliseo", color = Color(0xFF00796B))
                                TextBadge("🗼 Torre Eiffel", color = Color(0xFF00796B))
                                TextBadge("🍣 Tokio Shibuya", color = Color(0xFF3F51B5))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Billetes y Fichas:", color = TextMuted, fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        when (selectedMode) {
                            "disney" -> {
                                Text("🪙 Moneda Oro ($500) | Boletos Fantasyland & Tomorrowland", color = TextLight, fontSize = 11.sp)
                            }
                            "classic" -> {
                                Text("🪙 Moneda Oro ($500) | Bonos Playa, Avión y Hoteles", color = TextLight, fontSize = 11.sp)
                            }
                            "galactic" -> {
                                Text("🪙 Crédito Galáctico ($500) | Bonos Nova, Fuel y Alien Pass", color = TextLight, fontSize = 11.sp)
                            }
                            "world" -> {
                                Text("🪙 Euro de Oro ($500) | Pases Eurostar, Vuelo Transatlántico", color = TextLight, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sync matrix visual readouts
            Card(
                colors = CardDefaults.cardColors(containerColor = GameNavyDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GameNavyCard, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "CONECTIVIDAD MULTIPLATAFORMA ARKAIOS HUB",
                        color = GameTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeviceSyncBadge("📱 Smartphones (Billeteras)", isSynced = true)
                        DeviceSyncBadge("💻 Web (Editor Canvas)", isSynced = true)
                        DeviceSyncBadge("📺 Tablet (Tablero 4D)", isSynced = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.navigateTo("setup") },
                colors = ButtonDefaults.buttonColors(containerColor = GameGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    "DESPLEGAR JUEGO EN TABLET ⚙️",
                    color = GameNavyDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PresetDropdownItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) GameTeal.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) GameTeal else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isSelected) GameTeal else TextLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
        if (isSelected) {
            Text("✅", fontSize = 14.sp)
        }
    }
}

@Composable
fun TextBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(text = text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DeviceSyncBadge(label: String, isSynced: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSynced) GameTeal.copy(alpha = 0.1f) else GameNavyDark)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(if (isSynced) "🟢" else "🔴", fontSize = 8.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = if (isSynced) TextLight else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}


