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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.PlantState
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.PurchaseCheck
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.core.avatar.ShopItem
import com.shub39.grit.core.avatar.ShopItemKind
import com.shub39.grit.core.avatar.preview
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.components.GritDialog
import com.shub39.grit.shared.ui.habit.ui.component.WaterText
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.avatar_cancel
import grit.shared.ui.generated.resources.shop_buy
import grit.shared.ui.generated.resources.shop_buy_for
import grit.shared.ui.generated.resources.shop_choose
import grit.shared.ui.generated.resources.shop_chosen
import grit.shared.ui.generated.resources.shop_close
import grit.shared.ui.generated.resources.shop_confirm_text
import grit.shared.ui.generated.resources.shop_confirm_title
import grit.shared.ui.generated.resources.shop_from_stage
import grit.shared.ui.generated.resources.shop_item_acc_bird
import grit.shared.ui.generated.resources.shop_item_acc_birdhouse
import grit.shared.ui.generated.resources.shop_item_acc_blossoms
import grit.shared.ui.generated.resources.shop_item_acc_butterfly
import grit.shared.ui.generated.resources.shop_item_acc_fairy_lights
import grit.shared.ui.generated.resources.shop_item_acc_fruits
import grit.shared.ui.generated.resources.shop_item_acc_ladybug
import grit.shared.ui.generated.resources.shop_item_acc_swing
import grit.shared.ui.generated.resources.shop_item_acc_watering_can
import grit.shared.ui.generated.resources.shop_item_deco_bench
import grit.shared.ui.generated.resources.shop_item_deco_fence
import grit.shared.ui.generated.resources.shop_item_deco_hedgehog
import grit.shared.ui.generated.resources.shop_item_deco_lantern
import grit.shared.ui.generated.resources.shop_item_deco_mushrooms
import grit.shared.ui.generated.resources.shop_item_deco_pond
import grit.shared.ui.generated.resources.shop_item_deco_rabbit
import grit.shared.ui.generated.resources.shop_item_deco_snail
import grit.shared.ui.generated.resources.shop_item_pot_blue
import grit.shared.ui.generated.resources.shop_item_pot_gold
import grit.shared.ui.generated.resources.shop_item_pot_green
import grit.shared.ui.generated.resources.shop_item_pot_purple
import grit.shared.ui.generated.resources.shop_item_pot_terracotta
import grit.shared.ui.generated.resources.shop_item_pot_yellow
import grit.shared.ui.generated.resources.shop_low_energy
import grit.shared.ui.generated.resources.shop_out_of_season
import grit.shared.ui.generated.resources.shop_preview
import grit.shared.ui.generated.resources.shop_preview_missing
import grit.shared.ui.generated.resources.shop_preview_stage
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
        "pot_terracotta" -> Res.string.shop_item_pot_terracotta
        "pot_blue" -> Res.string.shop_item_pot_blue
        "pot_yellow" -> Res.string.shop_item_pot_yellow
        "pot_green" -> Res.string.shop_item_pot_green
        "pot_purple" -> Res.string.shop_item_pot_purple
        "pot_gold" -> Res.string.shop_item_pot_gold
        "acc_watering_can" -> Res.string.shop_item_acc_watering_can
        "acc_ladybug" -> Res.string.shop_item_acc_ladybug
        "acc_butterfly" -> Res.string.shop_item_acc_butterfly
        "acc_blossoms" -> Res.string.shop_item_acc_blossoms
        "acc_bird" -> Res.string.shop_item_acc_bird
        "acc_birdhouse" -> Res.string.shop_item_acc_birdhouse
        "acc_fruits" -> Res.string.shop_item_acc_fruits
        "acc_fairy_lights" -> Res.string.shop_item_acc_fairy_lights
        "acc_swing" -> Res.string.shop_item_acc_swing
        "deco_mushrooms" -> Res.string.shop_item_deco_mushrooms
        "deco_fence" -> Res.string.shop_item_deco_fence
        "deco_snail" -> Res.string.shop_item_deco_snail
        "deco_hedgehog" -> Res.string.shop_item_deco_hedgehog
        "deco_rabbit" -> Res.string.shop_item_deco_rabbit
        "deco_bench" -> Res.string.shop_item_deco_bench
        "deco_pond" -> Res.string.shop_item_deco_pond
        "deco_lantern" -> Res.string.shop_item_deco_lantern
        else -> null
    }

@Composable
private fun itemName(item: ShopItem): String = item.nameRes()?.let { stringResource(it) } ?: item.id

private fun ShopItem.emoji(): String =
    when (id) {
        "acc_watering_can" -> "💧"
        "acc_ladybug" -> "🐞"
        "acc_butterfly" -> "🦋"
        "acc_blossoms" -> "🌸"
        "acc_bird" -> "🐦"
        "acc_birdhouse" -> "🏠"
        "acc_fruits" -> "🍎"
        "acc_fairy_lights" -> "✨"
        "acc_swing" -> "🪢"
        "deco_mushrooms" -> "🍄"
        "deco_fence" -> "🪵"
        "deco_snail" -> "🐌"
        "deco_hedgehog" -> "🦔"
        "deco_rabbit" -> "🐇"
        "deco_bench" -> "🪑"
        "deco_pond" -> "🪷"
        "deco_lantern" -> "🏮"
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
                    item.kind == ShopItemKind.POT_COLOR && worn ->
                        OutlinedButton(onClick = {}, enabled = false) {
                            Text(stringResource(Res.string.shop_chosen))
                        }
                    item.kind == ShopItemKind.POT_COLOR ->
                        FilledTonalButton(onClick = { onWear(true) }) {
                            Text(stringResource(Res.string.shop_choose))
                        }
                    item.kind == ShopItemKind.DECORATION && worn ->
                        OutlinedButton(onClick = { onWear(false) }) {
                            Text(stringResource(Res.string.shop_put_away))
                        }
                    item.kind == ShopItemKind.DECORATION ->
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
            PurchaseCheck.STAGE_TOO_LOW ->
                Text(
                    text = "🔒 " + stringResource(Res.string.shop_from_stage, item.minStage.number),
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
            ShopItemKind.POT_COLOR ->
                Box(
                    modifier =
                        Modifier.size(24.dp).background(potColorOf(item.id), shape = CircleShape)
                )
            ShopItemKind.ACCESSORY,
            ShopItemKind.DECORATION ->
                Text(text = item.emoji(), style = MaterialTheme.typography.titleLarge)
        }
    }
}

/**
 * Shows the meadow without and with [item] before buying it. Items that cannot be bought yet are
 * previewed too: a locked item on the plant grown to its stage, with a hint why buying is not
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
        MeadowScene(
            stage = preview.stage,
            mood = avatar.plant.mood,
            potColor = shown.potColor,
            accessories = shown.accessories,
            decorations = shown.decorations,
            weather = avatar.plant.weather,
            species = avatar.data.species,
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
            PurchaseCheck.STAGE_TOO_LOW ->
                Text(
                    text =
                        "🔒 " +
                            stringResource(
                                Res.string.shop_preview_stage,
                                preview.stage.number,
                                avatar.plant.stage.number,
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
        plant = PlantState(activeDays = 32, stage = PlantStage.YOUNG_PLANT, mood = PlantMood.HAPPY),
        energy = 30,
        data =
            AvatarData(
                purchases =
                    listOf(
                        Purchase("pot_blue", LocalDate(2026, 10, 1), 10),
                        Purchase("acc_ladybug", LocalDate(2026, 10, 2), 15),
                    ),
                potColor = "pot_blue",
                accessories = setOf("acc_ladybug"),
            ),
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun ShopItemListPreview() {
    ShopItemList(
        avatar = previewAvatar,
        items = ShopCatalog.items.filter { it.kind == ShopItemKind.ACCESSORY },
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
        item = ShopCatalog.byId("acc_blossoms")!!,
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
        item = ShopCatalog.byId("deco_pond")!!,
        onConfirm = {},
        onDismiss = {},
    )
}
