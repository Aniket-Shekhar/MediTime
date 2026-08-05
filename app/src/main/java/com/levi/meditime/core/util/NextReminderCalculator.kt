package com.levi.meditime.core.util

import com.levi.meditime.domain.model.RepeatType
import java.util.Calendar

/**
 * Utility to calculate the next trigger time for recurring reminders.
 */
object NextReminderCalculator {

    /**
     * Calculates the next occurrence based on the current trigger time and repeat type.
     * 
     * @param currentTriggerTime The time (in millis) when the alarm just fired.
     * @param repeatType The frequency selected by the user.
     * @param selectedDays The list of days selected if repeatType is SPECIFIC_DAYS.
     * @return The next time (in millis) the alarm should fire.
     */
    fun nextTriggerTime(currentTriggerTime: Long, repeatType: RepeatType, selectedDays: List<Int> = emptyList()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTriggerTime
        }

        when (repeatType) {
            RepeatType.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            RepeatType.WEEKDAYS -> {
                // If Fri, Sat, Sun -> jump to Mon. Otherwise +1 day.
                do {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                } while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
                         calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            }
            RepeatType.WEEKENDS -> {
                // If Sun -> jump to Sat. Otherwise +1 day (if Sat) or find next Sat.
                do {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                } while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && 
                         calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            }
            RepeatType.SPECIFIC_DAYS -> {
                // PRD 1.1: If specific days are selected, find the next matching day
                if (selectedDays.isNotEmpty()) {
                    do {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    } while (!selectedDays.contains(calendar.get(Calendar.DAY_OF_WEEK)))
                } else {
                    // Fallback to daily if no days selected
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        return calendar.timeInMillis
    }
}
