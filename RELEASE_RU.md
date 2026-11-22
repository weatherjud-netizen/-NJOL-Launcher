# Сборка и выпуск NJOL 1.0

NJOL — неофициальная изменённая версия GPLv3-проектов upstream. Этот документ описывает текущий снимок и не заменяет юридическую проверку конкретной поставки.

## Сборка Debug локально

Нужны JDK 17, Gradle Wrapper 9.4.1, Android SDK Platform `37.0`, Build Tools `36.0.0` и NDK `25.2.9519653`. Минимальная версия Android — API 26. Сборке нужен доступ к Google Maven, Maven Central, Gradle и JitPack.

```bash
./gradlew --no-daemon :ZalithLauncher:testDebugUnitTest
./gradlew --no-daemon :ZalithLauncher:assembleDebug -Darch=arm64
./gradlew --no-daemon :ZalithLauncher:assembleDebug -Darch=all
```

APK появляются в `ZalithLauncher/build/outputs/apk/debug/`. `all` создаёт универсальный вариант. Отображаемая версия NJOL — **1.0**; Debug-суффикс добавляется только сборочной конфигурацией APK.

## GitHub Actions

[`.github/workflows/build.yml`](.github/workflows/build.yml) запускает unit-тесты и Debug-сборку, устанавливает SDK/NDK и загружает универсальный APK-артефакт. Workflow не делает production Release: текущая публичная конфигурация подписи использует тестовый ключ.

## Подпись и идентификатор

Debug APK предназначен только для тестирования. Публичный тестовый keystore исходной конфигурации нельзя использовать как доверенный ключ. Для публичного релиза создайте новый закрытый release keystore, храните его и пароли вне Git, подключите GitHub Secrets и проверьте сертификат на чистом устройстве.

Идентификатор Debug-варианта — `com.napolejavaon.launcher.debug`. Данные и локальные аккаунты между Debug и будущим Release автоматически не переносятся; предусмотрите проверенный экспорт/импорт.

## Исходники и лицензии

При публикации APK предоставьте соответствующий исходный комплект, `LICENSE`, notices сторонних компонентов и устойчивую ссылку на исходники. Не включайте в архив приватные ключи, пароли, OAuth-токены и API-ключи. Встроенные JRE/JAR/AAR/SO требуют отдельной проверки полноты соответствующих исходников и условий лицензирования.

Не удаляйте сохранённые copyright notices и сведения о происхождении в [LICENSE_RU.md](LICENSE_RU.md) и [THIRD_PARTY_NOTICES_RU.md](THIRD_PARTY_NOTICES_RU.md). Они нужны для корректного распространения изменённой GPLv3-версии.
