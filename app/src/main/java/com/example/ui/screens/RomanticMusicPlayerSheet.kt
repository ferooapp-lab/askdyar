package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomMusicTrackEntity
import com.example.ui.LoveQuotesViewModel
import com.example.util.BuiltInAmbientPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomanticMusicPlayerSheet(
    viewModel: LoveQuotesViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPlaying by viewModel.isAmbientPlaying.collectAsState()
    val activeTrackTitle by viewModel.currentMusicTrackTitle.collectAsState()
    val activeTrackKey by viewModel.currentMusicTrackKey.collectAsState()
    val volume by viewModel.musicVolume.collectAsState()
    val customTracks by viewModel.customMusicTracks.collectAsState()
    val adminSettings by viewModel.adminSettings.collectAsState()

    var pendingAudioUri by remember { mutableStateOf<Uri?>(null) }
    var customTrackTitle by remember { mutableStateOf("") }
    var customArtistName by remember { mutableStateOf("Benim Seçimim") }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingAudioUri = uri
            customTrackTitle = ""
            customArtistName = "Benim Seçimim"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("romantic_music_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Romantik Fon Müziği Stüdyosu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Kendi romantik müziğini ekle veya hazır melodileri dinle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Now Playing Control Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isPlaying) "ŞU AN ÇALIYOR" else "DURAKLATILDI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = activeTrackTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.toggleAmbientMusic() },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .sizeIn(minHeight = 48.dp)
                                    .testTag("music_sheet_play_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Durdur" else "Çal"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPlaying) "Durdur" else "Başlat", fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Ses Seviyesi",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Slider(
                                value = volume,
                                onValueChange = { viewModel.setMusicVolume(it) },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "%${(volume * 100).toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Add Custom Track Button
            item {
                Button(
                    onClick = {
                        if (adminSettings.allowCustomMusic) {
                            audioPickerLauncher.launch(arrayOf("audio/*"))
                        } else {
                            Toast.makeText(context, "Özel müzik ekleme şu an yönetici tarafından kısıtlı.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = adminSettings.allowCustomMusic,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 50.dp)
                        .testTag("add_custom_music_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cihazdan Romantik Fon Müziği Ekle (MP3 / WAV)", fontWeight = FontWeight.Bold)
                }
            }

            // User Uploaded Tracks
            item {
                Text(
                    text = "Benim Eklediğim Müzikler (${customTracks.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (customTracks.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Henüz kendi fon müziğini eklemedin. Yukarıdaki butonla telefonundan sevdiğin romantik bir parçayı ekleyebilirsin 🎵",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                items(customTracks, key = { it.id }) { track ->
                    val isCurrent = activeTrackKey == "custom_${track.id}" && isPlaying
                    CustomTrackRow(
                        track = track,
                        isPlayingCurrent = isCurrent,
                        onPlay = { viewModel.playCustomMusicTrack(track) },
                        onDelete = {
                            viewModel.deleteCustomMusicTrack(track)
                            Toast.makeText(context, "Müzik silindi.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Built-in Synthesized Presets
            item {
                Text(
                    text = "Hazır Romantik Melodiler",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(BuiltInAmbientPresets.presets, key = { it.key }) { preset ->
                val isCurrent = activeTrackKey == preset.key && isPlaying
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = if (isCurrent) BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.playAmbientPreset(preset.key) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(preset.emoji, fontSize = 22.sp)
                            Column {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if (isCurrent) viewModel.toggleAmbientMusic()
                                else viewModel.playAmbientPreset(preset.key)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrent) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isCurrent) "Duraklat" else "Çal",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    pendingAudioUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingAudioUri = null },
            title = { Text("Fon Müziğini Adlandır", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Eklediğin müziğe bir isim verebilir veya boş bırakıp dosya adını kullanabilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customTrackTitle,
                        onValueChange = { customTrackTitle = it },
                        label = { Text("Müzik Adı (Opsiyonel)") },
                        placeholder = { Text("Örn: Bizim Şarkımız") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customArtistName,
                        onValueChange = { customArtistName = it },
                        label = { Text("Sanatçı / Not") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importCustomMusicFromUri(
                            uri = uri,
                            customTitle = customTrackTitle,
                            artistName = customArtistName
                        ) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        pendingAudioUri = null
                    }
                ) {
                    Text("Ekle ve Çal")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAudioUri = null }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun CustomTrackRow(
    track: CustomMusicTrackEntity,
    isPlayingCurrent: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isPlayingCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = if (isPlayingCurrent) BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onPlay)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} • Ekleyen: ${track.addedBy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlay, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = if (isPlayingCurrent) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlayingCurrent) "Çalıyor" else "Çal",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
