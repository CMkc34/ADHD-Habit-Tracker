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
package com.shub39.grit.logic.avatar

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarStore
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.core.fighter.FighterLook
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.StatLevelCodec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single

/** Stores the fighter in the app's shared preferences file, under keys prefixed with `avatar_`. */
@Single(binds = [AvatarStore::class])
class AvatarStoreImpl(private val datastore: DataStore<Preferences>) : AvatarStore {

    companion object {
        private val nameKey = stringPreferencesKey("avatar_name")
        private val purchasesKey = stringSetPreferencesKey("avatar_purchases")
        // the worn outfit; the key is the plant's pot colour, whose ids are no outfits
        private val outfitKey = stringPreferencesKey("avatar_pot_color")
        private val accessoriesKey = stringSetPreferencesKey("avatar_accessories")
        private val decorationsKey = stringSetPreferencesKey("avatar_decorations")
        // the rank number; the key is the plant's, so ranks the plant had are not announced again
        private val seenStageKey = intPreferencesKey("avatar_seen_stage")
        private val userNameKey = stringPreferencesKey("avatar_user_name")
        // the FighterLook id, which keeps the plant species ids (sprout, cherry)
        private val speciesKey = stringPreferencesKey("avatar_species")
        // "statKey|level" entries, see StatLevelCodec
        private val seenStatLevelsKey = stringSetPreferencesKey("avatar_seen_stat_levels")

        private const val SEPARATOR = "|"

        // "itemId|2026-10-22|10"
        internal fun Purchase.encode(): String = listOf(itemId, date, price).joinToString(SEPARATOR)

        internal fun decodePurchase(value: String): Purchase? {
            val parts = value.split(SEPARATOR)
            if (parts.size != 3) return null
            val date = runCatching { LocalDate.parse(parts[1]) }.getOrNull() ?: return null
            val price = parts[2].toIntOrNull() ?: return null
            return Purchase(itemId = parts[0], date = date, price = price)
        }
    }

    override fun getAvatar(): Flow<AvatarData> =
        datastore.data.map { prefs ->
            AvatarData(
                name = prefs[nameKey].orEmpty(),
                purchases =
                    prefs[purchasesKey].orEmpty().mapNotNull(::decodePurchase).sortedBy { it.date },
                outfit = prefs[outfitKey]?.takeIf(ShopCatalog::isOutfit),
                accessories = prefs[accessoriesKey].orEmpty(),
                decorations = prefs[decorationsKey].orEmpty(),
                seenStage = prefs[seenStageKey] ?: 0,
                userName = prefs[userNameKey].orEmpty(),
                look = FighterLook.fromId(prefs[speciesKey]),
                seenStatLevels = StatLevelCodec.decodeAll(prefs[seenStatLevelsKey].orEmpty()),
            )
        }

    override suspend fun setName(name: String) {
        datastore.edit { it[nameKey] = name.trim() }
    }

    override suspend fun setUserName(name: String) {
        datastore.edit { it[userNameKey] = name }
    }

    override suspend fun setLook(look: FighterLook) {
        datastore.edit { it[speciesKey] = look.id }
    }

    override suspend fun addPurchase(purchase: Purchase) {
        datastore.edit { it[purchasesKey] = it[purchasesKey].orEmpty() + purchase.encode() }
    }

    override suspend fun setOutfit(itemId: String?) {
        datastore.edit { if (itemId == null) it.remove(outfitKey) else it[outfitKey] = itemId }
    }

    override suspend fun setAccessory(itemId: String, worn: Boolean) {
        datastore.edit {
            val accessories = it[accessoriesKey].orEmpty()
            it[accessoriesKey] = if (worn) accessories + itemId else accessories - itemId
        }
    }

    override suspend fun setDecoration(itemId: String, shown: Boolean) {
        datastore.edit {
            val decorations = it[decorationsKey].orEmpty()
            it[decorationsKey] = if (shown) decorations + itemId else decorations - itemId
        }
    }

    override suspend fun markStageSeen(stage: Int) {
        datastore.edit { it[seenStageKey] = maxOf(it[seenStageKey] ?: 0, stage) }
    }

    override suspend fun markStatLevelsSeen(levels: Map<FighterStat, Int>) {
        datastore.edit {
            val seen = StatLevelCodec.decodeAll(it[seenStatLevelsKey].orEmpty())
            val merged =
                (seen.keys + levels.keys).associateWith { stat ->
                    maxOf(seen[stat] ?: 0, levels[stat] ?: 0)
                }
            it[seenStatLevelsKey] = StatLevelCodec.encodeAll(merged)
        }
    }

    override suspend fun replaceAll(data: AvatarData) {
        datastore.edit {
            it[nameKey] = data.name
            it[purchasesKey] =
                data.purchases.mapTo(mutableSetOf()) { purchase -> purchase.encode() }
            data.outfit.let { outfit ->
                if (outfit == null) it.remove(outfitKey) else it[outfitKey] = outfit
            }
            it[accessoriesKey] = data.accessories
            it[decorationsKey] = data.decorations
            it[seenStageKey] = data.seenStage
            it[userNameKey] = data.userName
            it[speciesKey] = data.look.id
            it[seenStatLevelsKey] = StatLevelCodec.encodeAll(data.seenStatLevels)
        }
    }
}
