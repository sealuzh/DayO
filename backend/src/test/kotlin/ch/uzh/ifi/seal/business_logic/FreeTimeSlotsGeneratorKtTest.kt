package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.CalendarEvent
import ch.uzh.ifi.seal.domain_classes.TimeSlot
import ch.uzh.ifi.seal.entities_tests.TestUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.time.LocalTime

@RunWith(SpringRunner::class)
@SpringBootTest
class FreeTimeSlotsGeneratorKtTest {

    @Autowired
    private lateinit var testUtil: TestUtil

    @Test
    fun `Generate free time slots for 2 hours without events`() {
        //prepare
        val settings = testUtil.generateSettings("08:00", "10:00")
        val lastStartTime = LocalTime.parse("09:45")

        //do
        val freeTimeSlots = generateFreeTimeSlots(settings, listOf<CalendarEvent>())

        //assert
        assert(freeTimeSlots.size == 8)
        assertEquals(freeTimeSlots.lastOrNull(), TimeSlot(lastStartTime, 7))
    }

    @Test
    fun `TimeSlot generation with events`() {
        //prepare
        val settings = testUtil.generateSettings("12:00", "17:00")
        val events = mutableListOf<CalendarEvent>()
        events.add(CalendarEvent("1", LocalDate.now(), LocalTime.parse("17:00"), "lunch", LocalTime.parse("18:00"), null))
        events.add(CalendarEvent("2", LocalDate.now(), LocalTime.parse("12:00"), "lunch", LocalTime.parse("14:00"), null))
        events.add(CalendarEvent("3", LocalDate.now(), LocalTime.parse("14:30"), "meeting", LocalTime.parse("15:00"), null))

        //do
        val freeTimeSlots = generateFreeTimeSlots(settings, events)

        println("Generated free time slots: \n")
        freeTimeSlots.forEach { println(it) }

        //check
        assert(freeTimeSlots[0] != TimeSlot(LocalTime.parse("12:00"), 0))
    }

}