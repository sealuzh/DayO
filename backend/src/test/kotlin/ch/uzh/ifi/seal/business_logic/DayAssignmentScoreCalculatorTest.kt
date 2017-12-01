package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class DayAssignmentScoreCalculatorTest {

    val scoreCalc = DayAssignmentScoreCalculator()

    ///////////// Tests for overlapsWithEvent() /////////////
    @Test
    fun `event starts at task end - no overlap`() {
        val task = task(duration = 45, startHour = 8, startMinute = 15)
        val schedule = schedule(event(startHour = 8, endHour = 9))

        assertFalse(scoreCalc.overlapsWithEvent(task, schedule))
    }

    @Test
    fun `event ends at task start - no overlap`() {
        val task = task(duration = 45, startHour = 9, startMinute = 0)
        val schedule = schedule(event(startHour = 8, endHour = 9))

        assertFalse(scoreCalc.overlapsWithEvent(task, schedule))
    }

    @Test
    fun `event before task and not adjacent to task - no overlap`() {
        val task = task(duration = 45, startHour = 9, startMinute = 0)
        val schedule = schedule(event(startHour = 7, endHour = 8))

        assertFalse(scoreCalc.overlapsWithEvent(task, schedule))
    }

    @Test
    fun `event after task and not adjacent to task - no overlap`() {
        val task = task(duration = 45, startHour = 9, startMinute = 0)
        val schedule = schedule(event(startHour = 10, endHour = 11))

        assertFalse(scoreCalc.overlapsWithEvent(task, schedule))
    }

    @Test
    fun `event contained in task - overlap`() {
        val task = task(duration = 120, startHour = 9, startMinute = 0)
        val schedule = schedule(event(startHour = 10, endHour = 11))

        assertTrue(scoreCalc.overlapsWithEvent(task, schedule))
    }

    private fun schedule(calendarEvent: CalendarEvent) = Schedule(null, null, listOf(calendarEvent), listOf(), listOf())

    private fun event(startHour: Int, endHour: Int) = CalendarEvent("id1", LocalDate.now(),
            LocalTime.of(startHour, 0), "descr", LocalTime.of(endHour, 0), User())

    private fun task(duration: Int, startHour: Int, startMinute: Int) = Task("descr", duration, null, null, null).apply {
        startingTimeSlot = TimeSlot(LocalTime.of(startHour, startMinute), 1)
    }

    /////////////////////////// Tests for findOverlappingEvents() /////////////////////
    @Test
    fun `no overlapping events - empty result`() {

        // prepare
        val task = task(duration = 120, startHour = 8, startMinute = 0)
        val events = listOf(event(startHour = 7, endHour = 8), event(startHour = 10, endHour = 12))

        // do
        val overlappingEvents = findOverlappingEvents(task, null, events)
        println(overlappingEvents)

        // verify
        assert(overlappingEvents.isEmpty())
    }

    @Test
    fun `event starts during task - non-empty result`() {
        // prepare
        val task = task(duration = 120, startHour = 8, startMinute = 0)
        val events = listOf(event(startHour = 7, endHour = 8), event(startHour = 9, endHour = 12))

        // do
        val overlappingEvents = findOverlappingEvents(task, null, events)
        println(overlappingEvents)

        // verify
        assert(overlappingEvents.size == 1)
        assert("09:00" == overlappingEvents.first().start.toString())
    }

    /////////////////////////// canTaskBePartitioned() ////////////////////////

    @Test
    fun `task 120 minutes, event is longer - false`() {
        // prepare
        val task = task(duration = 120, startHour = 8, startMinute = 0)
        val events = listOf(event(startHour = 7, endHour = 8), event(startHour = 9, endHour = 12))

        // do
        val result = scoreCalc.canTaskBePartitioned(task, events)

        // verify
        assertFalse(result)
    }

    @Test
    fun `short tasks - false`() {
        // prepare
        val tasks = listOf(task(duration = 15, startHour = 8, startMinute = 0),
                task(duration = 45, startHour = 9, startMinute = 0),
                task(duration = 30, startHour = 10, startMinute = 0))

        val events = listOf(event(startHour = 8, endHour = 9), event(startHour = 9, endHour = 12))

        // do + verify
        tasks.forEach { t: Task ->
            assertFalse(scoreCalc.canTaskBePartitioned(t, events))
        }
    }

    @Test
    fun `large enough task overlapping with two events - true`() {
        // prepare
        val task = task(duration = 160, startHour = 8, startMinute = 0)
        val events = listOf(event(startHour = 9, endHour = 10), event(startHour = 11, endHour = 12))

        // do + verify
        assertTrue(scoreCalc.canTaskBePartitioned(task, events))
    }

    @Test
    fun `large enough task overlapping with too many events - false`() {
        // prepare
        val task = task(duration = 160, startHour = 8, startMinute = 0)
        val events = listOf(event(startHour = 9, endHour = 10), event(startHour = 11, endHour = 12),
                CalendarEvent(LocalDate.now(), LocalTime.of(12, 30), LocalTime.of(13, 0), ""))

        // do + verify
        val overlappingEvents = findOverlappingEvents(task, null, events)
        println(overlappingEvents)
        assertFalse(scoreCalc.canTaskBePartitioned(task, events))
    }

    @Test
    fun `very large event overlapping with the task of 2 hours - false`() {
        // prepare
        val task = task(duration = 120, startHour = 8, startMinute = 0)
        val events = listOf(event(startHour = 9, endHour = 12))

        // do + verify
        val overlappingEvents = findOverlappingEvents(task, null, events)
        println(overlappingEvents)
        assertFalse(scoreCalc.canTaskBePartitioned(task, events))
    }
}