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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.PurchaseCheck
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.core.avatar.ShopItem
import com.shub39.grit.core.avatar.ShopItemKind
import com.shub39.grit.core.avatar.preview
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.components.GritDialog
import com.shub39.grit.shared.ui.habit.fighter.FighterPixels
import com.shub39.grit.shared.ui.habit.fighter.FighterShopScene
import com.shub39.grit.shared.ui.habit.fighter.PropIcon
import com.shub39.grit.shared.ui.habit.ui.component.WaterText
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.avatar_cancel
import grit.shared.ui.generated.resources.shop_buy
import grit.shared.ui.generated.resources.shop_buy_for
import grit.shared.ui.generated.resources.shop_choose
import grit.shared.ui.generated.resources.shop_close
import grit.shared.ui.generated.resources.shop_confirm_text
import grit.shared.ui.generated.resources.shop_confirm_title
import grit.shared.ui.generated.resources.shop_from_rank
import grit.shared.ui.generated.resources.shop_item_gi_white
import grit.shared.ui.generated.resources.shop_item_gi_blue
import grit.shared.ui.generated.resources.shop_item_gi_red
import grit.shared.ui.generated.resources.shop_item_gi_black
import grit.shared.ui.generated.resources.shop_item_gi_purple
import grit.shared.ui.generated.resources.shop_item_gi_gold
import grit.shared.ui.generated.resources.shop_item_gear_sweatband
import grit.shared.ui.generated.resources.shop_item_gear_gloves
import grit.shared.ui.generated.resources.shop_item_gear_sneakers
import grit.shared.ui.generated.resources.shop_item_gear_cape
import grit.shared.ui.generated.resources.shop_item_gear_sunglasses
import grit.shared.ui.generated.resources.shop_item_gear_champion_belt
import grit.shared.ui.generated.resources.shop_item_gear_crown
import grit.shared.ui.generated.resources.shop_item_prop_mat
import grit.shared.ui.generated.resources.shop_item_prop_water_bottle
import grit.shared.ui.generated.resources.shop_item_prop_punching_bag
import grit.shared.ui.generated.resources.shop_item_prop_dumbbells
import grit.shared.ui.generated.resources.shop_item_prop_wooden_dummy
import grit.shared.ui.generated.resources.shop_item_prop_banner
import grit.shared.ui.generated.resources.shop_item_prop_trophy_shelf
import grit.shared.ui.generated.resources.shop_item_prop_gong
import grit.shared.ui.generated.resources.shop_low_energy
import grit.shared.ui.generated.resources.shop_out_of_season
import grit.shared.ui.generated.resources.shop_preview
import grit.shared.ui.generated.resources.shop_preview_missing
import grit.shared.ui.generated.resources.shop_preview_rank
import grit.shared.ui.generated.resources.shop_preview_title
import grit.shared.ui.generated.resources.shop_preview_with
import grit.shared.ui.generated.resources.shop_preview_without
import grit.shared.ui.generated.resources.shop_put_away
import grit.shared.ui.generated.resources.shop_put_on
import grit.shared.ui.generated.resources.shop_set_up
import grit.shared.ui.generated.resources.shop_take_off
import grit.shared.ui.generated.resources.visibility
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private fun ShopItem.nameRes(): StringResource? =
    when (id) {
        FighterItems.GI_WHITE -> Res.string.shop_item_gi_white
        FighterItems.GI_BLUE -> Res.string.shop_item_gi_blue
        FighterItems.GI_RED -> Res.string.shop_item_gi_red
        FighterItems.GI_BLACK -> Res.string.shop_item_gi_black
        FighterItems.GI_PURPLE -> Res.string.shop_item_gi_purple
        FighterItems.GI_GOLD -> Res.string.shop_item_gi_gold
        FighterItems.SWEATBAND -> Res.string.shop_item_gear_sweatband
        FighterItems.GLOVES -> Res.string.shop_item_gear_gloves
        FighterItems.SNEAKERS -> Res.string.shop_item_gear_sneakers
        FighterItems.CAPE -> Res.string.shop_item_gear_cape
        FighterItems.SUNGLASSES -> Res.string.shop_item_gear_sunglasses
        FighterItems.CHAMPION_BELT -> Res.string.shop_item_gear_champion_belt
        FighterItems.CROWN -> Res.string.shop_item_gear_crown
        FighterItems.MAT -> Res.string.shop_item_prop_mat
        FighterItems.WATER_BOTTLE -> Res.string.shop_item_prop_water_bottle
        FighterItems.PUNCHING_BAG -> Res.string.shop_item_prop_punching_bag
        FighterItems.DUMBBELLS -> Res.string.shop_item_prop_dumbbells
        FighterItems.WOODEN_DUMMY -> Res.string.shop_item_prop_wooden_dummy
        FighterItems.BANNER -> Res.string.shop_item_prop_banner
        FighterItems.TROPHY_SHELF -> Res.string.shop_item_prop_trophy_shelf
        FighterItems.GONG -> Res.string.shop_item_prop_gong
        else -> null
    }

@Composable
private fun itemName(item: ShopItem): String = item.nameRes()?.let { stringResource(it) } ?: item.id

/** Gear has no pixel icon of its own yet; it is drawn on the fighter in the preview. */
private fun ShopItem.emoji(): String =
    when (id) {
        FighterItems.SWEATBAND -> "🎽"
        FighterItems.GLOVES -> "🥊"
        FighterItems.SNEAKERS -> "👟"
        FighterItems.CAPE -> "🦸"
        FighterItems.SUNGLASSES -> "🕶️"
        FighterItems.CHAMPION_BELT -> "🥇"
        FighterItems.CROWN -> "👑"
        else -> "🎁"
    }

/**
 * One tab of the shop page: [items] as rows. [onBuyClick] opens the buy dialog with its preview
 * (also from the eye button of items that cannot be bought yet), [onWear] puts an owned item on or
 * takes it off.
 */
@Composable
fun ShopItemList(
    avatar: AvatarState,
    items: List<ShopItem>,
    onBuyClick: (ShopItem) -> Unit,
    onWear: (ShopItem, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.widthIn(max = 500.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                ShopRow(
                    item = item,
                    check = avatar.check(item),
                    worn = avatar.isWorn(item),
                    onBuyClick = { onBuyClick(item) },
                    onWear = { onWear(item, it) },
                )
            }
        }
    }
}

@Composable
private fun ShopRow(
    item: ShopItem,
    check: PurchaseCheck,
    worn: Boolean,
    onBuyClick: () -> Unit,
    onWear: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ItemIcon(item)
        Text(
            text = itemName(item),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (check != PurchaseCheck.ALREADY_OWNED) {
            IconButton(onClick = onBuyClick) {
                Icon(
                    painter = painterResource(Res.drawable.visibility),
                    contentDescription = stringResource(Res.string.shop_preview),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when (check) {
            PurchaseCheck.ALREADY_OWNED ->
                when {
                    item.kind == ShopItemKind.OUTFIT && worn ->
                        OutlinedButton(onClick = { onWear(false) }) {
                            Text(stringResource(Res.string.shop_take_off))
                        }
                    item.kind == ShopItemKind.OUTFIT ->
                        FilledTonalButton(onClick = { onWear(true) }) {
                            Text(stringResource(Res.string.shop_choose))
                        }
                    item.kind == ShopItemKind.PROP && worn ->
                        OutlinedButton(onClick = { onWear(false) }) {
                            Text(stringResource(Res.string.shop_put_away))
                        }
                    item.kind == ShopItemKind.PROP ->
                        FilledTonalButton(onClick = { onWear(true) }) {
                            Text(stringResource(Res.string.shop_set_up))
                        }
                    worn ->
                        OutlinedButton(onClick = { onWear(false) }) {
                            Text(stringResource(Res.string.shop_take_off))
                        }
                    else ->
                        FilledTonalButton(onClick = { onWear(true) }) {
                            Text(stringResource(Res.string.shop_put_on))
                        }
                }
            PurchaseCheck.RANK_TOO_LOW ->
                Text(
                    text = "🔒 " + stringResource(Res.string.shop_from_rank, item.minRank.number),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            PurchaseCheck.OUT_OF_SEASON ->
                Text(
                    text = stringResource(Res.string.shop_out_of_season),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            PurchaseCheck.NOT_ENOUGH_ENERGY ->
                Button(onClick = {}, enabled = false) {
                    WaterText(stringResource(Res.string.shop_buy_for, item.price))
                }
            PurchaseCheck.OK ->
                Button(onClick = onBuyClick) {
                    WaterText(stringResource(Res.string.shop_buy_for, item.price))
                }
        }
    }
}

@Composable
private fun ItemIcon(item: ShopItem) {
    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
        when (item.kind) {
            ShopItemKind.OUTFIT ->
                Box(
                    modifier =
                        Modifier.size(24.dp)
                            .background(
                                Color(FighterPixels.outfitColor(item.id) ?: 0),
                                shape = CircleShape,
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            ShopItemKind.GEAR ->
                Text(text = item.emoji(), style = MaterialTheme.typography.titleLarge)
            ShopItemKind.PROP -> PropIcon(item.id, Modifier.size(32.dp))
        }
    }
}

/**
 * Shows the fighter without and with [item] before buying it. Items that cannot be bought yet are
 * previewed too: a locked item on the fighter at the rank it needs, with a hint why buying is not
 * possible. Warns when less than one freeze worth of energy would be left.
 */
@Composable
fun BuyItemDialog(
    avatar: AvatarState,
    item: ShopItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val preview = avatar.preview(item)
    val balanceAfter = avatar.energy - item.price
    var showWith by remember(item) { mutableStateOf(true) }
    val shown = if (showWith) preview.with else preview.without

    GritDialog(onDismissRequest = onDismiss) {
        FighterShopScene(
            data = shown,
            rank = preview.rank,
            mood = avatar.fighter.mood,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !showWith,
                onClick = { showWith = false },
                label = { Text(stringResource(Res.string.shop_preview_without)) },
            )
            FilterChip(
                selected = showWith,
                onClick = { showWith = true },
                label = { Text(stringResource(Res.string.shop_preview_with)) },
            )
        }
        Text(
            text =
                if (preview.canBuy) stringResource(Res.string.shop_confirm_title, itemName(item))
                else stringResource(Res.string.shop_preview_title, itemName(item)),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        when (preview.check) {
            PurchaseCheck.OK -> {
                WaterText(
                    text = stringResource(Res.string.shop_confirm_text, item.price, balanceAfter),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                if (balanceAfter < ActivityPlanner.FREEZE_COST) {
                    WaterText(
                        text =
                            stringResource(Res.string.shop_low_energy, ActivityPlanner.FREEZE_COST),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            PurchaseCheck.RANK_TOO_LOW ->
                Text(
                    text =
                        "🔒 " +
                            stringResource(
                                Res.string.shop_preview_rank,
                                preview.rank.number,
                                avatar.fighter.rank.number,
                            ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            PurchaseCheck.NOT_ENOUGH_ENERGY ->
                WaterText(
                    text = stringResource(Res.string.shop_preview_missing, preview.missingEnergy),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            PurchaseCheck.OUT_OF_SEASON ->
                Text(
                    text = stringResource(Res.string.shop_out_of_season),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            PurchaseCheck.ALREADY_OWNED -> Unit
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (preview.canBuy) {
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.avatar_cancel)) }
                TextButton(onClick = onConfirm) { Text(stringResource(Res.string.shop_buy)) }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.shop_close)) }
            }
        }
    }
}

private val previewAvatar =
    AvatarState(
        fighter =
            FighterState(activeDays = 32, rank = FighterRank.FIGHTER, mood = FighterMood.READY),
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
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun ShopItemListPreview() {
    ShopItemList(
        avatar = previewAvatar,
        items = ShopCatalog.items.filter { it.kind == ShopItemKind.GEAR },
        onBuyClick = {},
        onWear = { _, _ -> },
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun BuyItemDialogPreview() {
    BuyItemDialog(
        avatar = previewAvatar,
        item = ShopCatalog.byId(FighterItems.GLOVES)!!,
        onConfirm = {},
        onDismiss = {},
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun LockedItemPreviewDialogPreview() {
    BuyItemDialog(
        avatar = previewAvatar,
        item = ShopCatalog.byId(FighterItems.TROPHY_SHELF)!!,
        onConfirm = {},
        onDismiss = {},
    )
}
