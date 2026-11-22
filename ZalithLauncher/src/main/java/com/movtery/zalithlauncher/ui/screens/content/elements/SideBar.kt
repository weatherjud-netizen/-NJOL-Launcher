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
package com.movtery.zalithlauncher.ui.screens.content.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.BackgroundCard

/** Compact shortcuts remain usable when collapsed, and the rail occupies only its visible width. */
@Composable
fun SideBar(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onFpsClick: () -> Unit,
    onVersionsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onBackgroundClick: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val width by animateDpAsState(
        targetValue = if (expanded) 154.dp else 56.dp,
        animationSpec = tween(220), label = "sidebarWidth"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(180), label = "sidebarVisibility"
    )

    BackgroundCard(
        modifier = modifier.width(width).fillMaxHeight().alpha(alpha),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 5.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Shortcut(
                icon = if (expanded) R.drawable.ic_arrow_left_rounded else R.drawable.ic_arrow_right_rounded,
                label = stringResource(if (expanded) R.string.njol_sidebar_collapse else R.string.njol_sidebar_expand),
                expanded = expanded,
                onClick = { expanded = !expanded }
            )
            Shortcut(R.drawable.ic_video_settings, stringResource(R.string.game_menu_option_fps_settings), expanded, onFpsClick)
            Shortcut(R.drawable.ic_assignment_filled, stringResource(R.string.page_title_version_manage), expanded, onVersionsClick)
            Shortcut(R.drawable.ic_image_outlined, stringResource(R.string.settings_launcher_background_title), expanded, onBackgroundClick)
            Shortcut(R.drawable.ic_info_outlined, stringResource(R.string.about_launcher_title), expanded, onInfoClick)
        }
    }
}

@Composable
private fun Shortcut(icon: Int, label: String, expanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(42.dp)
            .clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(46.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                modifier = Modifier.size(21.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
    }
}
