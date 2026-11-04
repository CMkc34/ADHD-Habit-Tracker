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
package com.shub39.grit.shared.ui.fakes

import com.shub39.grit.core.AnalyticsEvent
import com.shub39.grit.core.activity.ActivityRepo
import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.DailyBonus
import com.shub39.grit.core.activity.DayStatus
import com.shub39.grit.core.activity.FreezeNotice
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarRepo
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.PlantState
import com.shub39.grit.core.avatar.PurchaseCheck
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitRepo
import com.shub39.grit.core.habits.HabitStatus
import com.shub39.grit.core.habits.HabitWithAnalytics
import com.shub39.grit.core.habits.MeasurableTargets
import com.shub39.grit.core.habits.OverallAnalytics
import com.shub39.grit.core.habits.Target
import com.shub39.grit.core.habits.style.RoutineStyle
import com.shub39.grit.core.habits.style.RoutineStyleStore
import com.shub39.grit.core.interfaces.AlarmScheduler
import com.shub39.grit.core.interfaces.AnalyticsWrapper
import com.shub39.grit.core.interfaces.SettingsDatastore
import com.shub39.grit.core.motivation.MotivationSettings
import com.shub39.grit.core.now
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * In-memory [HabitRepo]. The current streak is not calculated: tests set it per habit in
 * [currentStreaks], so they check how the caller uses the repository's value. Like the real
 * repository, a check-in marks today as active in [activityRepo] and check-ins update its
 * [DailyBonus] days.
 */
class FakeHabitRepo(habits: List<Habit>, private val activityRepo: FakeActivityRepo? = null) :
    HabitRepo {
    private val habits = MutableStateFlow(habits)
    private val statuses = MutableStateFlow(emptyList<HabitStatus>())

    val currentStreaks = mutableMapOf<Long, Int>()

    override suspend fun upsertHabit(habit: Habit): Long {
        habits.update { list -> list.filterNot { it.id == habit.id } + habit }
        return habit.id
    }

    override suspend fun deleteHabit(habitId: Long) {
        habits.update { list -> list.filterNot { it.id == habitId } }
    }

    override suspend fun getHabits(): List<Habit> = habits.value

    override suspend fun getHabitById(id: Long): Habit? = habits.value.find { it.id == id }

    override suspend fun getHabitStatuses(): List<HabitStatus> = statuses.value

    override fun getHabitsWithAnalytics(): Flow<List<HabitWithAnalytics>> =
        statuses.map { all ->
            habits.value.map { habit ->
                withAnalytics(habit, all.filter { it.habitId == habit.id })
            }
        }

    override fun getCompletedHabitIds(): Flow<List<Long>> = flowOf(emptyList())

    override fun getOverallAnalytics(): Flow<OverallAnalytics> = flowOf(OverallAnalytics())

    override fun getHabitsWithStatus(): Flow<List<Pair<Habit, Boolean>>> = flowOf(emptyList())

    override suspend fun getStatusForHabit(id: Long): List<HabitStatus> =
        statuses.value.filter { it.habitId == id }

    override suspend fun insertHabitStatus(habitStatus: HabitStatus) {
        statuses.update { it + habitStatus }
        activityRepo?.markActive(LocalDate.now())
        updateBonusDays()
    }

    override suspend fun deleteHabitStatus(habitId: Long, date: LocalDate) {
        statuses.update { list -> list.filterNot { it.habitId == habitId && it.date == date } }
        updateBonusDays()
    }

    private fun updateBonusDays() {
        activityRepo?.report?.update {
            it.copy(bonusDays = DailyBonus.days(habits.value, statuses.value))
        }
    }

    override suspend fun getCompletedHabitsForDate(date: LocalDate): Map<Habit, Double?> =
        emptyMap()

    private fun withAnalytics(habit: Habit, statuses: List<HabitStatus>): HabitWithAnalytics {
        val noTarget = Target(progress = 0.0, target = 0.0)
        return HabitWithAnalytics(
            habit = habit,
            consistency = 0f,
            statuses = statuses,
            weeklyComparisonData = emptyList(),
            weekDayFrequencyData = emptyMap(),
            measurableTargets = MeasurableTargets(noTarget, noTarget, noTarget, noTarget, noTarget),
            currentStreak = currentStreaks[habit.id] ?: 0,
            bestStreak = 0,
            startedDaysAgo = 0,
        )
    }
}

class FakeSettingsDatastore : SettingsDatastore {
    override fun getStartOfTheWeekPref(): Flow<DayOfWeek> = flowOf(DayOfWeek.MONDAY)

    override suspend fun setStartOfWeek(day: DayOfWeek) {}

    override fun getIs24Hr(): Flow<Boolean> = flowOf(true)

    override suspend fun setIs24Hr(pref: Boolean) {}

    override fun getNotificationsFlow(): Flow<Boolean> = flowOf(false)

    override suspend fun setNotifications(pref: Boolean) {}

    override fun getBiometricLockPref(): Flow<Boolean> = flowOf(false)

    override suspend fun setBiometricPref(pref: Boolean) {}

    override fun getCompactViewPref(): Flow<Boolean> = flowOf(false)

    override suspend fun setCompactView(pref: Boolean) {}
}

class FakeMotivationSettings : MotivationSettings {
    val popupEnabled = MutableStateFlow(true)

    override fun getPopupEnabled(): Flow<Boolean> = popupEnabled

    override suspend fun setPopupEnabled(enabled: Boolean) {
        popupEnabled.value = enabled
    }

    val greetingEnabled = MutableStateFlow(true)
    val lastGreetingDay = MutableStateFlow<LocalDate?>(null)

    override fun getGreetingEnabled(): Flow<Boolean> = greetingEnabled

    override suspend fun setGreetingEnabled(enabled: Boolean) {
        greetingEnabled.value = enabled
    }

    override fun getLastGreetingDay(): Flow<LocalDate?> = lastGreetingDay

    override suspend fun setLastGreetingDay(day: LocalDate) {
        lastGreetingDay.value = day
    }

    val setupDone = MutableStateFlow(false)

    override fun getSetupDone(): Flow<Boolean> = setupDone

    override suspend fun setSetupDone() {
        setupDone.value = true
    }
}

/** [ActivityRepo] whose report and freeze notice tests set directly or via [markActive]. */
class FakeActivityRepo(
    val report: MutableStateFlow<ActivityReport> =
        MutableStateFlow(
            ActivityReport(currentStreak = 0, longestStreak = 0, energy = 0, days = emptyMap())
        ),
    val freezeNotice: MutableStateFlow<FreezeNotice?> = MutableStateFlow(null),
) : ActivityRepo {
    val seenFreezes = mutableSetOf<LocalDate>()

    override fun getActivityReport(): Flow<ActivityReport> = report

    override fun getFreezeNotice(): Flow<FreezeNotice?> = freezeNotice

    /** Marks [day] as active; the first time adds one day to the streak and +1 energy. */
    fun markActive(day: LocalDate) {
        report.update { report ->
            if (report.days[day] == DayStatus.ACTIVE) return@update report
            report.copy(
                currentStreak = report.currentStreak + 1,
                energy = report.energy + 1,
                days = report.days + (day to DayStatus.ACTIVE),
            )
        }
    }

    override suspend fun setAutoFreeze(enabled: Boolean) =
        report.update { it.copy(autoFreeze = enabled) }

    override suspend fun markFreezesSeen(days: Set<LocalDate>) {
        seenFreezes += days
        freezeNotice.update { notice ->
            notice?.takeUnless { it.frozenDays.all(seenFreezes::contains) }
        }
    }
}

/**
 * [AvatarRepo] on top of [activityRepo]. The shop is not simulated: buying always succeeds and is
 * recorded in [bought], wearing treats every item as an accessory.
 */
class FakeAvatarRepo(
    private val activityRepo: FakeActivityRepo = FakeActivityRepo(),
    val data: MutableStateFlow<AvatarData> = MutableStateFlow(AvatarData()),
) : AvatarRepo {
    override fun getAvatarState(): Flow<AvatarState> =
        combine(activityRepo.report, data) { report, data ->
            AvatarState(PlantState.of(report), report.energy, data)
        }

    val bought = mutableListOf<String>()

    override suspend fun buy(itemId: String): PurchaseCheck {
        bought += itemId
        return PurchaseCheck.OK
    }

    override suspend fun rename(name: String) = data.update { it.copy(name = name) }

    override suspend fun setUserName(name: String) = data.update { it.copy(userName = name) }

    override suspend fun setSpecies(species: AvatarSpecies) =
        data.update { it.copy(species = species) }

    override suspend fun wear(itemId: String, worn: Boolean) =
        data.update {
            it.copy(accessories = if (worn) it.accessories + itemId else it.accessories - itemId)
        }

    override suspend fun markStageSeen(stage: PlantStage) =
        data.update { it.copy(seenStage = stage.number) }
}

class FakeAlarmScheduler : AlarmScheduler {
    override fun schedule(habit: Habit) {}

    override fun cancel(habit: Habit) {}

    override fun cancelAll() {}
}

class FakeAnalytics : AnalyticsWrapper {
    override fun trackEvent(event: AnalyticsEvent, properties: Map<String, Any>) {}
}

class FakeRoutineStyleStore : RoutineStyleStore {
    val styles = MutableStateFlow<Map<Long, RoutineStyle>>(emptyMap())

    override fun getStyles(): Flow<Map<Long, RoutineStyle>> = styles

    override suspend fun setStyle(habitId: Long, style: RoutineStyle) =
        styles.update { it + (habitId to style) }

    override suspend fun deleteStyle(habitId: Long) = styles.update { it - habitId }
}
