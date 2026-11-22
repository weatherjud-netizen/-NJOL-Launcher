/*
 * NJOL help center, based on Zalith Launcher 2.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.ui.screens.content.help

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import kotlinx.coroutines.launch

/** Stable entry points for embedding the help center in another Compose screen. */
enum class HelpTopic(@StringRes val title: Int, @StringRes val body: Int) {
    ABOUT(R.string.help_about_title, R.string.help_about_body),
    START(R.string.help_start_title, R.string.help_start_body),
    RENDERERS(R.string.help_renderers_title, R.string.help_renderers_body),
    KRYPTON(R.string.help_krypton_title, R.string.help_krypton_body),
    GRAPHICS(R.string.help_graphics_title, R.string.help_graphics_body),
    JAVA(R.string.help_java_title, R.string.help_java_body),
    PERFORMANCE(R.string.help_performance_title, R.string.help_performance_body),
    LOADERS(R.string.help_loaders_title, R.string.help_loaders_body),
    MODS(R.string.help_mods_title, R.string.help_mods_body),
    ACCOUNTS(R.string.help_accounts_title, R.string.help_accounts_body),
    PLUGINS(R.string.help_plugins_title, R.string.help_plugins_body),
    CREATOR_LAB(R.string.help_creator_lab_title, R.string.help_creator_lab_body),
    CONTROLS(R.string.help_controls_title, R.string.help_controls_body),
    LOGS(R.string.help_logs_title, R.string.help_logs_body),
    LEGAL(R.string.help_legal_title, R.string.help_legal_body)
}

private data class HelpLink(@StringRes val label: Int, val url: String)

private fun links(topic: HelpTopic): List<HelpLink> = when (topic) {
    HelpTopic.ABOUT -> listOf(
        HelpLink(R.string.help_link_zalith, "https://github.com/ZalithLauncher/ZalithLauncher2"),
        HelpLink(R.string.help_link_fork, "https://github.com/Star1xr/ZalithLauncher2Plus"),
        HelpLink(R.string.help_link_pojav, "https://github.com/PojavLauncherTeam/PojavLauncher")
    )
    HelpTopic.RENDERERS -> listOf(
        HelpLink(R.string.help_link_mobileglues, "https://github.com/MobileGL-Dev/MobileGlues-release")
    )
    HelpTopic.KRYPTON -> listOf(
        HelpLink(R.string.help_link_krypton, "https://github.com/BZLZHH/NG-GL4ES"),
        HelpLink(R.string.help_link_krypton_plugin, "https://github.com/BZLZHH/NGG-FCLRendererPlugin")
    )
    HelpTopic.PLUGINS -> listOf(
        HelpLink(R.string.help_link_native, "https://github.com/ZalithLauncher/NativeLibPlugin"),
        HelpLink(R.string.help_link_driver, "https://github.com/FCL-Team/FCLDriverPlugin")
    )
    HelpTopic.CREATOR_LAB -> listOf(
        HelpLink(R.string.help_link_termux, "https://github.com/termux/termux-app"),
        HelpLink(R.string.help_link_termux_fdroid, "https://f-droid.org/en/packages/com.termux/"),
        HelpLink(R.string.help_link_github, "https://github.com/"),
        HelpLink(R.string.help_link_codespaces, "https://docs.github.com/en/codespaces/about-codespaces/what-are-codespaces"),
        HelpLink(R.string.help_link_fabric_docs, "https://docs.fabricmc.net/develop/")
    )
    HelpTopic.LEGAL -> listOf(
        HelpLink(R.string.help_link_license, "https://github.com/Star1xr/ZalithLauncher2Plus/blob/main/LICENSE"),
        HelpLink(R.string.help_link_minecraft, "https://www.minecraft.net/")
    )
    else -> emptyList()
}

/**
 * Standalone content. A host may provide its own link dispatcher; without one, Android's
 * URI handler is used. [initialTopic] scrolls directly to a section when supplied.
 * The lazy list keeps only visible sections composed and loads no remote content.
 */
@Composable
fun HelpCenterScreen(
    modifier: Modifier = Modifier,
    initialTopic: HelpTopic? = null,
    openLink: ((String) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val linkError = stringResource(R.string.help_link_error)
    val topics = HelpTopic.entries

    LaunchedEffect(initialTopic) {
        initialTopic?.let { listState.scrollToItem(topics.indexOf(it)) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(stringResource(R.string.help_center_title), style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp).semantics { heading() })
        Text(stringResource(R.string.help_contents_hint), style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(topics, key = { _, topic -> topic.name }) { index, topic ->
                OutlinedButton(onClick = { scope.launch { listState.animateScrollToItem(index) } }) {
                    Text(stringResource(topic.title))
                }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(topics, key = { it.name }) { topic ->
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(topic.title), style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() })
                        Text(stringResource(topic.body), style = MaterialTheme.typography.bodyMedium)
                        links(topic).forEach { link ->
                            TextButton(onClick = {
                                if (openLink != null) openLink(link.url)
                                else try { uriHandler.openUri(link.url) } catch (_: Exception) {
                                    Toast.makeText(context, linkError, Toast.LENGTH_SHORT).show()
                                }
                            }) { Text(stringResource(link.label)) }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ready-to-use modal entry point for hosts without a navigation route. Example:
 * HelpCenterDialog(onDismissRequest = { showHelp = false }, initialTopic = HelpTopic.ABOUT)
 */
@Composable
fun HelpCenterDialog(
    onDismissRequest: () -> Unit,
    initialTopic: HelpTopic? = null,
    openLink: ((String) -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f).padding(horizontal = 12.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.help_close)) }
                }
                HelpCenterScreen(modifier = Modifier.weight(1f), initialTopic = initialTopic, openLink = openLink)
            }
        }
    }
}
