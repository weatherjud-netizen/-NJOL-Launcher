# Участие в разработке NJOL

Спасибо за вклад в NapoleJavaON Launcher (NJOL). Это изменённый Android-лаунчер для Minecraft: Java Edition, распространяемый под GPLv3.

## Перед изменением кода

1. Проверьте [README.md](README.md), [LICENSE](LICENSE), [LICENSE_RU.md](LICENSE_RU.md) и [THIRD_PARTY_NOTICES_RU.md](THIRD_PARTY_NOTICES_RU.md).
2. Не удаляйте copyright-заголовки, лицензионные тексты, notices сторонних компонентов или сведения о происхождении кода.
3. Не меняйте совместимые package names, storage/auth identity и форматы данных без отдельного плана миграции.
4. Не добавляйте ключи, пароли, OAuth client ID или API-токены в Git.

## Проверка изменений

```bash
git diff --check
./gradlew --no-daemon :ZalithLauncher:testDebugUnitTest
./gradlew --no-daemon :ZalithLauncher:assembleDebug -Darch=all
```

Изменения пользовательского интерфейса должны сохранять доступность, плавную навигацию и работу на устройствах от API 26. Новые кнопки должны открывать реально существующие экраны или безопасные внешние ссылки, а не обещать ещё не реализованные функции.

## Атрибуция

NJOL основан на GPLv3-коде [Star1xr/ZalithLauncher2Plus](https://github.com/Star1xr/ZalithLauncher2Plus), использующем работу [ZalithLauncher/ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2), PojavLauncher и другие компоненты. Это происхождение должно оставаться отражённым в юридических документах и notices проекта.
