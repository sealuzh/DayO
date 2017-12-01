package ch.uzh.ifi.seal

import ch.uzh.ifi.seal.business_logic.generateFreeTimeSlots
import ch.uzh.ifi.seal.business_logic.rules
import ch.uzh.ifi.seal.business_logic.urgencyRule
import ch.uzh.ifi.seal.domain_classes.*
import ch.uzh.ifi.seal.domain_classes.MorningnessEveningnessType.*
import ch.uzh.ifi.seal.domain_classes.SleepQuality.BAD
import ch.uzh.ifi.seal.domain_classes.SleepQuality.NORMAL
import ch.uzh.ifi.seal.domain_classes.TaskDifficulty.*
import ch.uzh.ifi.seal.domain_classes.TaskImportance.*
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test
import org.optaplanner.core.api.solver.SolverFactory
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


/**
 * This class will be used to test, if it is possible to schedule the predefined set of tasks for the test user
 * with some planned events restricting the planning possibilities and the schedule having a start time and
 * end time as specified in the user settings.
 */

class ScheduleCreationTest {
    private val solverFactory: SolverFactory<Schedule> = SolverFactory.createFromXmlResource("solver-config.xml")
    private val solver = solverFactory.buildSolver()

    @Test
    fun `normal sleep quality and duration && MORNING && no due dates`() {

        // prepare
        val testSchedule = createSchedule(withDueDate = false, type = MORNING_TYPE, sleepDuration = 7.0, sleepQuality = NORMAL)
        testSchedule.timeSlots.add(TimeSlot(null, -1)) //adding the "unassigned" time slot
        initializeRules(testSchedule)

        // do
        val buffer = getBestScheduleOptions()
        val solution = solver.solve(testSchedule)
        solution.assignedTasks = getAssignedTasks(solution)

        // verify - solution valid
        assertNoOverlapWithEvents(solution)
        assertTasksDontOverlap(solution)
        // verify - solution optimal for given problem
        solution.assignedTasks.filter { it.task.difficulty == CHALLENGING }
                .forEach {
            Assert.assertTrue(it.toString(), it.startingTime <= LocalTime.NOON)
        }
        printSolutionInfo(buffer, solution)
    }


    @Test
    fun `normal sleep quality and duration && EVENING && no due dates`() {

        // prepare
        val testSchedule = createSchedule(withDueDate = false, type = EVENING_TYPE, sleepDuration = 8.0, sleepQuality = NORMAL)
        testSchedule.timeSlots.add(TimeSlot(LocalTime.MIN, -1)) //adding the "unassigned" time slot
        initializeRules(testSchedule)

        // do
        val buffer = getBestScheduleOptions()
        val solution = solver.solve(testSchedule)
        solution.assignedTasks = getAssignedTasks(solution)
        solution.assignedTasks.filter { it.task.difficulty == CHALLENGING }
                .forEach {
                    assert(it.startingTime >= LocalTime.NOON)
                }
        assertNoOverlapWithEvents(solution)
        assertTasksDontOverlap(solution)
        printSolutionInfo(buffer, solution)
    }


    @Test
    fun `normal sleep quality and duration && EVENING && WITH due dates - tasks with due dates are assigned`() {

        // prepare
        val testSchedule = createSchedule(withDueDate = true, type = EVENING_TYPE, sleepDuration = 7.5, sleepQuality = NORMAL)
        testSchedule.timeSlots.add(TimeSlot(LocalTime.MIN, -1)) //adding the "unassigned" time slot
        initializeRules(testSchedule)

        // do
        val buffer = getBestScheduleOptions()
        val solution = solver.solve(testSchedule)
        solution.assignedTasks = getAssignedTasks(solution)
        printSolutionInfo(buffer, solution)

        // verify
        // solution is valid
        assertNoOverlapWithEvents(solution)
        assertTasksDontOverlap(solution)
        // optimality
        solution.assignedTasks.filter { it.task.difficulty == CHALLENGING }
                .forEach {
                    assert(it.startingTime >= LocalTime.NOON)
                }

        assert(solution.assignedTasks.find { it.task.description == "water plants" } != null )
        assert(solution.assignedTasks.find { it.task.description == "laundry" } != null )
    }

    @Test
    fun `BAD sleep quality and duration && NEITHER type && WITH due dates - easier tasks are assigned`() {

        // prepare
        val testSchedule = createSchedule(withDueDate = true, type = NEITHER_TYPE, sleepQuality = BAD, sleepDuration = 4.0)
        testSchedule.timeSlots.add(TimeSlot(LocalTime.MIN, -1)) //adding the "unassigned" time slot
        initializeRules(testSchedule)

        // do
        val buffer = getBestScheduleOptions()
        val solution = solver.solve(testSchedule)
        solution.assignedTasks = getAssignedTasks(solution)
        printSolutionInfo(buffer, solution)

        // verify
        // solution is a valid solution
        assertNoOverlapWithEvents(solution)
        assertTasksDontOverlap(solution)
        // solutions is optimal
        assert(solution.assignedTasks.find { it.task.description == "water plants" } != null )
        assert(solution.assignedTasks.find { it.task.description == "laundry" } != null )
    }

    private fun getBestScheduleOptions(): MutableList<Schedule> {
        val buffer = mutableListOf<Schedule>()
        solver.addEventListener({
            if (buffer.size == 3) {
                buffer.sortByDescending { it.score }
                buffer.removeAt(2)
            }
            buffer.add((it.newBestSolution as Schedule))
        })
        return buffer
    }

    private fun printSolutionInfo(buffer: List<Schedule>, assignedSchedule: Schedule) {

        println("\n///////////////////////////////// solution info //////////////////////////////////////\n")

        println("--PROBLEM STATEMENT--")
        println("The time slots for the day:")
        assignedSchedule.timeSlots.forEach { println(it) }

        println("Generated events:")
        println(assignedSchedule.calendarEvents)

        println("--SOLUTION--")
        println("Three best solutions' scores are:")
        buffer.forEach { println(it.score) }
        println("\nThe score of the best solution is ${assignedSchedule.score}")

        val numOfFreeSlots = assignedSchedule.timeSlots.size - 1
        val numOfAssignedSlots = assignedSchedule.assignedTasks
                .map { it.task.duration }
                .sum() / ConfigurationConstants.TIME_SLOT_DURATION
        println("\nNumber of free slots in the day  = $numOfFreeSlots")
        println("Assigned slots count = $numOfAssignedSlots")

        println("\nAssigned tasks:")
        assignedSchedule.assignedTasks.forEach { println(it) }

        println("\nUnassigned tasks:")
        assignedSchedule.allTasks.filter { it.startingTimeSlot.id == -1 }.forEach { println(it) }
    }

    private fun assertTasksDontOverlap(solution: Schedule) {
        val sortedTasks = solution.assignedTasks.sortedBy { it.startingTime }
        for ((index, task) in sortedTasks.withIndex()) {
            if (index < sortedTasks.size - 1) {
                assertTrue(task.startingTime != sortedTasks[index + 1].startingTime)
            }
        }
    }

    private fun assertNoOverlapWithEvents(solution: Schedule) {
        val assignedTasks = solution.assignedTasks
        val events = solution.calendarEvents

        events.forEach { e ->
            assert(assignedTasks.find { it.startingTime == e.start } == null)
        }
    }

    private fun getAssignedTasks(solution: Schedule): List<AssignedTask> {
        val list = solution.allTasks
                .filter { it.startingTimeSlot != null && it.startingTimeSlot.id != -1 }
                .map { task -> AssignedTask(task.startingTimeSlot.startTime, task) }
        for (t in list) {
            println(t)
        }
        return list

    }

    private fun initializeRules(schedule: Schedule) {
        val today = LocalDate.now()
        rules.forEach { it.init(schedule.owner, today) }
        urgencyRule.recalculateWorkingDays(today)
    }

    private fun createSchedule(withDueDate: Boolean, type: MorningnessEveningnessType, sleepQuality: SleepQuality, sleepDuration: Double): Schedule {

        val user = User("Test User", "email@some.domain")
        val startDay = LocalTime.parse("08:00")
        val endDay = LocalTime.parse("18:00")
        user.settings = Settings(startDay, endDay, type)
        user.dailyForms.add(DailyFormInfo(LocalDate.now(), sleepDuration, sleepQuality, StressLevel.INSIGNIFICANT, user))
        val events = createEvents(startDay)
        val settings = Settings(startDay, endDay)

        val tasks = generateTasks(user, withDueDate)


        val schedule = Schedule(startDay, endDay, events, generateFreeTimeSlots(settings, events), tasks)
        schedule.owner = user

        return schedule
    }

    private fun createEvents(dayStart: LocalTime): List<CalendarEvent> {
        val events = ArrayList<CalendarEvent>()
        var startingTime = dayStart.plusHours(1) //first event will be scheduled one hour after day start
        val user = User("testUser")
        user.email = user.name + "@someMail.com"
        //adding 30 minutes long event each two hours
        for (i in 1..4) {
            events.add(CalendarEvent("id$i", LocalDate.now(), startingTime, "Fixed appointment #$i", startingTime.plusMinutes(30), user))
            startingTime = startingTime.plusHours(2)
        }
        return events
    }

    private fun generateTasks(user: User, withDueDate: Boolean): MutableList<Task> {
        val tasks = ArrayList<Task>()
        //  low priority tasks (priority=LOW)
        tasks.add(Task("water plants", 60, EASY, LOW, user))
        tasks.add(Task("check emails", 60, EASY, LOW, user))
        tasks.add(Task("do something easy but important", 60, EASY, HIGH, user))
        tasks.add(Task("walk 5000 steps", 60, REGULAR, LOW, user))
        tasks.add(Task("read book", 60, CHALLENGING, LOW, user))

        //middle priority tasks (priority=MEDIUM)
        tasks.add(Task("laundry", 60, EASY, MEDIUM, user))
        tasks.add(Task("write introduction section", 60, REGULAR, MEDIUM, user))
        tasks.add(Task("plan preparation to the interview", 60, CHALLENGING, MEDIUM, user))

        //high priority tasks (priority=HIGHER)
        tasks.add(Task("cook dinner", 60, EASY, HIGH, user))
        tasks.add(Task("Angular 2 tutorial", 60, REGULAR, HIGH, user))
        tasks.add(Task("backend architecture", 60, CHALLENGING, HIGH, user))

        if (withDueDate) {
            tasks.find { it.description == "water plants" }!!.apply { dueDate = LocalDate.now() }
            tasks.find { it.description == "laundry" }!!.apply { dueDate = LocalDate.now().plusDays(1) }
        }

        return tasks
    }

}
