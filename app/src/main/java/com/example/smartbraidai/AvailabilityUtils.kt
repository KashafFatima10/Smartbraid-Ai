package com.example.smartbraidai

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class BookingDayOption(
    val dayKey: String,
    val dayLabel: String,
    val dateLabel: String,
    val date: LocalDate
)

private val timeInputFormats = listOf(
    DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH),
    DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH),
    DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH),
    DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
)

private val displayTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
private val bookingDateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

fun buildBookingDayOptions(anchorDate: LocalDate = LocalDate.now()): List<BookingDayOption> {
    return (0..6).map { offset ->
        val date = anchorDate.plusDays(offset.toLong())
        val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        BookingDayOption(
            dayKey = normalizeDayKey(dayLabel),
            dayLabel = dayLabel.uppercase(Locale.ENGLISH),
            dateLabel = date.format(bookingDateFormatter),
            date = date
        )
    }
}

fun normalizeDayKey(value: String): String {
    return when (value.trim().lowercase(Locale.ENGLISH)) {
        "mon", "monday" -> "Mon"
        "tue", "tues", "tuesday" -> "Tue"
        "wed", "wednesday" -> "Wed"
        "thu", "thur", "thurs", "thursday" -> "Thu"
        "fri", "friday" -> "Fri"
        "sat", "saturday" -> "Sat"
        "sun", "sunday" -> "Sun"
        else -> value.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
    }
}

fun buildAvailableSlots(
    availability: Map<String, Map<String, String>>,
    dayKey: String,
    intervalMinutes: Long = 30
): List<String> {
    val dayAvailability = availability.entries.firstOrNull { normalizeDayKey(it.key) == normalizeDayKey(dayKey) }?.value
        ?: return emptyList()

    val enabled = dayAvailability["enabled"]?.toBooleanStrictOrNull() ?: false
    if (!enabled || intervalMinutes <= 0) return emptyList()

    val startTime = parseTime(dayAvailability["start"]) ?: LocalTime.of(9, 0)
    val endTime = parseTime(dayAvailability["end"]) ?: LocalTime.of(17, 0)
    if (!startTime.isBefore(endTime)) return emptyList()

    val slots = mutableListOf<String>()
    var cursor = startTime
    while (cursor.isBefore(endTime)) {
        slots.add(cursor.format(displayTimeFormatter))
        cursor = cursor.plusMinutes(intervalMinutes)
    }
    return slots
}

fun groupSlotsByPeriod(slots: List<String>): Map<String, List<String>> {
    val grouped = linkedMapOf(
        "MORNING" to mutableListOf<String>(),
        "AFTERNOON" to mutableListOf<String>(),
        "EVENING" to mutableListOf<String>()
    )

    slots.forEach { slot ->
        val period = when (parseTime(slot)?.hour ?: 0) {
            in 0..11 -> "MORNING"
            in 12..16 -> "AFTERNOON"
            else -> "EVENING"
        }
        grouped.getValue(period).add(slot)
    }

    return grouped.filterValues { it.isNotEmpty() }
}

fun formatAvailabilityWindow(dayAvailability: Map<String, String>?): String {
    if (dayAvailability == null) return "Not set"
    val enabled = dayAvailability["enabled"]?.toBooleanStrictOrNull() ?: false
    if (!enabled) return "Unavailable"

    val start = parseTime(dayAvailability["start"])
    val end = parseTime(dayAvailability["end"])
    return if (start != null && end != null) {
        "${start.format(displayTimeFormatter)} - ${end.format(displayTimeFormatter)}"
    } else {
        "Available"
    }
}

fun formatAvailabilitySummary(availability: Map<String, Map<String, String>>): String {
    val orderedDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val activeDays = orderedDays.mapNotNull { day ->
        val dayAvailability = availability.entries.firstOrNull { normalizeDayKey(it.key) == day }?.value
        val enabled = dayAvailability?.get("enabled")?.toBooleanStrictOrNull() ?: false
        if (!enabled) null else "$day ${formatAvailabilityWindow(dayAvailability)}"
    }

    return if (activeDays.isEmpty()) {
        "No availability set"
    } else {
        activeDays.joinToString(" • ")
    }
}

private fun parseTime(value: String?): LocalTime? {
    if (value.isNullOrBlank()) return null
    timeInputFormats.forEach { formatter ->
        runCatching { LocalTime.parse(value.trim(), formatter) }.getOrNull()?.let { return it }
    }
    return null
}