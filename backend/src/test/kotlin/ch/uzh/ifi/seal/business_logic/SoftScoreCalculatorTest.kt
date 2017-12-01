package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.*
import ch.uzh.ifi.seal.entities_tests.TestUtil
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class SoftScoreCalculatorTest {

    // class under test
    private val softScoreCalc = SoftScoreCalculator()
    private val testUtil = TestUtil()
    @Test
    fun `positive score for the high priority tasks`() {

        // prepare
        val user = User("Test User")
        user.settings = testUtil.generateSettings("10:00", "17:00")
        user.settings.daytimeProductivityType = MorningnessEveningnessType.MORNING_TYPE
        user.dailyForms.add(DailyFormInfo(LocalDate.now(), 8.0, SleepQuality.NORMAL, StressLevel.INSIGNIFICANT, user))

        rules.forEach { it.init(user, LocalDate.now()) }

        val task = Task("", 60, TaskDifficulty.CHALLENGING, TaskImportance.HIGH, user)
        task.startingTimeSlot = TimeSlot(LocalTime.of(10, 0), 1)

        // do
        val score = softScoreCalc.calculateSoftScore(listOf(task))
        println(score)
        // verify
        assert(score > 0)
    }
}