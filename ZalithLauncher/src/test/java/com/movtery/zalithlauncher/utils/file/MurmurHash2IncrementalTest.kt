/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.utils.file

import org.apache.commons.codec.digest.MurmurHash2
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files

class MurmurHash2IncrementalTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val whitespace = listOf(0x9, 0xa, 0xd, 0x20)

    @Test
    fun testTwoWay() {
        val cases = listOf(
            ByteArray(0),
            "a".toByteArray(Charsets.US_ASCII),
            "ab".toByteArray(Charsets.US_ASCII),
            "abc".toByteArray(Charsets.US_ASCII),
            "abcd".toByteArray(Charsets.US_ASCII),
            "abcde".toByteArray(Charsets.US_ASCII),
            "a\tb\nc\rd e".toByteArray(Charsets.US_ASCII),
            byteArrayOf(9, 10, 13, 32),
            // Multiple 8192-byte reads, all byte values, and a non-aligned tail.
            ByteArray(32771) { ((it * 31 + 7) and 0xff).toByte() }
        )
        cases.forEachIndexed { index, bytes ->
            val file = temporaryFolder.newFile("murmur-$index.bin")
            file.writeBytes(bytes)
            assertEquals("Filtered vector $index", way1(file, whitespace), way2(file, whitespace))
            assertEquals("Unfiltered vector $index", way1(file, emptyList()), way2(file, emptyList()))
        }
    }

    @Test
    fun testKnownHashAndUnsignedBytesToSkip() {
        val file = temporaryFolder.newFile("known.bin")
        file.writeBytes("abc".toByteArray(Charsets.US_ASCII))
        assertEquals(1621425345L, MurmurHash2Incremental.computeHash(file))

        file.writeBytes(byteArrayOf(0, 0x80.toByte(), 0xff.toByte(), 1, 0xff.toByte()))
        val bytesToSkip = listOf(0x80, 0xff)
        assertEquals(way1(file, bytesToSkip), way2(file, bytesToSkip))
    }

    // Original full-buffer Apache Commons implementation, used as the reference.
    private fun way1(file: File, bytesToSkip: List<Int>): Long {
        val baos = ByteArrayOutputStream()
        Files.newInputStream(file.toPath()).use { stream ->
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (stream.read(buf).also { bytesRead = it } != -1) {
                for (i in 0 until bytesRead) {
                    val value = buf[i].toInt() and 0xff
                    if (value !in bytesToSkip) baos.write(value)
                }
            }
        }
        return Integer.toUnsignedLong(MurmurHash2.hash32(baos.toByteArray(), baos.size(), 1))
    }

    private fun way2(file: File, bytesToSkip: List<Int>): Long {
        return MurmurHash2Incremental.computeHash(file, byteToSkip = bytesToSkip)
    }
}
