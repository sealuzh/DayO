package ch.uzh.ifi.seal.entities_tests

import ch.uzh.ifi.seal.domain_classes.*
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime

@Component
class TestUtil {

    fun createUser(name: String) = User(name).apply {
        email = name + "@someMail.com"
    }

    fun generateEvents(user: User): List<CalendarEvent> {
        val events = ArrayList<CalendarEvent>()
        events.addAll(mutableListOf(
                CalendarEvent("id1", LocalDate.now(), LocalTime.now(), "CalendarEvent 1", LocalTime.now().plusHours(2), user),
                CalendarEvent("id2", LocalDate.now(), LocalTime.now().plusHours(3), "CalendarEvent 2", LocalTime.now().plusHours(4), user))
        )
        return events
    }

    fun generateSettings(from: String, to: String): Settings {
        val startTime = LocalTime.parse(from)
        val endTime = LocalTime.parse(to)
        return Settings(startTime, endTime)
    }

    fun generateTasks(user: User): List<Task> {
        val tasks = java.util.ArrayList<Task>()

        tasks.addAll(listOf(
                Task("software architecture planning", 180, TaskDifficulty.CHALLENGING, TaskImportance.HIGH, user),
                Task("plan next week", 30, TaskDifficulty.EASY, TaskImportance.HIGH, user),
                Task("cleaning", 120, TaskDifficulty.EASY, TaskImportance.MEDIUM, user))
        )
        user.tasks = tasks
        return tasks
    }

    fun createDailyForm(user: User): DailyFormInfo {
        val dailyFormInfo = DailyFormInfo()
        dailyFormInfo.date = LocalDate.now()
        dailyFormInfo.sleepDuration = 6.0
        dailyFormInfo.sleepQuality = SleepQuality.BAD
        dailyFormInfo.stressLevel = StressLevel.HIGHER
        dailyFormInfo.owner = user

        return dailyFormInfo
    }
}


