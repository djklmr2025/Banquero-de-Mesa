package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.min

// --- CREATOR STUDIO: DISEÑADOR VISUAL DE BILLETES ---
// Pantalla "bills_config". Lee/escribe directamente sobre viewModel.billTemplates,
// usando addBillTemplate / updateBillDenomination / updateBillDesign / removeBillTemplate.

private val PRESET_COLORS = listOf(
    0xFFFFD54F.toInt(), 0xFF43A047.toInt(), 0xFF00ACC1.toInt(), 0xFF29B6F6.toInt(),
    0xFF8E24AA.toInt(), 0xFFFF5722.toInt(), 0xFFE53935.toInt(), 0xFF3949AB.toInt(),
    0xFF00E676.toInt(), 0xFFFFD600.toInt()
)

private val ICON_OPTIONS = listOf("star", "favorite", "beach", "pyramid", "airplane", "hotel", "bus", "coin", "ticket")

private fun iconFor(name: String): ImageVector = when (name) {
    "star" -> Icons.Filled.Star
    "favorite" -> Icons.Filled.Favorite
    "beach" -> Icons.Filled.Landscape
    "pyramid" -> Icons.Filled.Terrain
    "airplane" -> Icons.Filled.Flight
    "hotel" -> Icons.Filled.Hotel
    "bus" -> Icons.Filled.DirectionsBus
    "coin" -> Icons.Filled.MonetizationOn
    "ticket" -> Icons.Filled.ConfirmationNumber
    else -> Icons.Filled.Star
}

@Composable
fun GameBillsConfigView(viewModel: GameViewModel) {
    val bills by viewModel.billTemplates.collectAsStateWithLifecycle()
    var editingId by remember { mutableStateOf<String?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Diseñador de Billetes",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF21005D)
        )
        Text(
            text = "Toca un billete para editarlo, o crea uno nuevo.",
            fontSize = 13.sp,
            color = Color(0xFF49454F)
        )

        Spacer(Modifier.height(16.dp))

        // --- Galería de billetes existentes ---
        bills.forEach { bill ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { editingId = bill.id; isCreatingNew = false },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BillCanvasPreview(bill = bill, modifier = Modifier.size(width = 96.dp, height = 60.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(bill.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("$${bill.denomination}", fontSize = 12.sp, color = Color(0xFF49454F))
                }
                IconButton(onClick = { editingId = bill.id; isCreatingNew = false }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color(0xFF6750A4))
                }
                IconButton(onClick = { viewModel.removeBillTemplate(bill.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color(0xFFB3261E))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { isCreatingNew = true; editingId = null },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Nuevo billete")
        }

        Spacer(Modifier.height(20.dp))

        // --- Formulario de edición / creación ---
        val bankBeingEdited = bills.find { it.id == editingId }
        if (isCreatingNew || bankBeingEdited != null) {
            BillEditorForm(
                viewModel = viewModel,
                existing = bankBeingEdited,
                onDone = { editingId = null; isCreatingNew = false }
            )
        }
    }
}

@Composable
private fun BillEditorForm(
    viewModel: GameViewModel,
    existing: BillTemplate?,
    onDone: () -> Unit
) {
    var denominationText by remember(existing?.id) { mutableStateOf((existing?.denomination ?: 1000L).toString()) }
    var label by remember(existing?.id) { mutableStateOf(existing?.label ?: "Nuevo Billete") }
    var frontText by remember(existing?.id) { mutableStateOf(existing?.frontDesignText ?: "REPÚBLICA TURISTA") }
    var backText by remember(existing?.id) { mutableStateOf(existing?.backDesignText ?: "BANCO CENTRAL") }
    var isCoin by remember(existing?.id) { mutableStateOf(existing?.isCoin ?: false) }
    var colorHex by remember(existing?.id) { mutableStateOf(existing?.colorHex ?: PRESET_COLORS.first()) }
    var patternType by remember(existing?.id) { mutableStateOf(existing?.patternType ?: "classic") }
    var frontIcon by remember(existing?.id) { mutableStateOf(existing?.frontIcon ?: "star") }
    var backIcon by remember(existing?.id) { mutableStateOf(existing?.backIcon ?: "favorite") }

    val previewBill = BillTemplate(
        id = existing?.id ?: "preview",
        denomination = denominationText.toLongOrNull() ?: 0L,
        colorHex = colorHex,
        iconName = frontIcon,
        label = label,
        isCoin = isCoin,
        frontDesignText = frontText,
        backDesignText = backText,
        patternType = patternType,
        frontIcon = frontIcon,
        backIcon = backIcon
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3EDF7))
            .padding(16.dp)
    ) {
        Text("Vista previa", fontSize = 13.sp, color = Color(0xFF49454F))
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BillCanvasPreview(bill = previewBill, modifier = Modifier.size(width = 220.dp, height = 130.dp))
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = label, onValueChange = { label = it },
            label = { Text("Nombre del billete") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = denominationText, onValueChange = { denominationText = it.filter(Char::isDigit) },
            label = { Text("Denominación ($)") },
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = frontText, onValueChange = { frontText = it },
            label = { Text("Texto frente") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = backText, onValueChange = { backText = it },
            label = { Text("Texto reverso") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Es moneda (redonda)?", fontSize = 13.sp, modifier = Modifier.weight(1f))
            Switch(checked = isCoin, onCheckedChange = { isCoin = it })
        }

        Spacer(Modifier.height(12.dp))
        Text("Color", fontSize = 13.sp, color = Color(0xFF49454F))
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(PRESET_COLORS) { c ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(c))
                        .border(
                            width = if (c == colorHex) 3.dp else 0.dp,
                            color = Color(0xFF21005D),
                            shape = CircleShape
                        )
                        .clickable { colorHex = c }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Patrón", fontSize = 13.sp, color = Color(0xFF49454F))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("classic", "stars", "stripes", "retro").forEach { p ->
                FilterChip(
                    selected = patternType == p,
                    onClick = { patternType = p },
                    label = { Text(p) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Ícono frente", fontSize = 13.sp, color = Color(0xFF49454F))
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ICON_OPTIONS) { name ->
                IconButton(
                    onClick = { frontIcon = name },
                    modifier = Modifier
                        .background(
                            if (frontIcon == name) Color(0xFFEADDFF) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(iconFor(name), contentDescription = name, tint = Color(0xFF21005D))
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    val denom = denominationText.toLongOrNull() ?: 0L
                    if (existing != null) {
                        viewModel.updateBillDenomination(existing.id, denom)
                        viewModel.updateBillDesign(
                            billId = existing.id,
                            label = label,
                            isCoin = isCoin,
                            frontDesignText = frontText,
                            backDesignText = backText,
                            patternType = patternType,
                            colorHex = colorHex,
                            frontIcon = frontIcon,
                            backIcon = backIcon
                        )
                    } else {
                        viewModel.addBillTemplate(
                            denomination = denom,
                            colorHex = colorHex,
                            iconName = frontIcon,
                            label = label,
                            isCoin = isCoin,
                            frontDesignText = frontText,
                            backDesignText = backText,
                            patternType = patternType,
                            frontIcon = frontIcon,
                            backIcon = backIcon
                        )
                    }
                    onDone()
                },
                modifier = Modifier.weight(1f)
            ) { Text("Guardar") }

            OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
        }
    }
}

/**
 * Vista previa dibujada a mano en Canvas: billete rectangular o moneda circular,
 * con patrón de fondo (classic / stars / stripes / retro) y denominación centrada.
 */
@Composable
fun BillCanvasPreview(bill: BillTemplate, modifier: Modifier = Modifier) {
    val baseColor = Color(bill.colorHex)
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (bill.isCoin) {
                val radius = min(w, h) / 2f * 0.9f
                val center = Offset(w / 2f, h / 2f)
                drawCircle(color = baseColor, radius = radius, center = center)
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = radius * 0.72f,
                    center = center,
                    style = Stroke(width = 2f)
                )
            } else {
                drawRoundRect(
                    color = baseColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.55f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                    style = Stroke(width = 3f)
                )
            }

            when (bill.patternType) {
                "stars" -> {
                    val step = w / 6f
                    var x = step / 2f
                    while (x < w) {
                        drawCircle(Color.White.copy(alpha = 0.35f), radius = 2.5f, center = Offset(x, h * 0.2f))
                        drawCircle(Color.White.copy(alpha = 0.35f), radius = 2.5f, center = Offset(x, h * 0.8f))
                        x += step
                    }
                }
                "stripes" -> {
                    val step = w / 8f
                    var x = -h
                    while (x < w) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.25f),
                            start = Offset(x, h),
                            end = Offset(x + h, 0f),
                            strokeWidth = 4f
                        )
                        x += step
                    }
                }
                "retro" -> {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.5f),
                        topLeft = Offset(w * 0.06f, h * 0.12f),
                        size = androidx.compose.ui.geometry.Size(w * 0.88f, h * 0.76f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                        style = Stroke(width = 2f)
                    )
                }
                else -> { /* classic: sin patrón extra, solo el borde ya dibujado */ }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = bill.frontDesignText,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = "$${bill.denomination}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Icon(
                imageVector = iconFor(bill.frontIcon),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
