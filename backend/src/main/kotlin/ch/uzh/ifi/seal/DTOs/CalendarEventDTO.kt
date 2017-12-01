package ch.uzh.ifi.seal.DTOs

import ch.uzh.ifi.seal.domain_classes.CalendarEvent
import java.time.Duration
import java.time.LocalTime

/**
 * Representation of the event to be transported to the client. Compared to the CalendarEvent has duration
 * field instead of end
 */
data class CalendarEventDTO(val id: String,
                            val startingTime: LocalTime,
                            val duration: Int,
                            val description: String)

fun CalendarEvent.toDTO(): CalendarEventDTO {
    val duration = Duration.between(this.start, this.end).toMinutes().toInt()
    return CalendarEventDTO(this.id, this.start, duration, this.description)
}