package ch.uzh.ifi.seal.DTOs

import ch.uzh.ifi.seal.business_logic.findOverlappingEvents
import ch.uzh.ifi.seal.domain_classes.AssignedTask
import ch.uzh.ifi.seal.domain_classes.CalendarEvent
import ch.uzh.ifi.seal.domain_classes.Schedule
import java.time.Duration
import java.time.LocalTime

/**
 * Class for transferring schedule data to the frontend
 * Score included to show the user how the options differ
 */
data class ScheduleDTO(
        val id: Int,
        val startOfDay: LocalTime,
        val endOfDay: LocalTime,
        val events: List<CalendarEventDTO>,
        val tasks: List<TaskDTO>,
        val hardScore: Int,
        val softScore: Int)

fun Schedule.toDTO(): ScheduleDTO {
    val events = calendarEvents.map { it.toDTO() }.sortedBy { it.startingTime }
    val tasks = dtoNotOverlappingWithEvents(assignedTasks, calendarEvents)
            .sortedByDescending { it.startingTime }
    return ScheduleDTO(id, startOfDay, endOfDay, events, tasks, score.hardScore, score.softScore)
}

fun dtoNotOverlappingWithEvents(assignedTasks: List<AssignedTask>, calendarEvents: List<CalendarEvent>): List<TaskDTO> {
    val dtoList = mutableListOf<TaskDTO>()
    assignedTasks.forEach { task ->
        val overlappingEvents = findOverlappingEvents(assignedTask = task, events = calendarEvents)
        val origTask = task.task
        var taskStart = task.startingTime
        var remainingDuration = task.task.duration
        overlappingEvents.forEach { event ->
            val duration = Duration.between(taskStart, event.start).toMinutes().toInt()
            remainingDuration -= duration

            dtoList.add(origTask.toDTO(duration, taskStart))
            taskStart = event.end
        }
        dtoList.add(origTask.toDTO(remainingDuration, taskStart))
    }
    return dtoList
}
