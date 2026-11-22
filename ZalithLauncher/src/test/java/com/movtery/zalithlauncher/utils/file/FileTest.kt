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

import kotlinx.coroutines.runBlocking
import org.jackhuang.hmcl.util.DigestUtils
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun testCalculateFileSha1() = runBlocking {
        val cases = listOf(
            ByteArray(0) to "da39a3ee5e6b4b0d3255bfef95601890afd80709",
            "abc".toByteArray(Charsets.US_ASCII) to "a9993e364706816aba3e25717850c26c9cd0d89d",
            // More than one 8192-byte read, including every possible byte value.
            ByteArray(32771) { ((it * 31 + 7) and 0xff).toByte() } to
                "e0939087c9843df83aa89ad20e09e46f3075be23"
        )

        cases.forEachIndexed { index, (bytes, expected) ->
            val file = temporaryFolder.newFile("sha1-$index.bin")
            file.writeBytes(bytes)
            val actual = calculateFileSha1(file)
            assertEquals("SHA-1 vector $index", expected, actual)
            assertEquals("HMCL SHA-1 comparison for vector $index", DigestUtils.digestToString("SHA-1", file.toPath()), actual)
        }
    }
}
