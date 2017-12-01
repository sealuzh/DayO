package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.data_access.TaskRepository
import ch.uzh.ifi.seal.domain_classes.*
import com.google.common.annotations.VisibleForTesting
import mu.KLogging
import org.optaplanner.core.api.solver.SolverFactory
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Class responsible for generating of the possible schedules for the day
 * User gives access to all information relevant for planning:
 *      daily form for the information about the current day
 *      settings (start and end of the working day)
 *      tasks which user has
 *
 * Three best options of the solution are returned.
 */
@Component
class ScheduleGenerator(private val taskRepository: TaskRepository) {
    companion object : KLogging()

    val solverFactory: SolverFactory<Schedule> = SolverFactory.createFromXmlResource("solver-config.xml")

    fun generateScheduleOptions(user: User, today: LocalDate, events: List<CalendarEvent>): List<Schedule> {
        // prepare the schedule
        val settings = user.settings
        val todayEvents = findEventsForToday(events, today, settings)
        val activeTasks = findActiveTasks(user)

        val freeTimeSlots = generateFreeTimeSlots(settings, todayEvents)
        freeTimeSlots.add(TimeSlot(null, -1)) // adding the special unassigned slot

        val problem = Schedule(today, settings.startOfDay, settings.endOfDay, todayEvents, freeTimeSlots, activeTasks, user)

        // solve the optimization problem
        val possibleSchedules = solveScheduleOptimization(problem)

        possibleSchedules.forEach { schedule ->
            schedule.assignedTasks = schedule.allTasks
                    .filter { it.startingTimeSlot != null && it.startingTimeSlot.id != -1 }
                    .map { task ->
                        val managedTask = taskRepository.save(task)
                        AssignedTask(task.startingTimeSlot.startTime, managedTask)
                    }
        }
        return possibleSchedules
    }

    /** Solving the problem of allocating tasks to the given schedule with constraints
     *  from DailyFormInfo and user Settings
     *  @param problem to solve = schedule with initial pool of all active tasks and free time slots
     *  @return 3 solutions with the highest score */
    private fun solveScheduleOptimization(problem: Schedule): List<Schedule> {

        // prepare the solver
        val solver = solverFactory.buildSolver()
        // listen to the event of best solution changed
        val bestOptions = mutableListOf<Schedule>()
        solver.addEventListener({
            if (bestOptions.size == 3) {
                bestOptions.sortByDescending { it.score }
                bestOptions.removeAt(2)
            }
            bestOptions.add((it.newBestSolution as Schedule))
        })

        // the best solution
        val bestSolution = solver.solve(problem)

        logger.info("Found {} solutions: {}. Best solution: {}", bestOptions.size, bestOptions, bestSolution)
        return bestOptions
    }

    private fun findActiveTasks(user: User): List<Task> {
        return user.tasks.filter { it.completed == null && it.deleted == null }
    }
}

@VisibleForTesting
internal fun findEventsForToday(events: List<CalendarEvent>, today: LocalDate, settings: Settings): List<CalendarEvent> {
    val todayEvents = events.filter { it.date == today }
    truncateOverflowingEvents(todayEvents, settings)

    val filteredEvents = todayEvents.filter {
        val startsWithinDay = it.start >= settings.startOfDay && it.start < settings.endOfDay
        val equalToDay = it.start == settings.startOfDay && it.end == settings.endOfDay
        val endsWithinDay = it.end <= settings.endOfDay
        startsWithinDay && endsWithinDay && !equalToDay
    }
    return filteredEvents.sorted() // return the events sorted by the start time (ascending)
}

private fun truncateOverflowingEvents(events: List<CalendarEvent>, settings: Settings) {
    events.forEach {
        if (it.start < settings.startOfDay && it.end > settings.startOfDay && it.end < settings.endOfDay) {
            it.start = settings.startOfDay
        } else if (it.start < settings.endOfDay && it.end > settings.endOfDay) {
            it.end = settings.endOfDay
        }
    }
}