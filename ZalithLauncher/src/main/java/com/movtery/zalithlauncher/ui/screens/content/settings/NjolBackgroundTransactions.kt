/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.ui.screens.content.settings

import android.content.Context
import android.net.Uri
import android.system.Os
import androidx.core.content.FileProvider
import com.movtery.zalithlauncher.utils.image.isImageFile
import com.movtery.zalithlauncher.utils.video.isVideoFile
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/** Serializes all background changes initiated from launcher settings. */
internal object NjolBackgroundTransactions {
    private val mutex = Mutex()

    suspend fun import(context: Context, source: Uri, model: BackgroundViewModel) {
        // Do not let navigation or task cancellation interrupt the rollback/commit.
        withContext(Dispatchers.IO + NonCancellable) {
            mutex.withLock {
                val appContext = context.applicationContext
                val target = model.backgroundFile
                val parent = target.parentFile ?: throw IOException("Background directory is unavailable")
                if (!parent.isDirectory && !parent.mkdirs()) {
                    throw IOException("Cannot create background directory")
                }
                val staged = File.createTempFile("njol-background-", ".tmp", appContext.cacheDir)
                var backup: File? = null
                var startedImport = false
                var committed = false
                try {
                    val input = appContext.contentResolver.openInputStream(source)
                        ?: throw IOException("Cannot open the selected image or video")
                    input.use { stream ->
                        FileOutputStream(staged).use { output ->
                            stream.copyTo(output)
                            output.fd.sync()
                        }
                    }
                    if (!staged.isImageFile() && !staged.isVideoFile()) {
                        throw IOException("The selected file is not a valid image or video")
                    }
                    // BackgroundViewModel.import removes its destination first. Keep a complete
                    // same-directory backup until it has successfully refreshed its state.
                    if (target.exists()) {
                        val saved = File.createTempFile(".njol-background-backup-", ".tmp", parent)
                        backup = saved
                        target.inputStream().use { inputStream ->
                            FileOutputStream(saved).use { output ->
                                inputStream.copyTo(output)
                                output.fd.sync()
                            }
                        }
                    }
                    val stagedUri = FileProvider.getUriForFile(
                        appContext, "${appContext.packageName}.provider", staged
                    )
                    startedImport = true
                    model.import(appContext, stagedUri)
                    committed = true
                } catch (failure: Throwable) {
                    if (startedImport) {
                        val saved = backup
                        if (saved != null) {
                            try {
                                // Atomic replacement also removes any partial failed import.
                                Os.rename(saved.absolutePath, target.absolutePath)
                                backup = null
                            } catch (restoreError: Exception) {
                                // Never delete the only remaining copy if restoration fails.
                                throw IOException(
                                    "Не удалось восстановить фон. Резервная копия: ${saved.absolutePath}",
                                    failure
                                ).apply { addSuppressed(restoreError) }
                            }
                        } else if (target.exists() && !target.delete()) {
                            failure.addSuppressed(IOException("Cannot remove incomplete background"))
                        }
                    }
                    throw failure
                } finally {
                    staged.delete()
                    if (committed || !startedImport) backup?.delete()
                }
            }
        }
    }

    suspend fun delete(model: BackgroundViewModel) {
        withContext(Dispatchers.IO + NonCancellable) {
            mutex.withLock { model.delete() }
        }
    }
}
