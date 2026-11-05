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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.ShopItem
import com.shub39.grit.core.avatar.ShopItemKind
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.LocalWindowSizeClass
import com.shub39.grit.shared.ui.WindowSize.Companion.isExpanded
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundScene
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.nav_arrow_back
import grit.shared.ui.generated.resources.shop_gear
import grit.shared.ui.generated.resources.shop_outfits
import grit.shared.ui.generated.resources.shop_seasonal
import grit.shared.ui.generated.resources.shop_title
import grit.shared.ui.generated.resources.shop_training_ground
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

/** The tabs of the shop page; seasonal items get their own tab, whatever kind they are. */
private enum class ShopTab(val title: StringResource) {
    OUTFITS(Res.string.shop_outfits),
    GEAR(Res.string.shop_gear),
    TRAINING_GROUND(Res.string.shop_training_ground),
    SEASON(Res.string.shop_seasonal);

    fun items(offered: List<ShopItem>): List<ShopItem> =
        when (this) {
            OUTFITS -> offered.filter { it.season == null && it.kind == ShopItemKind.OUTFIT }
            GEAR -> offered.filter { it.season == null && it.kind == ShopItemKind.GEAR }
            TRAINING_GROUND -> offered.filter { it.season == null && it.kind == ShopItemKind.PROP }
            SEASON -> offered.filter { it.season != null }
        }
}

/**
 * Own page for the shop, opened from the Avatar tab: the fighter on top shows at once what it
 * wears, below it one tab per kind of item (#7). The "Season" tab only shows while seasonal items
 * are for sale or after some were bought.
 */
@Composable
fun AvatarShopPage(
    avatar: AvatarState?,
    onAction: (HabitsAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    var selected by rememberSaveable { mutableIntStateOf(ShopTab.OUTFITS.ordinal) }
    var itemToBuy by remember { mutableStateOf<ShopItem?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            colors =
                TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = Color.Transparent,
                    containerColor = Color.Transparent,
                ),
            title = {
                Text(text = stringResource(Res.string.shop_title), fontFamily = flexFontEmphasis())
            },
            navigationIcon = {
                FilledTonalIconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.nav_arrow_back),
                        contentDescription = "Navigate Back",
                    )
                }
            },
            actions = {
                avatar?.let {
                    ShakeChip(energy = it.energy, modifier = Modifier.padding(end = 8.dp))
                }
            },
            windowInsets =
                if (windowSizeClass.isExpanded()) {
                    WindowInsets(0)
                } else {
                    TopAppBarDefaults.windowInsets
                },
        )

        if (avatar == null) return@Column

        val offered = avatar.offered
        val tabs = ShopTab.entries.filter { it != ShopTab.SEASON || it.items(offered).isNotEmpty() }
        val current = tabs.firstOrNull { it.ordinal == selected } ?: ShopTab.OUTFITS

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                TrainingGroundScene(
                    data = avatar.data,
                    rank = avatar.fighter.rank,
                    mood = avatar.fighter.mood,
                    weather = avatar.fighter.weather,
                    modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                )
            }

            item {
                PrimaryTabRow(
                    selectedTabIndex = tabs.indexOf(current),
                    containerColor = Color.Transparent,
                    modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth(),
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = tab == current,
                            onClick = { selected = tab.ordinal },
                            text = { Text(stringResource(tab.title), maxLines = 1) },
                        )
                    }
                }
            }

            item {
                ShopItemList(
                    avatar = avatar,
                    items = current.items(offered),
                    onBuyClick = { itemToBuy = it },
                    onWear = { item, worn -> onAction(HabitsAction.WearShopItem(item.id, worn)) },
                )
            }
        }
    }

    val buying = itemToBuy
    if (avatar != null && buying != null) {
        BuyItemDialog(
            avatar = avatar,
            item = buying,
            onConfirm = {
                onAction(HabitsAction.BuyShopItem(buying.id))
                itemToBuy = null
            },
            onDismiss = { itemToBuy = null },
        )
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun AvatarShopPagePreview() {
    AvatarShopPage(
        avatar =
            AvatarState(
                fighter =
                    FighterState(
                        activeDays = 32,
                        rank = FighterRank.FIGHTER,
                        mood = FighterMood.READY,
                    ),
                energy = 30,
                data =
                    AvatarData(
                        purchases =
                            listOf(
                                Purchase(FighterItems.GI_BLUE, LocalDate(2026, 10, 1), 10),
                                Purchase(FighterItems.SWEATBAND, LocalDate(2026, 10, 2), 10),
                            ),
                        outfit = FighterItems.GI_BLUE,
                        accessories = setOf(FighterItems.SWEATBAND),
                    ),
            ),
        onAction = {},
        onNavigateBack = {},
    )
}
