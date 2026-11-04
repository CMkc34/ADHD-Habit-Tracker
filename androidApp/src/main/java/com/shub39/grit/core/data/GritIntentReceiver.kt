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
package com.shub39.grit.core.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.shub39.grit.core.GritLogger
import com.shub39.grit.core.data.notification.GritNotificationManager
import com.shub39.grit.core.habits.HabitRepo
import com.shub39.grit.core.habits.HabitStatus
import com.shub39.grit.core.interfaces.AlarmScheduler
import com.shub39.grit.core.interfaces.IntentActions
import com.shub39.grit.core.interfaces.SettingsDatastore
import com.shub39.grit.core.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class GritIntentReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val TAG = "GritIntentReceiver"
    }

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        GritLogger.d(TAG, "Received intent")
        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                val datastore = get<SettingsDatastore>()
                val pauseNotifications = datastore.getNotificationsFlow().first()

                if (intent != null && !pauseNotifications) {
                    when (intent.action) {
                        IntentActions.HABIT_NOTIFICATION.action -> habitNotification(intent)

                        IntentActions.ADD_HABIT_STATUS.action -> addHabitStatus(intent)

                        else -> return@launch
                    }
                }
            } catch (t: Throwable) {
                GritLogger.e(TAG, "Error: ", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun addHabitStatus(intent: Intent) {
        GritLogger.d(TAG, "Add habit status intent received")
        val habitId = intent.getLongExtra("habit_id", -1)
        if (habitId < 0) {
            GritLogger.e(TAG, "Invalid Habit Id: $habitId")
            return
        }

        val habitRepo = get<HabitRepo>()
        val habit = habitRepo.getHabitById(habitId)
        if (habit == null) {
            GritLogger.e(TAG, "Habit not found for Id: $habitId")
            return
        }

        try {
            val resultsBundle = RemoteInput.getResultsFromIntent(intent)
            val inputString =
                resultsBundle?.getCharSequence(GritNotificationManager.KEY_HABIT_INPUT)?.toString()

            val loggedValue =
                if (habit.isMeasurable) {
                    val parsedValue = inputString?.toDoubleOrNull()
                    if (parsedValue == null || parsedValue <= 0.0) {
                        GritLogger.e(
                            TAG,
                            "Invalid progress input for measurable habit: '$inputString'",
                        )
                        return
                    }
                    parsedValue
                } else {
                    1.0
                }

            habitRepo.insertHabitStatus(
                HabitStatus(habitId = habitId, date = LocalDate.now(), value = loggedValue)
            )

            GritLogger.d(TAG, "Habit status added successfully with value: $loggedValue")
        } finally {
            get<GritNotificationManager>().cancelNotification(habitId.toInt())
        }
    }

    private suspend fun habitNotification(intent: Intent) {
        GritLogger.d(TAG, "Habit notification intent received")

        val habitId = intent.getLongExtra("habit_id", -1)
        if (habitId < 0L) {
            GritLogger.e(TAG, "Invalid Habit Id: $habitId")
            return
        }

        val habitRepo = get<HabitRepo>()

        val habit = habitRepo.getHabitById(habitId)

        if (habit == null) {
            GritLogger.e(TAG, "Invalid Habit Id: $habitId")
            return
        }
        if (!habit.reminder) {
            GritLogger.e(TAG, "Reminders are disabled for habit: ${habit.title}")
            return
        }

        // check if habit is completed today, if not then show notification
        val habitStatus = habitRepo.getStatusForHabit(habitId)
        if (habitStatus.any { it.date == LocalDate.now() }) {
            GritLogger.d(TAG, "Habit already completed today")
        } else {
            get<GritNotificationManager>().habitNotification(habit)
        }

        get<AlarmScheduler>().schedule(habit)
    }
}
