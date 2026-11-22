package com.movtery.zalithlauncher.game.crash_analysis

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

class CrashAnalyzerTest {
    @get:Rule val temp = TemporaryFolder()

    private fun analyze(log: String = "", report: String = "", code: Int = 1, signal: Boolean = false) =
        CrashAnalyzer.analyze(CrashContext(code, signal, logContent = log, reportContent = report))

    @Test fun fabricExplicitDependencyNamesOnlyReportedMod() {
        val log = "ModResolutionException: incompatible mod set\n" +
            " - Mod 'Sodium' (sodium) 0.5 requires version 0.16 or later of mod 'Fabric Loader' (fabricloader), but only the wrong version is present: 0.15!"
        val tips = analyze(log)
        assertTrue(tips.any { it.title.contains("sodium") })
        assertFalse(tips.any { it.title.contains("fabricloader") })
        assertTrue(tips.any { it.title == "Ошибка загрузчика модов" })
    }

    @Test fun noIncompatibilityJustBecauseModsAreListed() {
        val tips = analyze("Loading mods: sodium, iris, optifine, jei, emi\nMod 'Example' (example) requires version 1.0 or later")
        assertEquals("Причина не определена", tips.single().title)
    }

    @Test fun forgeMissingDependencyNamesRequester() {
        val log = "Missing or unsupported mandatory dependencies:\n" +
            "Mod ID: 'library', Requested by: 'coolmod', Expected range: '[2,)', Actual version: '[MISSING]'"
        val tips = analyze(log)
        assertTrue(tips.any { it.title.contains("coolmod") })
        assertFalse(tips.any { it.title.contains("library") })
    }

    @Test fun forgeVersionMetadataAloneDoesNotAssertFailure() {
        val tips = analyze("Mod ID: 'library', Requested by: 'coolmod', Expected range: '[2,)', Actual version: '2.0'")
        assertEquals("Причина не определена", tips.single().title)
    }

    @Test fun forgeCrashReportOnlyNamesFailingBlock() {
        val report = "-- MOD example --\nFailure message: Example has failed to load correctly\n" +
            "-- MOD another --\nOther information: present"
        val tips = analyze(report = report)
        assertTrue(tips.any { it.title.contains("example") })
        assertFalse(tips.any { it.title.contains("another") })
    }

    @Test fun fabricEntrypointIsReportedAsFailureNotCertainCause() {
        val tips = analyze("Could not execute entrypoint stage 'main' due to errors, provided by 'samplemod' at 'com.secret.Class'!")
        assertTrue(tips.any { it.title.contains("samplemod") })
        assertTrue(tips.any { it.description.contains("может быть") })
    }

    @Test fun javaVersionIsDerivedFromBytecodeNotAndroidRuntime() {
        val tips = analyze("UnsupportedClassVersionError: Example has been compiled by a more recent version (class file version 65.0), this version only recognizes class file versions up to 61.0")
        assertTrue(tips.any { it.description.contains("Java 21") && it.description.contains("Java 17") })
    }

    @Test fun heapAndReservationGetOppositeSafeAdvice() {
        val heap = analyze("java.lang.OutOfMemoryError: Java heap space")
        assertTrue(heap.any { it.solution.contains("увеличить") })
        val reserve = analyze("Could not reserve enough space for object heap")
        assertTrue(reserve.any { it.solution.contains("Уменьшите") })
    }

    @Test fun exitCodesAloneAreNotMisdiagnosedAsOom() {
        val tips = analyze(code = 137)
        assertEquals("Причина не определена", tips.single().title)
        val signal = analyze(code = 9, signal = true)
        assertTrue(signal.single().description.contains("не устанавливает причину"))
    }

    @Test fun rawLogSecretsAndPathsNeverAppearInOutput() {
        val secret = "example-secret-token-abc"
        val path = "/private/user/account/path"
        val log = "accessToken=$secret\n$path\njava.lang.OutOfMemoryError: Java heap space"
        val output = analyze(log).toString()
        assertFalse(output.contains(secret))
        assertFalse(output.contains(path))
    }

    @Test fun rendererAndLoaderSignsCanCoexist() {
        val tips = analyze("ModResolutionException\nFailed to initialize EGL")
        assertTrue(tips.any { it.title == "Ошибка загрузчика модов" })
        assertTrue(tips.any { it.title.contains("рендеринга") })
    }

    @Test fun keepsOtherLegacyCategoriesWithUncertainAttribution() {
        val tips = analyze("ZipException\nNo space left on device\nUnknownHostException\nRegistry snapshot validation failed")
        assertTrue(tips.any { it.title == "Ошибка чтения архива" })
        assertTrue(tips.any { it.title == "Нехватка места в хранилище" })
        assertTrue(tips.any { it.title == "Сетевое сообщение в логе" })
        assertTrue(tips.any { it.title == "Несовпадение данных мира" })
    }

    @Test fun legacyOnlySignaturesRemainAvailableWithoutLegacyUnsafeAdvice() {
        val tips = analyze("Feature order cycle\nGL error\nProtocol version mismatch\nMissing dependency")
        assertTrue(tips.any { it.title == "Сообщение о данных мира или реестре" })
        assertTrue(tips.any { it.title == "Сообщение OpenGL" })
        assertTrue(tips.any { it.title == "Сетевое сообщение в логе" })
        assertTrue(tips.any { it.title == "Ошибка загрузчика модов" })
        assertFalse(tips.toString().contains("Remove"))
    }

    @Test fun onlyRecentReportFromActualGameDirectoryIsUsed() {
        val root = temp.newFolder("isolated")
        val dir = File(root, "crash-reports").apply { mkdir() }
        val old = File(dir, "crash-old.txt").apply { writeText("old marker") }
        val fresh = File(dir, "crash-new.txt").apply { writeText("fresh marker") }
        val now = System.currentTimeMillis()
        assertTrue(old.setLastModified(now - 60_000))
        assertTrue(fresh.setLastModified(now))
        assertEquals("fresh marker", CrashFiles.read(null, root, now - 30_000).report)
        assertEquals("", CrashFiles.read(null, root, now + 1_000).report)
        assertEquals("", CrashFiles.read(null, root, 0).report)
    }

    @Test fun reportSymlinksCannotEscapeGameDirectory() {
        val root = temp.newFolder("game")
        val outside = temp.newFolder("elsewhere")
        File(outside, "crash-secret.txt").writeText("private marker")
        Files.createSymbolicLink(File(root, "crash-reports").toPath(), outside.toPath())
        assertEquals("", CrashFiles.read(null, root, 1).report)
    }

    @Test fun hugeLogsAreBoundedAndKeepStartAndEnd() {
        val file = temp.newFile("game.log")
        file.writeText("beginning\n" + "x".repeat(700_000) + "\nending")
        val log = CrashFiles.read(file, null, 0).log
        assertTrue(log.startsWith("beginning"))
        assertTrue(log.endsWith("ending"))
        assertTrue(log.length <= 512 * 1024 + 1)
    }
}
