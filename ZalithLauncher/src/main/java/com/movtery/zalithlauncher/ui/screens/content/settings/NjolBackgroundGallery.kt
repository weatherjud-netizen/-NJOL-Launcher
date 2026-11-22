/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.ui.screens.content.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
internal fun NjolBackgroundGalleryCard(
    backgroundViewModel: BackgroundViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var visible by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val applyError = stringResource(R.string.njol_background_gallery_apply_error)
    val saveError = stringResource(R.string.njol_background_gallery_save_error)

    fun reportError(title: String, error: Throwable) {
        submitError(
            ErrorViewModel.ThrowableMessage(
                title = androidText(title),
                message = androidText(error.getMessageOrToString())
            )
        )
    }

    val savePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/webp")
    ) { uri ->
        if (uri != null && !busy) {
            busy = true
            scope.launch {
                try {
                    saveNjolImage(context, uri)
                    Toast.makeText(context, R.string.njol_background_gallery_saved, Toast.LENGTH_SHORT).show()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    reportError(saveError, error)
                } finally {
                    busy = false
                }
            }
        }
    }

    SettingsCard(
        modifier = Modifier.fillMaxWidth(),
        position = CardPosition.Middle,
        title = stringResource(R.string.njol_background_gallery_title),
        summary = stringResource(R.string.njol_background_gallery_summary),
        onClick = { visible = true }
    )

    if (visible) {
        AlertDialog(
            onDismissRequest = { if (!busy) visible = false },
            title = { Text(stringResource(R.string.njol_background_gallery_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = R.drawable.njol_hero,
                        contentDescription = stringResource(R.string.njol_background_gallery_image),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    Text(stringResource(R.string.njol_background_gallery_description))
                    FilledTonalButton(
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            try {
                                savePicker.launch("NJOL-background.webp")
                            } catch (error: Exception) {
                                reportError(saveError, error)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.njol_background_gallery_save))
                    }
                    if (busy) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            try {
                                val source = Uri.Builder()
                                    .scheme("android.resource")
                                    .authority(context.packageName)
                                    .appendPath(R.drawable.njol_hero.toString())
                                    .build()
                                NjolBackgroundTransactions.import(context, source, backgroundViewModel)
                                Toast.makeText(context, R.string.njol_background_gallery_applied, Toast.LENGTH_SHORT).show()
                                visible = false
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (error: Exception) {
                                reportError(applyError, error)
                            } finally {
                                busy = false
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.njol_background_gallery_apply))
                }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { visible = false }) {
                    Text(stringResource(R.string.generic_close))
                }
            }
        )
    }
}

/** Write the original packaged WebP bytes to the document selected by the user. */
private suspend fun saveNjolImage(context: Context, destination: Uri) {
    withContext(Dispatchers.IO + NonCancellable) {
        context.applicationContext.resources.openRawResource(R.drawable.njol_hero).use { input ->
            val output = context.applicationContext.contentResolver.openOutputStream(destination)
                ?: throw IOException("Cannot open the selected document for writing")
            output.use { input.copyTo(it) }
        }
    }
}
