package ch.uzh.ifi.seal.controllers

import ch.uzh.ifi.seal.LoggedInUserInfo
import ch.uzh.ifi.seal.data_access.TaskRepository
import ch.uzh.ifi.seal.domain_classes.*
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Quick way to add some data for testing purposes without having to input it manually
 */
@RequestMapping("sampleData")
@RestController
@Transactional
class SampleDataController(private val loggedInUserInfo: LoggedInUserInfo,
                           private val taskRepo: TaskRepository) {
    @PersistenceContext
    private lateinit var em: EntityManager

    @GetMapping("createSchedules/{count}")
    fun createSchedules(@PathVariable count: Int): List<Schedule> {
        require(count in 1..3)
        var schedules = mutableListOf<Schedule>()
        (1..count).forEach {
            val taskName = when (it) {
                1 -> "Bring out the garbage"
                2 -> "Eat a candy"
                else -> "Walk a bit"
            }
            val user = loggedInUserInfo.getUser()
            val events = listOf(
                    CalendarEvent(uuid(), LocalDate.now(), time(9, 30), "Meeting", time(10, 0), user),
                    CalendarEvent(uuid(), LocalDate.now(), time(13, 0), "Meeting", time(13, 20), user),
                    CalendarEvent(uuid(), LocalDate.now(), time(15, 0), "Meeting", time(16, 0), user)
            )
            events.forEach(em::persist)
            val schedule = Schedule(time(7, 0), time(16, 0), events, listOf(), listOf())
            val task = Task(taskName, 60, TaskDifficulty.REGULAR, TaskImportance.LOW, user)
            em.persist(task)
            val startingTime = LocalTime.of(14, 0)
            val assignedTask = AssignedTask(startingTime, task)

            task.assignments.add(assignedTask)
            schedule.assignedTasks.add(assignedTask)
            schedule.score = HardSoftScore.valueOf(50, -100)
            schedule.assignedTasks.forEach(em::persist)

            schedule.forDate = LocalDate.now()
            schedule.owner = user
            user.schedules.add(schedule)
            em.persist(schedule)
            schedules.add(schedule)
        }
        return schedules
    }

    /* Adding enough tasks to be able to produce complete schedule and to see the task list complete
     * 9 tasks from here and 1 could be added through the app */
    @GetMapping("/saveSomeTasks")
    fun saveSomeTasks(): List<Task> {
        val user = loggedInUserInfo.getUser()
        val testTasks = listOf(
                Task("Easy unimportant task for 1,5 hours", 90, TaskDifficulty.EASY, TaskImportance.LOW, user).apply { dueDate = LocalDate.now().plusDays(7) },
                Task("Short difficult task for 1 hour, very important", 60, TaskDifficulty.CHALLENGING, TaskImportance.HIGH, user),
                Task("Very short task due very soon", 15, TaskDifficulty.REGULAR, TaskImportance.MEDIUM, user).apply { dueDate = LocalDate.now().plusDays(1) },
                Task("Do something important for 2h", 120, TaskDifficulty.REGULAR, TaskImportance.HIGH, user),
                Task("Important and urgent task", 105, TaskDifficulty.EASY, TaskImportance.HIGH, user).apply { dueDate = LocalDate.now().plusDays(2) },
                Task("Some trivial task for 30 minutes", 30, TaskDifficulty.EASY, TaskImportance.LOW, user),
                Task("Research something", 45, TaskDifficulty.REGULAR, TaskImportance.MEDIUM, user).apply { dueDate = LocalDate.now().plusDays(5) },
                Task("Do things", 180, TaskDifficulty.EASY, TaskImportance.MEDIUM, user).apply { dueDate = LocalDate.now().plusDays(14) },
                Task("Implement XYZ", 180, TaskDifficulty.CHALLENGING, TaskImportance.MEDIUM, user)
        )
        user.tasks.addAll(testTasks)
        testTasks.forEach { taskRepo.save(it) }
        return testTasks
    }

    private fun uuid() = UUID.randomUUID().toString()

    @GetMapping("deleteSchedules")
    fun deleteSchedules() {
        val today = LocalDate.now()
        val user = loggedInUserInfo.getUser()
        val todaysSchedules = user.schedules.filter { it.forDate == today }
        user.schedules.removeAll(todaysSchedules)
        todaysSchedules.forEach(em::remove)
    }


    private fun time(h: Int, m: Int) = LocalTime.of(h, m)
}