/*
 * Copyright (C) 2026  Shubham Gorai
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.shub39.grit.shared.ui.habit.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.Weather
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.LocalWindowSizeClass
import com.shub39.grit.shared.ui.WindowSize.Companion.isExpanded
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.fighter.GROUND_ASPECT_RATIO
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundPixels
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundScene
import com.shub39.grit.shared.ui.habit.fighter.label
import com.shub39.grit.shared.ui.habit.ui.component.WATER_MARK
import com.shub39.grit.shared.ui.habit.ui.component.WaterText
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.fighter_rank_highest
import grit.shared.ui.generated.resources.fighter_rank_to_next
import grit.shared.ui.generated.resources.fighter_tired
import grit.shared.ui.generated.resources.shop_open
import grit.shared.ui.generated.resources.shopping_bag
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

// the training ground never gets wider than this, on tablets it stands in the middle of the page
private val MAX_SCENE_WIDTH = 600.dp

// room for the bar on top of the sky, the sun and the clouds start below it
private val TOP_BAR_SPACE = 76.dp

// the fighter is drawn bigger on the full page, but never bigger than half the scene's height
private const val MAX_FIGHTER_SCALE = TrainingGroundPixels.MAX_FIGHTER_SCALE

// the open stat sheet never covers more of the page than this
private const val MAX_SHEET_SHARE = 0.75f

/**
 * The Fighter tab (#9): the training ground fills the page. On top sit the water balance, the
 * fighter's name with its rank and progress (tap to rename) and the button to the shop, which has
 * its own page. At the bottom the stat sheet shows the four stats and can be pulled up for details;
 * the training ground ends behind its closed height, so the fighter always stays in view. Rank-up
 * and level-up notices are shown app-wide by [FighterNoticeHost].
 */
@Composable
fun AvatarPage(
    avatar: AvatarState?,
    routines: Map<FighterStat, List<String>>,
    onAction: (HabitsAction) -> Unit,
    onOpenShop: () -> Unit,
    onOpenRoutines: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var showRename by remember { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    // height of the closed sheet, the training ground ends there
    var closedSheetHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (avatar == null) return@BoxWithConstraints

        val expandedWindow = LocalWindowSizeClass.current.isExpanded()
        val statusBar =
            if (expandedWindow) 0.dp
            else WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val pageHeight = maxHeight

        BoxWithConstraints(
            modifier =
                Modifier.fillMaxSize()
                    // the scene runs on under the sheet's rounded corners
                    .padding(bottom = maxOf(closedSheetHeight - SHEET_CORNER, 0.dp))
                    .clipToBounds(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val width = minOf(maxWidth, MAX_SCENE_WIDTH)
            // one ground height, the unit TrainingGroundScene draws in
            val unit = width / GROUND_ASPECT_RATIO
            val sceneHeight = maxHeight / unit
            val fighterScale = (sceneHeight / 2f).coerceIn(1f, MAX_FIGHTER_SCALE)

            TrainingGroundScene(
                data = avatar.data,
                rank = avatar.fighter.rank,
                mood = avatar.fighter.mood,
                weather = avatar.fighter.weather,
                fighterScale = fighterScale,
                sceneHeight = sceneHeight,
                skyTop = (statusBar + TOP_BAR_SPACE) / unit,
                shape = RectangleShape,
                modifier = Modifier.width(width),
            )
        }

        Column(
            modifier =
                Modifier.align(Alignment.TopCenter)
                    .padding(top = statusBar + 8.dp, start = 12.dp, end = 12.dp)
                    .widthIn(max = MAX_SCENE_WIDTH)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AvatarTopBar(
                avatar = avatar,
                onRenameClick = { showRename = true },
                onOpenShop = onOpenShop,
            )

            if (avatar.fighter.mood == FighterMood.TIRED) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                ) {
                    Text(
                        text = stringResource(Res.string.fighter_tired, avatar.name),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }

        FighterStatSheet(
            fighter = avatar.fighter,
            routines = routines,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onOpenRoutines = onOpenRoutines,
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .widthIn(max = MAX_SCENE_WIDTH)
                    .heightIn(max = pageHeight * MAX_SHEET_SHARE)
                    .onSizeChanged {
                        if (!expanded) closedSheetHeight = with(density) { it.height.toDp() }
                    },
        )
    }

    if (avatar != null && showRename) {
        RenameAvatarDialog(
            currentName = avatar.data.name,
            species = avatar.data.look,
            onSave = {
                onAction(HabitsAction.RenameAvatar(it))
                showRename = false
            },
            onDismiss = { showRename = false },
        )
    }
}

/** Water on the left, the name sign in the middle, the shop button on the right. */
@Composable
private fun AvatarTopBar(avatar: AvatarState, onRenameClick: () -> Unit, onOpenShop: () -> Unit) {
    val fighter = avatar.fighter
    val paper = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WaterChip(energy = avatar.energy)

        Surface(
            onClick = onRenameClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = paper,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "${fighterName(avatar.data)} ✎",
                    style =
                        MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LinearProgressIndicator(
                    progress = { fighter.rankProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                )
                val daysToNext = fighter.daysToNextRank
                val next = fighter.rank.next
                Text(
                    text =
                        if (daysToNext == null || next == null)
                            stringResource(Res.string.fighter_rank_highest, fighter.rank.label())
                        else
                            pluralStringResource(
                                Res.plurals.fighter_rank_to_next,
                                daysToNext,
                                fighter.rank.label(),
                                daysToNext,
                                next.label(),
                            ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        FilledIconButton(onClick = onOpenShop, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = vectorResource(Res.drawable.shopping_bag),
                contentDescription = stringResource(Res.string.shop_open),
            )
        }
    }
}

/** The water balance as a small paper pill, for the Avatar and the shop page. */
@Composable
internal fun WaterChip(energy: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
    ) {
        WaterText(
            text = "$WATER_MARK $energy",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

private val previewRoutines =
    mapOf(
        FighterStat.STRENGTH to listOf("Push-ups"),
        FighterStat.SPEED to listOf("Morning walk"),
        FighterStat.STAMINA to listOf("Drink water", "Sleep by 11"),
        FighterStat.FOCUS to emptyList(),
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun AvatarPageFreshInstallPreview() {
    AvatarPage(
        avatar =
            AvatarState(
                fighter =
                    FighterState(
                        activeDays = 0,
                        rank = FighterRank.ROOKIE,
                        mood = FighterMood.TIRED,
                    ),
                energy = 0,
                data = AvatarData(),
            ),
        routines = FighterStat.entries.associateWith { emptyList() },
        onAction = {},
        onOpenShop = {},
        onOpenRoutines = {},
    )
}

private val trainedAvatar =
    AvatarState(
        fighter =
            FighterState(
                activeDays = 100,
                rank = FighterRank.CHAMPION,
                mood = FighterMood.READY,
                weather = Weather.SUNNY,
                xp =
                    mapOf(
                        FighterStat.STRENGTH to 48,
                        FighterStat.SPEED to 17,
                        FighterStat.STAMINA to 80,
                    ),
            ),
        energy = 30,
        data =
            AvatarData(
                name = "Fridolin",
                purchases =
                    listOf(
                        Purchase(FighterItems.GI_BLUE, LocalDate(2026, 10, 1), 40),
                        Purchase(FighterItems.SWEATBAND, LocalDate(2026, 10, 2), 15),
                        Purchase(FighterItems.MAT, LocalDate(2026, 10, 3), 20),
                    ),
                outfit = FighterItems.GI_BLUE,
                accessories = setOf(FighterItems.SWEATBAND),
                decorations = setOf(FighterItems.MAT),
            ),
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun AvatarPageTrainedPreview() {
    AvatarPage(
        avatar = trainedAvatar,
        routines = previewRoutines,
        onAction = {},
        onOpenShop = {},
        onOpenRoutines = {},
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun AvatarPageStatsOpenPreview() {
    AvatarPage(
        avatar = trainedAvatar,
        routines = previewRoutines,
        onAction = {},
        onOpenShop = {},
        onOpenRoutines = {},
        initiallyExpanded = true,
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 1000, heightDp = 700)
@Composable
private fun AvatarPageTabletPreview() {
    AvatarPage(
        avatar = trainedAvatar,
        routines = previewRoutines,
        onAction = {},
        onOpenShop = {},
        onOpenRoutines = {},
    )
}
