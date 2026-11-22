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

package com.movtery.zalithlauncher.ui.screens.content

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.version.installed.PlayTimeRepository
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarkdownView
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.defaultMarkdownConfig
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.style.ListTheme
import com.iffly.compose.markdown.style.MarkdownTheme
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountAvatar
import com.movtery.zalithlauncher.ui.screens.content.elements.CommonVersionInfoLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.AboutDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.SideBar
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.help.HelpCenterDialog
import com.movtery.zalithlauncher.ui.screens.content.help.HelpTopic
import com.movtery.zalithlauncher.ui.screens.game.elements.PerformanceSettingsDialog
import com.movtery.zalithlauncher.ui.screens.game.elements.PerformanceSettingsOperation
import com.movtery.zalithlauncher.ui.screens.main.custom_home.MarkdownBlock
import com.movtery.zalithlauncher.ui.screens.main.custom_home.customHomePage

import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.removeAndNavigateTo
import com.movtery.zalithlauncher.utils.PlayTimeUtils
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.HomePageState
import com.movtery.zalithlauncher.viewmodel.LocalHomePageViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    onOpenLink: (String) -> Unit,
    onHomePageEvent: (MarkdownBlock.Button.Event) -> Unit,
    onNavigateToStats: () -> Unit = {},
    onNavigateToPlayTimeStats: () -> Unit = {},
    onNavigateToLog: (String) -> Unit = {},
) {
    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        var showAboutDialog by remember { mutableStateOf(false) }
        var showHelp by remember { mutableStateOf(false) }
        var helpTopic by remember { mutableStateOf<HelpTopic?>(null) }
        var showNjolOnboarding by remember { mutableStateOf(!AllSettings.njolOnboardingCompleted.state) }
        var showPreflight by remember { mutableStateOf(false) }
        var pendingLaunchVersion by remember { mutableStateOf<Version?>(null) }
        var crashNotice by remember { mutableStateOf(AllSettings.njolLastCrashNotice.state) }
        var performanceSettingsState by remember { mutableStateOf<PerformanceSettingsOperation>(PerformanceSettingsOperation.None) }

        if (showAboutDialog) {
            AboutDialog(onDismissRequest = { showAboutDialog = false })
        }
        if (showHelp) {
            HelpCenterDialog(
                onDismissRequest = { showHelp = false; helpTopic = null },
                initialTopic = helpTopic,
                openLink = onOpenLink
            )
        }
        if (showNjolOnboarding) {
            NjolOnboardingDialog(
                onDismissRequest = {
                    AllSettings.njolOnboardingCompleted.save(true)
                    showNjolOnboarding = false
                }
            )
        }
        if (showPreflight) {
            NjolPreflightDialog(
                onDismissRequest = {
                    showPreflight = false
                    pendingLaunchVersion = null
                },
                onContinue = {
                    showPreflight = false
                    val version = pendingLaunchVersion
                    pendingLaunchVersion = null
                    onLaunchGame(version)
                }
            )
        }

        PerformanceSettingsDialog(
            operation = performanceSettingsState,
            onDismissRequest = { performanceSettingsState = PerformanceSettingsOperation.None }
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            SideBar(
                modifier = Modifier
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                isVisible = isVisible,
                onFpsClick = {
                    performanceSettingsState = PerformanceSettingsOperation.Fps
                },
                onVersionsClick = {
                    backStackViewModel.mainScreen.removeAndNavigateTo(
                        remove = NestedNavKey.VersionSettings::class,
                        screenKey = NormalNavKey.VersionsManager
                    )
                },
                onInfoClick = {
                    showAboutDialog = true
                },
                onBackgroundClick = {
                    val settings = NestedNavKey.Settings().apply {
                        backStack.clear()
                        backStack.add(NormalNavKey.Settings.Launcher)
                    }
                    backStackViewModel.mainScreen.removeAndNavigateTo(
                        remove = NestedNavKey.Settings::class,
                        screenKey = settings
                    )
                }
            )

            CompositionLocalProvider(
                LocalUriHandler provides object : UriHandler {
                    override fun openUri(uri: String) {
                        onOpenLink(uri)
                    }
                }
            ) {
                ContentMenu(
                    modifier = Modifier.weight(7f),
                    isVisible = isVisible,
                    onHomePageEvent = onHomePageEvent,
                    onNavigateToStats = onNavigateToStats,
                    onNavigateToPlayTimeStats = onNavigateToPlayTimeStats,
                    onNavigateToLog = onNavigateToLog,
                    onProjectClick = { helpTopic = null; showHelp = true },
                    crashNotice = crashNotice,
                    onDismissCrashNotice = {
                        AllSettings.njolLastCrashNotice.save("")
                        crashNotice = ""
                    },
                    onOpenOnboarding = { showNjolOnboarding = true },
                    onOpenCreatorLab = { helpTopic = HelpTopic.CREATOR_LAB; showHelp = true }
                )
            }

            val toAccountManageScreen: () -> Unit = {
                backStackViewModel.mainScreen.navigateTo(
                    screenKey = NormalNavKey.AccountManager(FirstLoginMenu.NONE)
                )
            }
            val toVersionManageScreen: () -> Unit = {
                backStackViewModel.mainScreen.removeAndNavigateTo(
                    remove = NestedNavKey.VersionSettings::class,
                    screenKey = NormalNavKey.VersionsManager
                )
            }
            val toVersionSettingsScreen: () -> Unit = {
                VersionsManager.currentVersion.value?.let { version ->
                    navigateToVersions(version)
                }
            }
            RightMenu(
                isVisible = isVisible,
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
                    .padding(top = 12.dp, end = 12.dp, bottom = 12.dp),
                onLaunchGame = { version ->
                    if (AllSettings.njolPreflightEnabled.state) {
                        pendingLaunchVersion = version
                        showPreflight = true
                    } else {
                        onLaunchGame(version)
                    }
                },
                toAccountManageScreen = toAccountManageScreen,
                toVersionManageScreen = toVersionManageScreen,
                toVersionSettingsScreen = toVersionSettingsScreen,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ContentMenu(
    isVisible: Boolean,
    onHomePageEvent: (MarkdownBlock.Button.Event) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToPlayTimeStats: () -> Unit = {},
    onNavigateToLog: (String) -> Unit,
    onProjectClick: () -> Unit,
    crashNotice: String,
    onDismissCrashNotice: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onOpenCreatorLab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    val homePageViewModel = LocalHomePageViewModel.current
    val pageState by homePageViewModel.pageState.collectAsStateWithLifecycle()
    val config = defaultMarkdownConfig()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (BuildConfig.DEBUG) {
            item {
            //debug版本关不掉的警告，防止有人把测试版当正式版用 XD
            BackgroundCard(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.generic_warning),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.launcher_version_debug_warning, BuildKeys.LAUNCHER_NAME),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        modifier = Modifier
                            .alpha(0.8f)
                            .align(Alignment.End),
                        text = stringResource(R.string.launcher_version_debug_warning_cant_close),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            }
        }

        if (crashNotice.isNotBlank()) {
            item {
                NjolCrashNoticeCard(
                    notice = crashNotice,
                    onDismiss = onDismissCrashNotice
                )
            }
        }
        item { ProjectBanner(onClick = onProjectClick) }
        item { CreatorLabCard(onClick = onOpenCreatorLab) }
        item { NjolControlCard(onOpenOnboarding = onOpenOnboarding) }
        item {
            StatsGrid(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                onNavigateToStats = onNavigateToStats,
                onNavigateToPlayTimeStats = onNavigateToPlayTimeStats,
                onNavigateToLog = onNavigateToLog
            )
        }

        // Keep configured local/URL home pages in the same scrolling list.
        when (val state = pageState) {
            is HomePageState.Blank -> {}
            is HomePageState.Loading -> {
                item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LoadingIndicator()
                        Text(
                            text = stringResource(R.string.settings_launcher_home_page_loading),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                }
            }
            is HomePageState.None -> {
                customHomePage(
                    blocks = state.page,
                    config = config,
                    onEvent = onHomePageEvent
                )
            }
        }
    }
}

@Composable
private fun NjolControlCard(onOpenOnboarding: () -> Unit) {
    var reduceMotion by remember { mutableStateOf(AllSettings.njolReduceMotion.state) }
    var preflight by remember { mutableStateOf(AllSettings.njolPreflightEnabled.state) }
    BackgroundCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("NJOL CONTROL", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
            Text("Готовим запуск без сюрпризов", style = MaterialTheme.typography.titleMedium)
            Text(
                "Здесь только реальные локальные проверки: версия Android, память и состояние конфигурации. Лаунчер ничего не оптимизирует автоматически.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.78f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reduce Motion", style = MaterialTheme.typography.bodyMedium)
                    Text("Отключить переходы и лишние эффекты", style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.68f))
                }
                Switch(
                    checked = reduceMotion,
                    onCheckedChange = {
                        reduceMotion = it
                        AllSettings.njolReduceMotion.save(it)
                    }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Preflight перед запуском", style = MaterialTheme.typography.bodyMedium)
                    Text("Показать краткую диагностику устройства", style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.68f))
                }
                Switch(
                    checked = preflight,
                    onCheckedChange = {
                        preflight = it
                        AllSettings.njolPreflightEnabled.save(it)
                    }
                )
            }
            Button(onClick = onOpenOnboarding, modifier = Modifier.fillMaxWidth()) {
                Text("Открыть знакомство с NJOL")
            }
        }
    }
}

@Composable
private fun NjolCrashNoticeCard(
    notice: String,
    onDismiss: () -> Unit,
) {
    BackgroundCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("NJOL CRASH INSIGHT", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp, color = MaterialTheme.colorScheme.error)
            Text("Последний запуск завершился с ошибкой", style = MaterialTheme.typography.titleMedium)
            Text(notice, style = MaterialTheme.typography.bodySmall)
            Text(
                "Подсказка основана только на локальном crash-log и списке модов текущей игры. Это рекомендация, а не доказательство единственной причины.",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.alpha(0.7f)
            )
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Понятно")
            }
        }
    }
}

@Composable
private fun NjolPreflightDialog(
    onDismissRequest: () -> Unit,
    onContinue: () -> Unit,
) {
    val runtime = Runtime.getRuntime()
    val availableRamMb = runtime.maxMemory() / (1024 * 1024)
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("NJOL PREFLIGHT", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("Проверка перед запуском", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Это сводка доступных системных данных, а не автоматическая оптимизация.", style = MaterialTheme.typography.bodyMedium)
                Text("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                Text("Лимит памяти процесса: ${availableRamMb} МБ")
                Text("Preflight не сканирует файлы, миры или моды перед запуском. Анализ модов выполняется только после игрового краша и только в папке текущей игры.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.72f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDismissRequest, modifier = Modifier.weight(1f)) {
                        Text("Отмена")
                    }
                    Button(onClick = onContinue, modifier = Modifier.weight(1f)) {
                        Text("Продолжить")
                    }
                }
            }
        }
    }
}

@Composable
private fun NjolOnboardingDialog(onDismissRequest: () -> Unit) {
    var page by remember { mutableStateOf(0) }
    val titles = listOf("Добро пожаловать в NJOL", "Ваши версии и аккаунты на месте", "Контроль вместо магии")
    val descriptions = listOf(
        "NapoleJavaON Launcher сохраняет привычный офлайн-режим и поддержку игровых загрузчиков, но добавляет понятный фирменный интерфейс.",
        "Управляйте версиями, Java, рендерами и аккаунтами как раньше. NJOL не удаляет данные и не меняет их автоматически.",
        "Preflight показывает только доступные сведения устройства. Performance Lab даёт рекомендации, а не применяет рискованные оптимизации без вашего решения."
    )
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("NJOL  /  1.0", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
                Text(titles[page], style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(descriptions[page], style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.alpha(0.65f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(titles.size) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (index == page) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDismissRequest, modifier = Modifier.weight(1f)) {
                        Text("Пропустить")
                    }
                    Button(
                        onClick = {
                            if (page == titles.lastIndex) onDismissRequest() else page++
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (page == titles.lastIndex) "Готово" else "Далее")
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatorLabCard(onClick: () -> Unit) {
    BackgroundCard(
        modifier = Modifier.fillMaxWidth().height(154.dp),
        shape = RoundedCornerShape(22.dp),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.njol_creator_lab),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.92f),
                        0.62f to Color.Black.copy(alpha = 0.48f),
                        1f to Color.Transparent
                    )
                )
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("NJOL  /  CREATOR LAB", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Создавай моды для Minecraft Java", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Termux или GitHub + AI — пошаговый маршрут внутри NJOL", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
                }
                Text("Открыть Creator Lab →", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProjectBanner(onClick: () -> Unit) {
    BackgroundCard(
        modifier = Modifier.fillMaxWidth().height(164.dp),
        shape = RoundedCornerShape(22.dp),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.njol_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.88f),
                        0.6f to Color.Black.copy(alpha = 0.45f),
                        1f to Color.Black.copy(alpha = 0.18f)
                    )
                )
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "NJOL  /  LAUNCHER",
                    color = Color.White.copy(alpha = 0.76f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.4.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "NapoleJavaON",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(R.string.njol_project_action),
                        color = Color.White.copy(alpha = 0.86f),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right_rounded),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp).size(22.dp)
            )
        }
    }
}

private const val CHANGELOGS_URL = "https://raw.githubusercontent.com/Star1xr/ZalithLauncher2Plus/refs/heads/main/CHANGELOGS_UPDATE.md"
private const val CHANGELOGS_UPDATE_TR = "https://raw.githubusercontent.com/Star1xr/ZalithLauncher2Plus/refs/heads/main/CHANGELOGS_UPDATE_TR.md"

@Composable
private fun StatsGrid(
    modifier: Modifier = Modifier,
    onNavigateToStats: () -> Unit,
    onNavigateToPlayTimeStats: () -> Unit = {},
    onNavigateToLog: (String) -> Unit,
) {
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val versionNames = remember(versions) { versions.map { it.getVersionName() } }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.stats_today_header),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .alpha(0.5f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeeklyPlayTimeChart(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                versionNames = versionNames
            )
            ChangelogCard(
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DailyPlayTimeCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                versionNames = versionNames,
                onClick = onNavigateToPlayTimeStats
            )
            LastLogCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onNavigateToLog = onNavigateToLog
            )
        }
    }
}

@Composable
private fun WeeklyPlayTimeChart(
    modifier: Modifier = Modifier,
    versionNames: List<String>
) {
    val weekData = remember(versionNames) {
        val dates = PlayTimeRepository.lastNDays(7).reversed()
        dates.map { date ->
            date to PlayTimeRepository.getDailyTotalPlayTime(date, versionNames)
        }
    }

    val maxMs = remember(weekData) { weekData.maxOfOrNull { it.second } ?: 1L }
    val primaryColor = MaterialTheme.colorScheme.primary

    val dayLabels = remember(weekData) {
        val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        weekData.map { (date, _) ->
            val d = parser.parse(date)
            if (d != null) sdf.format(d) else date.takeLast(5)
        }
    }

    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weekData.forEachIndexed { index, (_, ms) ->
                    val fraction = if (maxMs > 0) ms.toFloat() / maxMs else 0f
                    val hours = PlayTimeUtils.getPlayHours(ms)
                    val barAlpha = 0.4f + (fraction * 0.6f).coerceAtMost(0.6f)

                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "%.1f".format(hours),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            modifier = Modifier.alpha(0.8f)
                        )
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .weight(fraction.coerceAtLeast(0.03f))
                                .background(
                                    color = primaryColor.copy(alpha = barAlpha),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(Modifier.weight((1f - fraction).coerceAtLeast(0.001f)))
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = dayLabels.getOrElse(index) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            modifier = Modifier.alpha(0.5f),
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.stats_play_time_graph),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.5f)
                    .padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun DailyPlayTimeCard(
    modifier: Modifier = Modifier,
    versionNames: List<String>,
    onClick: () -> Unit = {}
) {
    val todayMs = remember(versionNames) {
        PlayTimeRepository.getDailyTotalPlayTime(PlayTimeRepository.today(), versionNames)
    }
    val hours = PlayTimeUtils.getPlayHours(todayMs)

    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "%.1f h".format(hours),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    fontSize = 26.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally).alpha(0.7f),
                    text = stringResource(R.string.stats_today),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.weight(1f))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.stats_statistics),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.stats_click_for_more),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LastLogCard(
    modifier: Modifier = Modifier,
    onNavigateToLog: (String) -> Unit
) {
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val logFile = remember(currentVersion) {
        currentVersion?.let { VersionsManager.getLatestLog(it) }
    }
    val logExists = remember(logFile) { logFile?.exists() == true }

    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = {
            if (logExists) logFile?.absolutePath?.let { onNavigateToLog(it) }
        },
        enabled = logExists
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.stats_last_log),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
                if (!logExists || logFile == null) {
                    Text(
                        text = stringResource(R.string.stats_no_log),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.6f)
                    )
                } else {
                    Text(
                        text = remember(logFile) {
                            try {
                                logFile.readText().take(2000)
                            } catch (e: Exception) { "" }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .alpha(0.5f)
                            .bottomFade(36.dp)
                    )
                    Text(
                        text = stringResource(R.string.stats_click_for_more),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
            if (logExists) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.generic_open_link),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RightMenuContent(
    modifier: Modifier = Modifier,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
    launchButton: @Composable (
        innerModifier: Modifier,
        onClick: () -> Unit,
        text: @Composable RowScope.() -> Unit
    ) -> Unit,
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    ConstraintLayout(
        modifier = modifier
    ) {
        val (accountAvatar, versionManagerLayout, launchButton) = createRefs()

        AccountAvatar(
            modifier = Modifier
                .constrainAs(accountAvatar) {
                    top.linkTo(parent.top)
                    bottom.linkTo(launchButton.top, margin = 32.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            account = account,
            onClick = toAccountManageScreen
        )

        var showList by remember { mutableStateOf(false) }
        var versionManagerRow by remember { mutableStateOf<LayoutCoordinates?>(null) }
        Box(
            modifier = Modifier.constrainAs(versionManagerLayout) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(launchButton.top)
            },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            versionManagerRow = coordinates
                        }
                ) {
                    VersionManagerLayout(
                        isRefreshing = isRefreshing,
                        version = version,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        swapToVersionManage = toVersionManageScreen,
                        openListMenu = { showList = true },
                    )
                }
                version?.takeIf { !isRefreshing && it.isValid() }?.let {
                    IconButton(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = toVersionSettingsScreen
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_filled),
                            contentDescription = stringResource(R.string.versions_manage_settings)
                        )
                    }
                }
            }

            val menuAnchor = versionManagerRow
            val menuAnchorBounds = menuAnchor?.boundsInParent()
            val menuAnchorX = menuAnchorBounds?.left ?: 0f
            val menuAnchorHeight = menuAnchorBounds?.height ?: 0f

            DropdownMenu(
                expanded = showList && menuAnchor != null,
                onDismissRequest = { showList = false },
                modifier = Modifier.width(260.dp),
                offset = DpOffset(
                    x = with(LocalDensity.current) { menuAnchorX.toDp() },
                    y = with(LocalDensity.current) { (-menuAnchorHeight).toDp() } - 8.dp
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                val versions by VersionsManager.versions.collectAsStateWithLifecycle()
                versions.forEach { version0 ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CommonVersionInfoLayout(
                                    modifier = Modifier.weight(1f),
                                    version = version0,
                                    iconSize = 28.dp
                                )
                                IconButton(
                                    onClick = {
                                        onLaunchGame(version0)
                                        showList = false
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                                        contentDescription = stringResource(R.string.main_launch_game),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            if (version == version0) return@DropdownMenuItem
                            VersionsManager.saveVersion(version0)
                            showList = false
                        }
                    )
                }
            }
        }

        launchButton(
            Modifier
                .fillMaxWidth()
                .constrainAs(launchButton) {
                    bottom.linkTo(parent.bottom, margin = 8.dp)
                }
                .padding(PaddingValues(horizontal = 12.dp)),
            {
                onLaunchGame(null)
            },
            {
                Icon(
                    painter = painterResource(R.drawable.ic_play_arrow_filled),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                MarqueeText(text = stringResource(R.string.main_launch_game))
            }
        )
    }
}

@Composable
private fun RightMenu(
    isVisible: Boolean,
    onLaunchGame: (Version?) -> Unit,
    modifier: Modifier = Modifier,
    toAccountManageScreen: () -> Unit = {},
    toVersionManageScreen: () -> Unit = {},
    toVersionSettingsScreen: () -> Unit = {}
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        RightMenuContent(
            modifier = Modifier.fillMaxSize(),
            onLaunchGame = onLaunchGame,
            toAccountManageScreen = toAccountManageScreen,
            toVersionManageScreen = toVersionManageScreen,
            toVersionSettingsScreen = toVersionSettingsScreen,
        ) { innerModifier, onClick, text ->
            ScalingActionButton(
                modifier = innerModifier,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                onClick = onClick,
                content = text
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VersionManagerLayout(
    isRefreshing: Boolean,
    version: Version?,
    swapToVersionManage: () -> Unit,
    openListMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .combinedClickable(
                role = Role.Button,
                onClick = swapToVersionManage,
                onLongClick = {
                    if (version != null) openListMenu()
                }
            )
            .padding(PaddingValues(all = 8.dp))
    ) {
        if (isRefreshing) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LoadingIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        } else {
            VersionIconImage(
                version = version,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (version == null) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .basicMarquee(iterations = Int.MAX_VALUE),
                    text = stringResource(R.string.versions_manage_no_versions),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                    if (version.isValid()) {
                        Text(
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            text = version.getVersionSummary(),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangelogCard(
    modifier: Modifier = Modifier
) {
    var content by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    val isTurkey = LocalConfiguration.current.locales[0].language == "tr"
    val changelogUrl = if (isTurkey) CHANGELOGS_UPDATE_TR else CHANGELOGS_URL

    LaunchedEffect(Unit) {
        try {
            val text = withContext(Dispatchers.IO) {
                java.net.URL(changelogUrl).readText()
            }
            content = text
        } catch (_: Exception) {
            content = null
        }
        isLoading = false
    }

    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = { if (content != null) showDialog = true }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            ) {
                val isTablet = LocalConfiguration.current.screenWidthDp >= 600

                Text(
                    text = stringResource(R.string.stats_changelog),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
                when {
                    isLoading -> {
                        Text(
                            text = stringResource(R.string.stats_changelog_loading),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }
                    content == null -> {
                        Text(
                            text = stringResource(R.string.generic_error),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }
                    else -> {
                        val contentText = content!!
                        val previewText = remember(contentText) {
                            val lines = contentText.lines()
                            if (lines.size <= 2) contentText
                            else lines.dropLast(2).joinToString("\n")
                        }
                        val bodySize = if (isTablet) MaterialTheme.typography.bodySmall.fontSize else MaterialTheme.typography.labelSmall.fontSize
                        val primary = MaterialTheme.colorScheme.primary
                        val onSurface = MaterialTheme.colorScheme.onSurface
                        val cardConfig = remember(bodySize, primary, onSurface) {
                            MarkdownRenderConfig.Builder()
                                .markdownTheme(
                                    MarkdownTheme(
                                        textStyle = TextStyle(fontSize = bodySize, lineHeight = bodySize * 1.4f, color = onSurface),
                                        headStyle = mapOf(
                                            1 to TextStyle(fontSize = bodySize * 1.2f, lineHeight = bodySize * 1.5f, fontWeight = FontWeight.Bold, color = primary),
                                            2 to TextStyle(fontSize = bodySize * 1.1f, lineHeight = bodySize * 1.4f, fontWeight = FontWeight.Bold, color = primary),
                                            3 to TextStyle(fontSize = bodySize, lineHeight = bodySize * 1.3f, fontWeight = FontWeight.SemiBold, color = primary),
                                        ),
                                        listTheme = ListTheme(
                                            markerTextStyle = TextStyle(
                                                fontSize = bodySize,
                                                lineHeight = bodySize * 1.4f,
                                                textAlign = TextAlign.End,
                                                color = onSurface,
                                            ),
                                        ),
                                    )
                                )
                                .build()
                        }
                        MarkdownView(
                            content = previewText,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .bottomFade(48.dp),
                            config = cardConfig,
                        )
                        Text(
                            text = stringResource(R.string.stats_click_for_more),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    if (showDialog && content != null) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val dialogConfig = defaultMarkdownConfig()
                    MarkdownView(
                        content = "# ${stringResource(R.string.stats_changelog)}\n\n${content!!}",
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        config = dialogConfig
                    )
                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(text = stringResource(R.string.generic_close))
                    }
                }
            }
        }
    }
}

private fun Modifier.bottomFade(edgeHeight: androidx.compose.ui.unit.Dp): Modifier = this
    .graphicsLayer {
        clip = true
        compositingStrategy = CompositingStrategy.Offscreen
    }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startY = size.height - edgeHeight.toPx(),
                endY = size.height
            ),
            blendMode = BlendMode.DstIn,
            size = size
        )
    }
