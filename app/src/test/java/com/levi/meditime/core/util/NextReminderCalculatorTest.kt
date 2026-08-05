package com.levi.meditime.core.util

import com.levi.meditime.domain.model.RepeatType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class NextReminderCalculatorTest {

    private val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)

    @Test
    fun `DAILY repeat schedules for next day same time`() {
        val today = time("2026-07-21 08:00")
        val expected = time("2026-07-22 08:00")
        
        val actual = NextReminderCalculator.nextTriggerTime(today, RepeatType.DAILY)
        
        assertEquals(format.format(expected), format.format(actual))
    }

    @Test
    fun `WEEKDAYS repeat on Friday jumps to Monday`() {
        val friday = time("2026-07-24 08:00") // Friday
        val expectedMonday = time("2026-07-27 08:00") // Monday
        
        val actual = NextReminderCalculator.nextTriggerTime(friday, RepeatType.WEEKDAYS)
        
        assertEquals(format.format(expectedMonday), format.format(actual))
    }

    @Test
    fun `WEEKENDS repeat on Sunday jumps to Saturday`() {
        val sunday = time("2026-07-19 08:00") // Sunday
        val expectedSaturday = time("2026-07-25 08:00") // Next Saturday
        
        val actual = NextReminderCalculator.nextTriggerTime(sunday, RepeatType.WEEKENDS)
        
        assertEquals(format.format(expectedSaturday), format.format(actual))
    }

    @Test
    fun `SPECIFIC_DAYS repeat with Monday and Friday selected`() {
        val monday = time("2026-07-20 08:00")
        val selectedDays = listOf(Calendar.MONDAY, Calendar.FRIDAY)
        
        val expectedFriday = time("2026-07-24 08:00")
        val actual = NextReminderCalculator.nextTriggerTime(monday, RepeatType.SPECIFIC_DAYS, selectedDays)
        
        assertEquals(format.format(expectedFriday), format.format(actual))
    }

    private fun time(dateString: String): Long {
        return format.parse(dateString)?.time ?: 0L
    }
}
