package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.CalendarEvent
import ch.uzh.ifi.seal.domain_classes.Settings
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Testing the filtering and truncation of the events when preparing for schedule generation
 */
class ScheduleGeneratorTest {

    val today = LocalDate.now()!!
    val settings = Settings(LocalTime.of(8,0), LocalTime.of(16,0))

    @Test
    fun `event outside of the day - event not added to the list`() {

        // prepare
        val first = CalendarEvent(today, LocalTime.of(7, 0), LocalTime.of(7, 30), "first")
        val justBefore = CalendarEvent(today, LocalTime.of(7, 0), LocalTime.of(8, 0), "just before start")
        val second = CalendarEvent(today, LocalTime.of(8, 0), LocalTime.of(8, 30), "second")
        val third = CalendarEvent(today, LocalTime.of(12, 0), LocalTime.of(13, 30), "third")
        val justAfter = CalendarEvent(today, LocalTime.of(16, 0), LocalTime.of(18, 30), "just after end")
        val events = listOf(first, justBefore, third, second, justAfter)

        // do
        val eventsForToday = findEventsForToday(events, today, settings)
        println("All: $events")
        println("In: $eventsForToday")

        // verify
        assert(first !in eventsForToday)
        assert(justAfter !in eventsForToday)
        assert(justBefore !in eventsForToday)
    }

    @Test
    fun `event's start or end outside of the day - event truncated`() {

        // prepare
        val first = CalendarEvent(today, LocalTime.of(7, 0), LocalTime.of(8, 30), "first")
        val second = CalendarEvent(today, LocalTime.of(9, 0), LocalTime.of(9, 30), "second")
        val third = CalendarEvent(today, LocalTime.of(15, 0), LocalTime.of(16, 30), "third")
        val events = listOf(first, second, third)

        // do
        val eventsForToday = findEventsForToday(events, today, settings)
        println(eventsForToday)

        // verify
        assert(first in eventsForToday)
        assert(second in eventsForToday)
        assert(third in eventsForToday)
        assertEquals("08:00", eventsForToday[0].start.toString())
        assertEquals("16:00", eventsForToday[2].end.toString())
    }


    @Test
    fun `event larger than the day or of same length - event not in list`() {

        // prepare
//        val weights = listOf(1.0, 2.0, 3.0, 4.0)
//        rules.zip(weights, { rule, weight -> rule.weight = weight })

        val first = CalendarEvent(today, LocalTime.of(7, 0), LocalTime.of(16, 30), "first")
        val second = CalendarEvent(today, LocalTime.of(8, 0), LocalTime.of(16, 0), "second")
        val third = CalendarEvent(today, LocalTime.of(15, 0), LocalTime.of(16, 30), "third")
        val events = listOf(first, second, third)


        // do
        val eventsForToday = findEventsForToday(events, today, settings)
        println(eventsForToday)

        // verify
        assert(first !in eventsForToday)
        assert(second !in eventsForToday)
        assert(third in eventsForToday)
    }

    @Test
    fun `event not for today - event not in list`() {

        // prepare
        val first = CalendarEvent(today, LocalTime.of(7, 0), LocalTime.of(12, 30), "first")
        val second = CalendarEvent(today.plusDays(2), LocalTime.of(8, 0), LocalTime.of(16, 0), "second")
        val third = CalendarEvent(today.plusDays(1), LocalTime.of(15, 0), LocalTime.of(16, 30), "third")
        val events = listOf(first, second, third)
        val settings = Settings(LocalTime.of(8,0), LocalTime.of(16,0))

        // do
        val eventsForToday = findEventsForToday(events, today, settings)
        println("All: $events")
        println(eventsForToday)

        // verify
        assert(first in eventsForToday)
        assert(second !in eventsForToday)
        assert(third !in eventsForToday)
    }
}