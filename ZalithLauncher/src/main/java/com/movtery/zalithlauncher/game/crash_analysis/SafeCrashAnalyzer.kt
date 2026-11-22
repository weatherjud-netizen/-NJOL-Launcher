package com.movtery.zalithlauncher.game.crash_analysis

/**
 * Signature-based analysis only. Never include arbitrary log excerpts, paths, account data,
 * Java arguments or stack traces in the result. A signature is evidence, not proof of cause.
 */
object CrashAnalyzer {
    private val fabricMod = Regex(
        "(?i)\\bmod\\s+'[^'\\r\\n]{1,100}'\\s*\\(([a-z][a-z0-9_-]{0,63})\\)([^\\r\\n]{0,500})"
    )
    private val forgeDependency = Regex(
        "(?i)mod id:\\s*'([a-z][a-z0-9_-]{0,63})'\\s*,\\s*requested by:\\s*'([a-z][a-z0-9_-]{0,63})'[^\\r\\n]{0,400}actual version:\\s*'[^'\\r\\n]{0,100}'"
    )
    private val forgeBlock = Regex("(?im)^\\s*--\\s*MOD\\s+([a-z][a-z0-9_-]{0,63})\\s*--\\s*$")
    private val entrypoint = Regex("(?i)could not execute entrypoint[^\\r\\n]{0,300}provided by '([a-z][a-z0-9_-]{0,63})'")
    private val classVersions = Regex("(?i)class file version (\\d{2,3})(?:\\.\\d+)?[^\\r\\n]{0,250}?(?:up to|through) (\\d{2,3})(?:\\.\\d+)?")

    fun analyze(context: CrashContext): List<CrashTip> {
        val text = context.logContent + "\n" + context.reportContent
        val tips = mutableListOf<CrashTip>()
        fun has(value: String) = text.contains(value, ignoreCase = true)
        fun tip(title: String, description: String, solution: String, severity: Severity = Severity.ERROR) {
            tips.add(CrashTip(severity, title, description, solution))
        }
        // Preserve the existing signature engine as an input, never its unverified English
        // diagnoses or destructive recommendations. A mod list alone proves no conflict.
        val legacyTitles = LegacyCrashAnalyzer.analyze(
            context.copy(logContent = text, mods = emptyList())
        ).mapTo(mutableSetOf()) { it.title }
        val knownMods = context.mods.associateBy { it.id.lowercase() }
        fun modLabel(id: String): String {
            val mod = knownMods[id.lowercase()]
            return if (mod == null || mod.name.equals(id, ignoreCase = true)) id
            else "${mod.name} ($id, ${mod.version})"
        }

        // A mod ID is shown only when a loader explicitly associates it with a failure.
        // Installed mod names or mere co-existence are not evidence of incompatibility.
        val incompatibleIds = linkedSetOf<String>()
        fabricMod.findAll(text).forEach { match ->
            val detail = match.groupValues[2].lowercase()
            val incompatible = detail.contains("is incompatible with") || detail.contains("conflicts with") ||
                (detail.contains("requires") && (detail.contains("which is missing") ||
                    detail.contains("not installed") || detail.contains("but only") || detail.contains("but a different version")))
            if (incompatible) safeModId(match.groupValues[1])?.let { incompatibleIds.add(it) }
        }
        if (has("Missing or unsupported mandatory dependencies") || has("Loading errors encountered") || has("ModLoadingException")) {
            forgeDependency.findAll(text).forEach { match ->
                safeModId(match.groupValues[2])?.let { incompatibleIds.add(it) }
            }
        }
        incompatibleIds.take(4).forEach { id ->
            tip("Несовместимость или зависимость мода: ${modLabel(id)}",
                "Загрузчик явно указал этот ID мода в сообщении о несовместимости или неудовлетворённой зависимости. Это обнаруженный признак, а не гарантия единственной причины сбоя.",
                "Проверьте требования именно этой версии мода, Minecraft и загрузчика; при необходимости вручную установите совместимую версию или временно отключите мод после резервной копии.")
        }

        val failedIds = linkedSetOf<String>()
        entrypoint.findAll(text).forEach { safeModId(it.groupValues[1])?.let(failedIds::add) }
        forgeBlock.findAll(text).forEach { match ->
            // Associate the failure message only with this block, not another mod below it.
            val start = match.range.last + 1
            val next = forgeBlock.find(text, start)?.range?.first ?: text.length
            val block = text.substring(start, minOf(next, start + 3000))
            if (block.contains("Failure message:", ignoreCase = true) &&
                (block.contains("failed to load", ignoreCase = true) || block.contains("has failed", ignoreCase = true))) {
                safeModId(match.groupValues[1])?.let(failedIds::add)
            }
        }
        failedIds.filterNot { it in incompatibleIds }.take(4).forEach { id ->
            tip("Ошибка при загрузке мода: ${modLabel(id)}",
                "Отчёт или загрузчик связывает этот ID мода с ошибкой инициализации. Причиной может быть сам мод, его зависимость или окружение.",
                "Сверьте версию мода и его зависимости с версией игры и загрузчика. Можно вручную временно отключить его для проверки, предварительно сохранив копию мира.")
        }

        if (has("ModResolutionException") || has("incompatible mod set") ||
            has("some of your mods are incompatible") || has("Loading errors encountered") ||
            has("ModLoadingException") || has("Missing or unsupported mandatory dependencies") ||
            legacyTitles.any { it in setOf("Conflicting Mods", "Missing Mod Dependency", "Incompatible Mod Set", "Mod Failed to Initialize") }) {
            tip("Ошибка загрузчика модов",
                "В логе найдено сообщение о сбое разрешения или загрузки модов. Оно само по себе не определяет виновный мод.",
                "Откройте лог и проверьте перечисленные зависимости и их версии для выбранного загрузчика (Forge, Fabric или другого).")
        }
        if (has("Mixin apply failed") || has("MixinApplyError") || has("MixinTransformerError") ||
            has("InjectionError") || has("mixin transformation failed") || "Missing Mixin Target" in legacyTitles) {
            tip("Ошибка применения Mixin",
                "В логе указана ошибка преобразования Mixin. Возможны несовместимые версии модов, игры или загрузчика; конкретный виновник не установлен.",
                "Проверьте версии модов и загрузчика, упомянутые в логе. Для проверки отключайте моды вручную только после резервной копии мира.")
        }

        when {
            has("OutOfMemoryError: Java heap space") -> tip("Недостаточно памяти Java (heap)",
                "В логе есть OutOfMemoryError: Java heap space: JVM исчерпала доступную ей кучу. Это может быть связано с объёмом сборки или утечкой памяти.",
                "Закройте другие приложения и проверьте выделение памяти для этой версии. При наличии свободной памяти можно немного увеличить лимит, оставив достаточно памяти Android.")
            has("OutOfMemoryError: Metaspace") -> tip("Недостаточно памяти Metaspace",
                "JVM сообщила об исчерпании Metaspace; это не то же самое, что нехватка игровой кучи.",
                "Проверьте пользовательские аргументы JVM и требования сборки. Не меняйте лимиты наугад; попробуйте совместимую Java.")
            has("OutOfMemoryError: Direct buffer memory") -> tip("Недостаточно памяти прямого буфера",
                "JVM сообщила об исчерпании памяти прямых буферов; это не доказывает нехватку памяти GPU.",
                "Закройте другие приложения и проверьте ограничения памяти и аргументы JVM. Попробуйте уменьшить нагрузку сборки.")
            has("OutOfMemoryError") -> tip("Ошибка памяти JVM",
                "В логе найден OutOfMemoryError, но по этому признаку нельзя надёжно определить исчерпанный ресурс.",
                "Проверьте подробности в логе, свободную память устройства и параметры JVM; изменяйте их постепенно.")
        }
        if (has("Could not reserve enough space for object heap") || has("Could not allocate enough memory for the Java heap")) {
            tip("Не удалось выделить кучу Java",
                "JVM не смогла зарезервировать запрошенную память; причиной могут быть ограничения процесса или нехватка доступной памяти.",
                "Уменьшите выделенную игре память и закройте другие приложения, затем попробуйте снова.")
        }
        if (has("UnsupportedClassVersionError")) {
            val versions = classVersions.find(text)
            val required = versions?.groupValues?.get(1)?.toIntOrNull()?.minus(44)?.takeIf { it in 1..36 }
            val current = versions?.groupValues?.get(2)?.toIntOrNull()?.minus(44)?.takeIf { it in 1..36 }
            val detail = if (required != null && current != null) " По версиям байткода требуется Java $required или новее; текущая JVM поддерживает до Java $current." else ""
            tip("Несовместимая версия Java",
                "Найден UnsupportedClassVersionError: один из классов скомпилирован для более новой Java, чем используемая JVM.$detail Это не обязательно мод.",
                "Выберите Java, подходящую одновременно версии игры, загрузчику и модам. Не обязательно устанавливать самую новую версию.")
        }
        if (has("Could not find or load main class")) {
            tip("Не найден главный класс Java", "JVM сообщила, что не удалось загрузить главный класс. Причиной может быть путь классов или неполная установка.",
                "Проверьте целостность и настройки выбранной версии. При необходимости восстановите её, не затрагивая миры.")
        }
        if (has("NoClassDefFoundError") || has("ClassNotFoundException")) {
            tip("Не найден класс Java", "В логе есть ошибка поиска класса. Это может быть недостающая библиотека, зависимость мода или ошибка установки; виновный мод не определён.",
                "Проверьте подробности исключения и зависимости сборки, сохранив миры и пользовательские файлы.", Severity.WARNING)
        }
        if (has("StackOverflowError")) {
            tip("Переполнение стека Java", "JVM сообщила о переполнении стека. Возможна рекурсия или ошибка в коде; виновник по одной сигнатуре неизвестен.",
                "Проверьте трассировку в логе и совместимость компонентов сборки.", Severity.WARNING)
        }
        if (has("Duplicate mods") || has("found two jars") || has("duplicate mod id")) {
            tip("Сообщение о дубликатах модов", "Загрузчик сообщил о возможных дубликатах модов; проверять следует файлы, перечисленные в логе.",
                "Вручную проверьте копии и версии модов. Для проверки временно переместите лишнюю копию в резервное место, ничего не удаляя.")
        }
        if (has("Registry snapshot validation failed") || has("Tried to load world with data version")) {
            tip("Несовпадение данных мира", "В логе есть сообщение о проверке реестра или версии данных мира. Причину и состояние сохранения по нему нельзя установить однозначно.",
                "Не перезаписывайте мир; сначала сделайте резервную копию и проверьте версию игры и набор модов, с которыми он работал.", Severity.WARNING)
        }
        if (legacyTitles.any { it in setOf("World Generation Cycle", "Missing Block/Item Mapping", "Unknown Biome", "ID Overflow (Legacy Minecraft)") }) {
            tip("Сообщение о данных мира или реестре", "В логе найдено сообщение о генерации мира, идентификаторе или сопоставлении. Оно может быть сопутствующим и не доказывает повреждение мира.",
                "Сначала сохраните резервную копию мира; затем проверьте упомянутые в логе версии игры и модов.", Severity.WARNING)
        }
        if (has("Failed to create graphics context") || has("Failed to create window") ||
            has("GLFW error 65542") || has("Pixel format not accelerated") || has("GL_OUT_OF_MEMORY") ||
            has("EGL_BAD_CONFIG") || has("Could not find compatible EGLConfig") || has("Failed to initialize EGL") ||
            has("Failed to load GLFW") || has("Could not initialize class org.lwjgl")) {
            tip("Ошибка графического контекста или рендеринга",
                "В логе есть сообщение об ошибке создания окна, графического контекста или GL_OUT_OF_MEMORY. Оно не доказывает конкретную неисправность драйвера.",
                "Попробуйте вручную выбрать другой совместимый рендерер для этой версии; при наличии шейдеров временно отключите их и уменьшите графическую нагрузку.")
        }
        if (legacyTitles.any { it == "OpenGL Error" || it == "Framebuffer Error" }) {
            tip("Сообщение OpenGL", "Лог содержит сообщение об ошибке OpenGL или framebuffer; оно могло быть предупреждением и не обязательно вызвало завершение.",
                "Если проблема связана с графикой, попробуйте вручную другой совместимый рендерер или временно отключите шейдеры.", Severity.WARNING)
        }
        if (has("UnsatisfiedLinkError") || has("# A fatal error has been detected by the Java Runtime Environment")) {
            tip("Ошибка JVM или нативной библиотеки",
                "В логе есть ошибка загрузки нативной библиотеки или аварийный отчёт JVM. Возможны разные причины, включая окружение Java и графику.",
                "Проверьте совместимость Java и архитектуры устройства; при графической ошибке попробуйте другой совместимый рендерер.", Severity.WARNING)
        }
        if (has("No space left on device")) {
            tip("Нехватка места в хранилище", "В логе есть сообщение No space left on device: операция записи не смогла выделить место.",
                "Проверьте свободное место и освободите его самостоятельно, сохранив важные файлы.")
        }
        if (has("Permission denied") || has("AccessDeniedException")) {
            tip("Отказано в доступе к файлу", "В логе есть сообщение об отказе в доступе. Само по себе оно не указывает, какого разрешения не хватает.",
                "Проверьте путь и права доступа к файлу в логе; не публикуйте этот путь без проверки личных данных.", Severity.WARNING)
        }
        if (has("ZipException") || has("invalid CEN header") || has("error in opening zip file") ||
            has("Invalid or corrupt jarfile") || has("CRC check failed") || has("unexpected end of ZLIB input stream")) {
            tip("Ошибка чтения архива", "В логе найден признак ошибки ZIP или JAR. Файл может быть неполным, повреждённым либо неподходящим; по этой сигнатуре он не идентифицирован.",
                "Проверьте упомянутый в логе файл. При необходимости вручную замените его проверенной копией, предварительно сохранив пользовательские данные.", Severity.WARNING)
        }
        if (has("Too many open files") || has("Unable to create native thread")) {
            tip("Достигнут системный лимит", "В логе указана ошибка открытия файлов или создания потока. Причин несколько, включая ограничения ресурсов процесса.",
                "Закройте ненужные приложения и проверьте лог на повторяющиеся ошибки; не меняйте системные ограничения наугад.", Severity.WARNING)
        }
        if (has("UnknownHostException") || has("ConnectException: Connection refused") ||
            has("SocketTimeoutException") || has("SSLHandshakeException") ||
            has("Authentication servers are down") || has("Invalid session") ||
            legacyTitles.any { it == "Protocol Version Mismatch" || it == "Download Blocked (403)" }) {
            tip("Сетевое сообщение в логе", "Найдена ошибка соединения, TLS или сессии. Она могла быть сопутствующей и не обязательно вызвала сбой; состояние серверов по логу не подтверждается.",
                "Если нужна сетевая функция, проверьте соединение и дату устройства. Для автономного запуска вход в онлайн-аккаунт не требуется только из-за этого сообщения.", Severity.WARNING)
        }
        if (context.isSignal && context.exitCode != 0) {
            tip("Процесс завершился по сигналу", "JVM сообщила о завершении по сигналу ${context.exitCode}. Сам номер сигнала не устанавливает причину; данные лога могут быть неполными.",
                "Проверьте игровой лог и отчёт текущего запуска. Убедитесь, что устройству хватает свободной памяти.", Severity.WARNING)
        }
        if (tips.isEmpty()) {
            tip("Причина не определена",
                if (text.isBlank()) "Лог и отчёт текущего запуска недоступны или пусты." else "В доступной части лога не найдено достаточно надёжных признаков известных проблем.",
                "Откройте игровой лог и проверьте сообщения об ошибках. Перед передачей лога удалите из копии личные данные.", Severity.INFO)
        }
        return tips
    }

    private fun safeModId(value: String): String? {
        // IDs only, not free-form names, paths, IPs or long secret-like hex strings.
        if (!value.matches(Regex("[a-z][a-z0-9_-]{0,63}"))) return null
        if (value.length >= 24 && value.all { it in "0123456789abcdef" }) return null
        return value
    }
}
