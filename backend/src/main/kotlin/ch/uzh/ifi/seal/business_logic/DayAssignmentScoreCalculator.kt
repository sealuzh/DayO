package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.ConfigurationConstants
import ch.uzh.ifi.seal.MIN_TASK_PARTITION_DURATION
import ch.uzh.ifi.seal.domain_classes.AssignedTask
import ch.uzh.ifi.seal.domain_classes.CalendarEvent
import ch.uzh.ifi.seal.domain_classes.Schedule
import ch.uzh.ifi.seal.domain_classes.Task
import mu.KLogging
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalTime

/**
 * The score calculator provides the way to calculate, if the hard constraints are fulfilled
 * and assigns the softScore to find the schedule which is more optimal
 * For now the optimal schedule is the one which has the tasks with the highest cumulative priority
 */
@Component
class DayAssignmentScoreCalculator : EasyScoreCalculator<Schedule> {

    private val softScoreCalculator = SoftScoreCalculator()

    companion object : KLogging()

    override fun calculateScore(schedule: Schedule): HardSoftScore {
        var hardScore = 0
        var softScore = 0

        //time slot with the id of -1 is considered unassigned
        val assignedTasks = schedule.allTasks
                .filter({ t -> t.startingTimeSlot != null && t.startingTimeSlot.id != -1 })
                .sortedBy { it.startingTimeSlot.id }

        for ((i, task) in assignedTasks.withIndex()) {
            if (!fitsIntoDaySchedule(task, schedule)) {
                hardScore -= 1000
            } else if (overlapsWithEvent(task, schedule) && !canTaskBePartitioned(task, schedule.calendarEvents)) {
                hardScore -= 1000
            } else if (i != assignedTasks.size - 1 && overlap(task, assignedTasks[i + 1])) {
                hardScore -= 1000
            } else {
                hardScore += 100 // for each assigned task
                softScore += softScoreCalculator.calculateScore(task)
            }
        }
        return HardSoftScore.valueOf(hardScore, softScore)
    }

    private fun overlap(curTask: Task, nextTask: Task): Boolean {
        val nextTimeSlotId = getNextTimeSlotId(curTask)
        return nextTimeSlotId > nextTask.startingTimeSlot.id
    }

    private fun fitsIntoDaySchedule(task: Task, schedule: Schedule): Boolean {
        val nextTimeSlotId = getNextTimeSlotId(task)
        val taskFinishedTime = task.startingTimeSlot.startTime.plusMinutes(task.duration.toLong())
        return nextTimeSlotId < schedule.timeSlots.size && !taskFinishedTime.isAfter(schedule.endOfDay)
    }

    private fun getNextTimeSlotId(curTask: Task): Int {
        val timeSlotId = curTask.startingTimeSlot.id
        val taskDurationInNumberOfSlots = curTask.duration / ConfigurationConstants.TIME_SLOT_DURATION
        return timeSlotId + taskDurationInNumberOfSlots
    }

    /**
     * Check, if the task assignment is overlapping with the event scheduled in the day
     * @return false, if there is no overlap
     */
    fun overlapsWithEvent(task: Task, schedule: Schedule): Boolean {
        if (schedule.calendarEvents.isEmpty()) return false

        val sortedEvents = schedule.calendarEvents
        val taskStarts = task.startingTimeSlot.startTime
        val taskEnds = task.startingTimeSlot.startTime.plusMinutes(task.duration.toLong())

        if (taskEnds.isBefore(sortedEvents.first().start) || taskStarts.isAfter(sortedEvents.last().end))
            return false

        sortedEvents.forEach { event ->
            if (taskStarts.isBefore(event.start) && !(taskEnds.isBefore(event.start) || taskEnds == event.start))
                return true
        }
        return false
    }

    /**
     * Checks, if the task can be split into the subtasks. Task can be split, if
     *      it is not to short: duration >= 60 minutes
     *      if the partition is large enough (greater than MIN_TASK_PARTITION_DURATION)
     *      if the breaking event is not too long (partition should be at most 2 times smaller)
     * @return false, if the partitioning of the given task does not satisfy the conditions above
     */
    fun canTaskBePartitioned(task: Task, events: List<CalendarEvent>): Boolean {
        if (task.duration < 60) return false
        else {
            val overlappingEvents = findOverlappingEvents(task = task, events = events)
            var remainingTaskDuration = task.duration
            var taskStart = task.startingTimeSlot.startTime

            for (event in overlappingEvents) {
                val eventDuration = Duration.between(event.start, event.end).toMinutes()
                val partition = Duration.between(taskStart, event.start).toMinutes()
                remainingTaskDuration -= partition.toInt()

                logger.debug{ "event $event partition: $partition remaining task duration: $remainingTaskDuration" }
                if (partition < MIN_TASK_PARTITION_DURATION || eventDuration > 2 * partition)
                    return false
                else
                    taskStart = event.end
            }
            return remainingTaskDuration >= MIN_TASK_PARTITION_DURATION
        }
    }
}
fun findOverlappingEvents(task: Task? = null, assignedTask: AssignedTask? = null, events: List<CalendarEvent>): List<CalendarEvent> {

    require(task != null || assignedTask != null)
    val taskStarts: LocalTime
    var taskEnds: LocalTime
    val overlaps = mutableListOf<CalendarEvent>()
    if (task != null) {
        taskStarts = task.startingTimeSlot.startTime
        taskEnds = task.startingTimeSlot.startTime.plusMinutes(task.duration.toLong())
    } else {
        taskStarts = assignedTask!!.startingTime
        taskEnds = assignedTask.startingTime.plusMinutes(assignedTask.task.duration.toLong())
    }
    val possiblyOverlappingEvents = events.filter { it.start > taskStarts }
    possiblyOverlappingEvents.forEach {event ->
        if (event.start in taskStarts..taskEnds && taskEnds != event.start) {
            overlaps.add(event)
            taskEnds = taskEnds.plusMinutes(Duration.between(event.start, event.end).toMinutes())
        } else {
            return overlaps
        }
    }
    return overlaps
}