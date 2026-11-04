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
package com.shub39.grit.logic.core.backup

import com.shub39.grit.core.GritLogger
import com.shub39.grit.core.activity.ActivityData
import com.shub39.grit.core.activity.ActivityStore
import com.shub39.grit.core.avatar.AvatarStore
import com.shub39.grit.core.habits.HabitRepo
import com.shub39.grit.core.habits.style.RoutineStyleStore
import com.shub39.grit.core.interfaces.AlarmScheduler
import com.shub39.grit.core.settings.backup.RestoreRepo
import com.shub39.grit.core.settings.backup.RestoreResult
import com.shub39.grit.core.settings.backup.SchemaMismatchException
import com.shub39.grit.logic.habits.database.HabitDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single(binds = [RestoreRepo::class])
class RestoreImpl(
    private val habitRepo: HabitRepo,
    private val alarmScheduler: AlarmScheduler,
    private val activityStore: ActivityStore,
    private val avatarStore: AvatarStore,
    private val routineStyleStore: RoutineStyleStore,
) : RestoreRepo {
    override suspend fun restoreData(): RestoreResult {
        return try {
            val file =
                FileKit.openFilePicker(mode = FileKitMode.Single, type = FileKitType.File("json"))
                    ?: return RestoreResult.Failure(exceptionType = InvalidFile)

            val json = Json { ignoreUnknownKeys = true }

            val jsonDeserialized = json.decodeFromString<ExportSchema>(file.readString())

            // fork: tasks were removed, so task data and its schema version are ignored
            if (jsonDeserialized.habitsSchemaVersion != HabitDatabase.SCHEMA_VERSION) {
                throw SchemaMismatchException()
            }

            // restoring check-ins marks today active, which is not a real check-in
            val activityBefore = activityStore.getActivity().first()

            withContext(Dispatchers.IO) {
                habitRepo.getHabits().forEach { alarmScheduler.cancel(it) }

                jsonDeserialized.habits
                    .map { it.toHabit() }
                    .forEach {
                        habitRepo.upsertHabit(it)
                        alarmScheduler.schedule(it)
                    }

                jsonDeserialized.habitStatus
                    .map { it.toHabitStatus() }
                    .forEach { habitRepo.insertHabitStatus(it) }
            }

            activityStore.replaceAll(
                activityBefore.mergedWith(
                    jsonDeserialized.activity?.toActivityData() ?: ActivityData()
                )
            )

            jsonDeserialized.avatar?.let { backup ->
                avatarStore.replaceAll(
                    avatarStore.getAvatar().first().mergedWith(backup.toAvatarData())
                )
            }

            // habits keep their ids on import, so styles map onto them directly
            jsonDeserialized.routineStyles?.forEach {
                routineStyleStore.setStyle(it.habitId, it.toRoutineStyle())
            }

            RestoreResult.Success
        } catch (e: SchemaMismatchException) {
            GritLogger.e("RestoreRepo", "Failed to restore data, old schema: ", e)
            RestoreResult.Failure(OldSchema)
        } catch (e: SerializationException) {
            GritLogger.e("RestoreRepo", "Failed to deserialize, invalid file: ", e)
            RestoreResult.Failure(InvalidFile)
        }
    }
}
