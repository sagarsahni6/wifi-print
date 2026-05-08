package com.wifiprint.app.ui.screens.templates

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wifiprint.app.ui.theme.*

// ── Template Data ──────────────────────────────────────────────────────

data class PrintTemplate(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val photoSlots: Int,
    val paperSize: String,
    val photoWidth: Dp,
    val photoHeight: Dp,
    val columns: Int = 2,
    val rows: Int = 2
)

val builtInTemplates = listOf(
    PrintTemplate(
        id = "id_card",
        name = "ID Card Photo",
        description = "Standard 35×45mm passport-size photo\n8 photos per A4 sheet",
        icon = Icons.Filled.Badge,
        color = Color(0xFF5C6BC0),
        photoSlots = 8,
        paperSize = "A4",
        photoWidth = 35.dp, photoHeight = 45.dp,
        columns = 4, rows = 2
    ),
    PrintTemplate(
        id = "passport",
        name = "Passport Photo",
        description = "ICAO standard 51×51mm photo\n4 photos per A4 sheet",
        icon = Icons.Filled.AirplaneTicket,
        color = Color(0xFF26A69A),
        photoSlots = 4,
        paperSize = "A4",
        photoWidth = 51.dp, photoHeight = 51.dp,
        columns = 2, rows = 2
    ),
    PrintTemplate(
        id = "visa_photo",
        name = "Visa Photo",
        description = "US Visa 50×50mm photo\n4 photos per A4 sheet",
        icon = Icons.Filled.Public,
        color = Color(0xFFFFA726),
        photoSlots = 4,
        paperSize = "A4",
        photoWidth = 50.dp, photoHeight = 50.dp,
        columns = 2, rows = 2
    ),
    PrintTemplate(
        id = "wallet_4x6",
        name = "Wallet Print (4×6)",
        description = "Wallet-size photos\n9 photos on 4×6 paper",
        icon = Icons.Filled.CreditCard,
        color = Color(0xFFEF5350),
        photoSlots = 9,
        paperSize = "4x6",
        photoWidth = 25.dp, photoHeight = 35.dp,
        columns = 3, rows = 3
    ),
    PrintTemplate(
        id = "photo_3r",
        name = "3R Photo (3.5×5)",
        description = "Standard 3R photo print\n1 photo per sheet",
        icon = Icons.Filled.Photo,
        color = Color(0xFF7E57C2),
        photoSlots = 1,
        paperSize = "3R",
        photoWidth = 89.dp, photoHeight = 127.dp,
        columns = 1, rows = 1
    ),
    PrintTemplate(
        id = "custom_grid",
        name = "Custom Grid",
        description = "Arrange photos in custom grid layout",
        icon = Icons.Filled.GridView,
        color = Color(0xFF26C6DA),
        photoSlots = 6,
        paperSize = "A4",
        photoWidth = 60.dp, photoHeight = 80.dp,
        columns = 3, rows = 2
    )
)

// ── Template Selection Screen ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintTemplateScreen(
    onBack: () -> Unit,
    onTemplateReady: (templateId: String, photos: List<Uri>) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf<PrintTemplate?>(null) }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showPreview by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedPhotos = uris
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            showPreview -> "Preview Layout"
                            selectedTemplate != null -> selectedTemplate!!.name
                            else -> "Print Templates"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            showPreview -> showPreview = false
                            selectedTemplate != null -> {
                                selectedTemplate = null
                                selectedPhotos = emptyList()
                            }
                            else -> onBack()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            showPreview && selectedTemplate != null -> {
                TemplatePreview(
                    template = selectedTemplate!!,
                    photos = selectedPhotos,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onPrint = {
                        onTemplateReady(selectedTemplate!!.id, selectedPhotos)
                    }
                )
            }
            selectedTemplate != null -> {
                PhotoPicker(
                    template = selectedTemplate!!,
                    photos = selectedPhotos,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onPickPhotos = { photoLauncher.launch(arrayOf("image/*")) },
                    onClearPhotos = { selectedPhotos = emptyList() },
                    onPreview = { showPreview = true }
                )
            }
            else -> {
                TemplateGrid(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onSelect = { selectedTemplate = it }
                )
            }
        }
    }
}

// ── Template Grid ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateGrid(
    modifier: Modifier = Modifier,
    onSelect: (PrintTemplate) -> Unit
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            "Choose a template to arrange your photos for printing.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(builtInTemplates) { template ->
                Card(
                    onClick = { onSelect(template) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = template.color.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Template icon
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = template.color.copy(alpha = 0.15f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(template.icon, null,
                                    tint = template.color,
                                    modifier = Modifier.size(28.dp))
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            template.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "${template.photoSlots} photos • ${template.paperSize}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // Grid preview dots
                        Spacer(Modifier.height(8.dp))
                        GridPreviewDots(template.columns, template.rows, template.color)
                    }
                }
            }
        }
    }
}

@Composable
private fun GridPreviewDots(columns: Int, rows: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (r in 0 until rows.coerceAtMost(3)) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (c in 0 until columns.coerceAtMost(4)) {
                    Surface(
                        modifier = Modifier.size(10.dp, 13.dp),
                        shape = RoundedCornerShape(2.dp),
                        color = color.copy(alpha = 0.3f),
                        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
                    ) {}
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

// ── Photo Picker Screen ────────────────────────────────────────────────

@Composable
private fun PhotoPicker(
    template: PrintTemplate,
    photos: List<Uri>,
    modifier: Modifier = Modifier,
    onPickPhotos: () -> Unit,
    onClearPhotos: () -> Unit,
    onPreview: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Template info card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = template.color.copy(alpha = 0.08f)
            )
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = template.color.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(template.icon, null, tint = template.color, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(template.name, fontWeight = FontWeight.SemiBold)
                    Text(template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Photo selection area
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Selected Photos", fontWeight = FontWeight.SemiBold)
                    if (photos.isNotEmpty()) {
                        TextButton(onClick = onClearPhotos) {
                            Text("Clear All", color = Red400)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (photos.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text("No photos selected",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Select up to ${template.photoSlots} photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Photo grid
                    val gridCols = 3
                    val chunked = photos.chunked(gridCols)
                    chunked.forEach { rowPhotos ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPhotos.forEach { uri ->
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Selected photo",
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(template.photoWidth / template.photoHeight)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Fill empty slots
                            repeat(gridCols - rowPhotos.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        "${photos.size} of ${template.photoSlots} slots filled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Pick photos button
                OutlinedButton(
                    onClick = onPickPhotos,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (photos.isEmpty()) "Select Photos" else "Change Photos")
                }
            }
        }

        // Print Settings summary
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Layout Settings", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                SettingRow("Paper Size", template.paperSize)
                SettingRow("Grid", "${template.columns} × ${template.rows}")
                SettingRow("Photo Size", "${template.photoWidth.value.toInt()}×${template.photoHeight.value.toInt()}mm")
                SettingRow("Photos per Sheet", "${template.photoSlots}")
            }
        }

        // Action buttons
        Button(
            onClick = onPreview,
            enabled = photos.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Visibility, null)
            Spacer(Modifier.width(8.dp))
            Text("Preview Layout", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium)
    }
}

// ── Template Preview ───────────────────────────────────────────────────

@Composable
private fun TemplatePreview(
    template: PrintTemplate,
    photos: List<Uri>,
    modifier: Modifier = Modifier,
    onPrint: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = modifier.padding(16.dp)) {
        // Paper simulation
        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Simulate print layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                for (row in 0 until template.rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0 until template.columns) {
                            val photoIndex = row * template.columns + col
                            val photoUri = if (photoIndex < photos.size) photos[photoIndex]
                            else if (photos.isNotEmpty()) photos[photoIndex % photos.size]
                            else null

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(template.photoWidth / template.photoHeight)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .border(
                                        1.dp,
                                        DividerColor,
                                        RoundedCornerShape(2.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(photoUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Photo ${photoIndex + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.Person,
                                        null,
                                        tint = DividerColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        // Info
        Text(
            "${template.name} • ${template.paperSize} • ${template.columns}×${template.rows} grid",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Print button
        Button(
            onClick = onPrint,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Print, null)
            Spacer(Modifier.width(8.dp))
            Text("Print Template", fontWeight = FontWeight.SemiBold)
        }
    }
}
