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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.PlantState
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.Weather
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.LocalWindowSizeClass
import com.shub39.grit.shared.ui.WindowSize.Companion.isExpanded
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.ui.component.WATER_MARK
import com.shub39.grit.shared.ui.habit.ui.component.WaterText
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.avatar_stage_short_days
import grit.shared.ui.generated.resources.avatar_stage_short_grown
import grit.shared.ui.generated.resources.avatar_tired
import grit.shared.ui.generated.resources.shop_open
import grit.shared.ui.generated.resources.shopping_bag
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

// the meadow never gets wider than this, on tablets it stands in the middle of the page
private val MAX_SCENE_WIDTH = 600.dp

// room for the bar on top of the sky, the sun and the clouds start below it
private val TOP_BAR_SPACE = 76.dp

// the plant is drawn bigger on the full page, but never bigger than half the scene's height
private const val MAX_PLANT_SCALE = 1.6f

/**
 * Own tab for the plant: the meadow fills the whole page. On top sit the water balance, the plant's
 * name with its progress (tap to rename) and the button to the shop, which has its own page.
 */
@Composable
fun AvatarPage(
    avatar: AvatarState?,
    onAction: (HabitsAction) -> Unit,
    onOpenShop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRename by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (avatar == null) return@Box

        val expanded = LocalWindowSizeClass.current.isExpanded()
        val statusBar =
            if (expanded) 0.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().clipToBounds(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val width = minOf(maxWidth, MAX_SCENE_WIDTH)
            // one meadow height, the unit MeadowScene draws in
            val unit = width / MEADOW_ASPECT_RATIO
            val sceneHeight = maxHeight / unit
            val plantScale = (sceneHeight / 2f).coerceIn(1f, MAX_PLANT_SCALE)

            MeadowScene(
                stage = avatar.plant.stage,
                mood = avatar.plant.mood,
                potColor = avatar.data.potColor,
                accessories = avatar.data.accessories,
                decorations = avatar.data.decorations,
                weather = avatar.plant.weather,
                species = avatar.data.species,
                plantScale = plantScale,
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

            if (avatar.plant.mood == PlantMood.TIRED) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                ) {
                    Text(
                        text = stringResource(Res.string.avatar_tired),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }

    if (avatar != null && showRename) {
        RenameAvatarDialog(
            currentName = avatar.data.name,
            species = avatar.data.species,
            onSave = {
                onAction(HabitsAction.RenameAvatar(it))
                showRename = false
            },
            onDismiss = { showRename = false },
        )
    }

    avatar?.newStage?.let { stage ->
        NewStageDialog(
            avatar = avatar,
            stage = stage,
            onDismiss = { onAction(HabitsAction.DismissNewStage) },
        )
    }
}

/** Water on the left, the name sign in the middle, the shop button on the right. */
@Composable
private fun AvatarTopBar(avatar: AvatarState, onRenameClick: () -> Unit, onOpenShop: () -> Unit) {
    val plant = avatar.plant
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
                    text = "${plantName(avatar.data)} ✎",
                    style =
                        MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LinearProgressIndicator(
                    progress = { plant.stageProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                )
                val daysToNext = plant.daysToNextStage
                Text(
                    text =
                        if (daysToNext == null)
                            stringResource(Res.string.avatar_stage_short_grown, plant.stage.number)
                        else
                            pluralStringResource(
                                Res.plurals.avatar_stage_short_days,
                                daysToNext,
                                plant.stage.number,
                                daysToNext,
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

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun AvatarPageFreshInstallPreview() {
    AvatarPage(
        avatar =
            AvatarState(
                plant = PlantState(activeDays = 0, stage = PlantStage.SEED, mood = PlantMood.TIRED),
                energy = 0,
                data = AvatarData(),
            ),
        onAction = {},
        onOpenShop = {},
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun AvatarPageWithPurchasesPreview() {
    AvatarPage(
        avatar =
            AvatarState(
                plant =
                    PlantState(
                        activeDays = 100,
                        stage = PlantStage.TREE,
                        mood = PlantMood.HAPPY,
                        weather = Weather.SUNNY,
                    ),
                energy = 30,
                data =
                    AvatarData(
                        name = "Fridolin",
                        purchases =
                            listOf(
                                Purchase("pot_blue", LocalDate(2026, 10, 1), 10),
                                Purchase("acc_ladybug", LocalDate(2026, 10, 2), 15),
                                Purchase("deco_rabbit", LocalDate(2026, 10, 3), 30),
                            ),
                        potColor = "pot_blue",
                        accessories = setOf("acc_ladybug"),
                        decorations = setOf("deco_rabbit"),
                    ),
            ),
        onAction = {},
        onOpenShop = {},
    )
}
