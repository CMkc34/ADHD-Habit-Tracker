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

import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.AvatarStore
import com.shub39.grit.core.avatar.Purchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeAvatarStore(initial: AvatarData = AvatarData()) : AvatarStore {
    val data = MutableStateFlow(initial)

    override fun getAvatar(): Flow<AvatarData> = data

    override suspend fun setName(name: String) {
        data.update { it.copy(name = name) }
    }

    override suspend fun setUserName(name: String) {
        data.update { it.copy(userName = name) }
    }

    override suspend fun setSpecies(species: AvatarSpecies) {
        data.update { it.copy(species = species) }
    }

    override suspend fun addPurchase(purchase: Purchase) {
        data.update { it.copy(purchases = it.purchases + purchase) }
    }

    override suspend fun setPotColor(itemId: String) {
        data.update { it.copy(potColor = itemId) }
    }

    override suspend fun setAccessory(itemId: String, worn: Boolean) {
        data.update {
            it.copy(accessories = if (worn) it.accessories + itemId else it.accessories - itemId)
        }
    }

    override suspend fun setDecoration(itemId: String, shown: Boolean) {
        data.update {
            it.copy(decorations = if (shown) it.decorations + itemId else it.decorations - itemId)
        }
    }

    override suspend fun markStageSeen(stage: Int) {
        data.update { it.copy(seenStage = maxOf(it.seenStage, stage)) }
    }

    override suspend fun replaceAll(data: AvatarData) {
        this.data.value = data
    }
}
