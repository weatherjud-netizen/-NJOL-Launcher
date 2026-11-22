# Авторы и сторонние компоненты

NapoleJavaON Launcher (NJOL) — неофициальная модификация, основанная на [Star1xr/ZalithLauncher2Plus](https://github.com/Star1xr/ZalithLauncher2Plus), который указан как форк [ZalithLauncher/ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2). Благодарим Star1xr, MovTery и всех участников проекта и переводчиков. Исходные сведения об авторстве и лицензировании сохранены в [README.md](README.md), [LICENSE](LICENSE), заголовках исходных файлов и истории соответствующих проектов.

Исходный README указывает, что проект использует компоненты запуска [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/tree/v3_openjdk/app_pojavlauncher/src/main/jni). В исходниках также присутствуют ссылки на [Hello Minecraft! Launcher](https://github.com/HMCL-dev/HMCL) и других авторов. Сохраняйте исходные уведомления и ссылки в самих файлах; этот документ не заменяет их.

## Где находятся существующие уведомления

- [ZalithLauncher/src/main/java/com/movtery/zalithlauncher/library/_Libraries.kt](ZalithLauncher/src/main/java/com/movtery/zalithlauncher/library/_Libraries.kt) — исходный список библиотек, авторов и заявленных лицензий. Он полезен как указатель, но не является проверенной полной ведомостью всех зависимостей и их транзитивных компонентов.
- [ZalithLauncher/src/main/res/raw/](ZalithLauncher/src/main/res/raw/) — сохранённые тексты лицензий, включая ANGLE, Apache 2.0, ByteHook, gl4es, LGPL 3, LWJGL, Mesa, MMKV, OpenNBT и другие.
- [Terracotta/src/main/res/raw/terracotta_license.txt](Terracotta/src/main/res/raw/terracotta_license.txt) — текст лицензии Terracotta. В реестре проекта Terracotta указан как AGPL-3.0; область применения и требования необходимо оценить для конкретной поставки.
- Заголовки [mod_data.txt](ZalithLauncher/src/main/assets/mod_data.txt) и [modpack_data.txt](ZalithLauncher/src/main/assets/modpack_data.txt) содержат уведомления HMCL и mcmod.cn, включая «All Rights Reserved». Нельзя предполагать, что одна GPL проекта автоматически охватывает все данные и ресурсы.
- [gradle/libs.versions.toml](gradle/libs.versions.toml), файлы `build.gradle.kts`, `ZalithLauncher/libs/`, `LWJGL/libs/`, `Terracotta/src/main/jniLibs/` и `ZalithLauncher/src/main/assets/` помогают составить фактическую ведомость компонентов конкретной сборки.

## Проверка перед распространением

В проект включены готовые `.jar`, `.aar`, `.so` и архивы JRE. Для каждого компонента, попадающего в APK, проверьте его точную версию, право распространения, применимые лицензионные тексты и уведомления, а также наличие соответствующих исходников и инструкций там, где они требуются. Отдельно проверьте лицензии LGPL, GPL и AGPL, возможные обязанности по возможности замены/перекомпоновки и правовой режим наборов данных. Учитывайте и транзитивные зависимости, получаемые Gradle.

Этот файл не заявляет, что все готовые бинарные компоненты исследованы или что этот репозиторий уже содержит полный соответствующий исходный код. Не удаляйте и не скрывайте существующие кредитные экраны и лицензионные материалы. Если для компонента нет подтверждённых прав или исходников, до распространения найдите их у правообладателя или пересмотрите комплект поставки.
