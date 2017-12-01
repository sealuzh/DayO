package ch.uzh.ifi.seal.DTOs

import ch.uzh.ifi.seal.business_logic.findOverlappingEvents
import ch.uzh.ifi.seal.domain_classes.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import java.time.LocalDate
import java.time.LocalTime

/**
 * Testing the creation of the DTO for the schedule in a proper way
 */
class ScheduleDTOTest {

    @Test
    fun ScheduleToDTO() {

        // prepare
        val today = LocalDate.now()
        val schedule = Schedule(today, LocalTime.of(9, 0), LocalTime.of(19, 0))
        schedule.calendarEvents = mutableListOf(newEvent(today, "10:00", "11:00"), newEvent(today, "12:40", "13:00"), newEvent(today, "15:00", "18:00"))
        val task = Task("something", 30, TaskDifficulty.REGULAR, TaskImportance.MEDIUM, User())
                .apply { id = 1 }

        val assignedTask = AssignedTask(LocalTime.NOON, task)
        schedule.assignedTasks = mutableListOf(assignedTask)
        schedule.score = HardSoftScore.valueOf(0, 100)

        // do
        val dto = schedule.toDTO()
        val taskDto = assignedTask.toDTO()

        // verify
        assertEquals(LocalTime.parse("09:00"), dto.startOfDay)
        assertEquals(LocalTime.parse("19:00"), dto.endOfDay)
        assertEquals(0, dto.hardScore)
        assertEquals(100, dto.softScore)
        assertEquals(assignedTask, schedule.assignedTasks.first())
        assertEquals(taskDto.id, dto.tasks.first().id)
    }

    private fun newEvent(today: LocalDate, from: String, to: String) = CalendarEvent("id", today, LocalTime.parse(from), "", LocalTime.parse(to), User())

    @Test
    fun `transform assigned tasks overlapping with events to TaskDTO`() {

        // prepare
        val events = listOf(event(9, 15, 10, 0), event(10, 30, 11, 0), event(15, 0, 16, 15))
        val shortTask = task(15)
        val mediumTask = task(90)
        val longTask = task(120)
        val assignedTasks = listOf(AssignedTask(LocalTime.parse("09:00"), shortTask),
                AssignedTask(LocalTime.parse("10:00"), mediumTask), AssignedTask(LocalTime.parse("14:15"), longTask))

        // do
        val dtoList = dtoNotOverlappingWithEvents(assignedTasks, events)
        println(findOverlappingEvents(assignedTask = AssignedTask(LocalTime.parse("09:00"), shortTask), events = events))
        dtoList.forEach { println(it) }

        // verify
        assert(dtoList.size == 5)
        assert(dtoList.find { it.id == 90 && it.startingTime.toString() == "10:00"} != null)
        assert(dtoList.find { it.id == 120 && it.startingTime.toString() == "16:15"} != null)
        // no tasks scheduled when event starts
        assert(dtoList.find { it.startingTime.toString() == "09:15"} == null)
        assert(dtoList.find { it.startingTime.toString() == "10:30"} == null)
        assert(dtoList.find { it.startingTime.toString() == "15:00"} == null)
    }

    private fun task(duration: Int) = Task("", duration, TaskDifficulty.REGULAR, TaskImportance.LOW, User()).apply { id = duration }

    private fun event(startHour: Int, startMin: Int, endHour: Int, endMin: Int) = CalendarEvent(null, LocalTime.of(startHour, startMin), LocalTime.of(endHour, endMin), "")
}