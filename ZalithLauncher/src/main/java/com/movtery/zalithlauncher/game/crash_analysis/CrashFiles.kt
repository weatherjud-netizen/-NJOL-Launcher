package com.movtery.zalithlauncher.game.crash_analysis

import java.io.File
import java.io.RandomAccessFile

/** Read-only, bounded local access. Never searches outside the actual game directory. */
object CrashFiles {
    private const val MAX_BYTES = 512 * 1024

    data class Input(val log: String, val report: String)

    fun read(logFile: File?, gameDirectory: File?, sessionStartedAt: Long): Input {
        val log = readWindow(logFile)
        val report = findCurrentReport(gameDirectory, sessionStartedAt)?.let(::readWindow).orEmpty()
        return Input(log, report)
    }

    private fun findCurrentReport(gameDirectory: File?, sessionStartedAt: Long): File? {
        if (gameDirectory == null || sessionStartedAt <= 0) return null
        return try {
            val root = gameDirectory.canonicalFile
            val folder = File(root, "crash-reports").canonicalFile
            // Do not follow a crash-reports symlink outside the game directory.
            if (folder.parentFile != root || !folder.isDirectory) return null
            folder.listFiles()?.asSequence()
                ?.filter { file ->
                    file.name.startsWith("crash-") && file.name.endsWith(".txt") &&
                        file.isFile && file.canonicalFile.parentFile == folder &&
                        file.lastModified() >= sessionStartedAt
                }
                ?.maxByOrNull { it.lastModified() }
        } catch (_: Exception) {
            null
        }
    }

    /** Keep both the beginning (loader diagnostics) and end (exception) of a large file. */
    private fun readWindow(file: File?): String {
        if (file == null) return ""
        return try {
            if (!file.isFile) return ""
            RandomAccessFile(file, "r").use { stream ->
                val size = stream.length()
                if (size <= MAX_BYTES) {
                    val bytes = ByteArray(size.toInt())
                    stream.readFully(bytes)
                    String(bytes, Charsets.UTF_8)
                } else {
                    val first = ByteArray(MAX_BYTES / 2)
                    val last = ByteArray(MAX_BYTES / 2)
                    stream.readFully(first)
                    stream.seek(size - last.size)
                    stream.readFully(last)
                    String(first, Charsets.UTF_8) + "\n" + String(last, Charsets.UTF_8)
                }
            }
        } catch (_: Exception) {
            ""
        }
    }
}
