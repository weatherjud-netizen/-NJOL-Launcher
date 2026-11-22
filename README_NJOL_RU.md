# NapoleJavaON Launcher — NJOL 1.0

NJOL — неофициальный изменённый Android-лаунчер для Minecraft: Java Edition. Проект ориентирован на понятный интерфейс, быстрые переходы, локальные аккаунты, установку игровых версий и заметный чёрно-белый фирменный стиль.

## Что уже включено

- главный экран NJOL с фирменной hero-обложкой и быстрыми переходами;
- справочный центр с навигацией по настройкам, Java, рендерам, плагинам, загрузчикам, аккаунтам, логам, крашам и лицензиям;
- галерея фонов с прозрачностью, размытием, импортом и сохранением через SAF;
- безопасный локальный анализатор логов и crash-report без автоматического удаления модов;
- настраиваемая игровая шестерёнка: размер, перетаскивание, прилипания к краям и сохранение позиции;
- офлайн-профили и доступные в исходном проекте Forge, Fabric, NeoForge, Quilt, Legacy Fabric, OptiFine, Cleanroom и другие загрузчики.

## Установка

APK из этого рабочего снимка — Debug-сборки для тестирования. Разрешите установку APK из источника, которому доверяете, и установите подходящий вариант. Debug application ID — `com.napolejavaon.launcher.debug`.

## Сборка

```bash
./gradlew --no-daemon :ZalithLauncher:testDebugUnitTest
./gradlew --no-daemon :ZalithLauncher:assembleDebug -Darch=arm64
./gradlew --no-daemon :ZalithLauncher:assembleDebug -Darch=all
```

Версия проекта — **1.0**. Текущая сборка использует JDK 17, compile SDK 37 и описанные в [RELEASE_RU.md](RELEASE_RU.md) компоненты Android SDK/NDK. Workflow [build.yml](.github/workflows/build.yml) выполняет unit-тесты и загружает универсальный Debug APK как артефакт Actions.

## Лицензия и происхождение

NJOL — изменённая версия GPLv3-проектов [Star1xr/ZalithLauncher2Plus](https://github.com/Star1xr/ZalithLauncher2Plus) и [ZalithLauncher/ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2), с использованием компонентов PojavLauncher и других сторонних библиотек. Это происхождение указано для атрибуции и не означает одобрение или официальную связь.

Не удаляйте `LICENSE`, copyright-заголовки, сторонние notices и соответствующий исходный код при распространении. Смотрите [LICENSE_RU.md](LICENSE_RU.md), [THIRD_PARTY_NOTICES_RU.md](THIRD_PARTY_NOTICES_RU.md) и [RELEASE_RU.md](RELEASE_RU.md).

## Ограничения

Microsoft OAuth client ID и CurseForge API key не включены. Debug APK подписан публичным тестовым ключом и не предназначен для доверенного production-релиза. Запуск Minecraft и отдельных модлоадеров на реальном устройстве ещё требует практической проверки.
