package ch.uzh.ifi.seal.domain_classes

import ch.uzh.ifi.seal.DTOs.CalendarEventDTO
import ch.uzh.ifi.seal.DTOs.toDTO
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class CalendarEventTest {

    @Test
    fun `test DTO creation`() {

        // prepare
        val event = CalendarEvent("id", LocalDate.now(), LocalTime.parse("10:00"), "event's description", LocalTime.parse("12:00"), null)
        val expectedDTO = CalendarEventDTO("id", LocalTime.parse("10:00"), 120, "event's description")

        // do
        val actualDTO = event.toDTO()

        // verify
        assertEquals(expectedDTO, actualDTO)
    }

}
