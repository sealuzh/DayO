package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Testing the correctness of the stress rule score implementation
 */
class StressRuleTest{

    // class under test
    val stressRule = StressRule()

    // prepare
    val user = User("testUser", "test@email.com")
    val today = LocalDate.now()

    @Test
    fun `highest stress level and challenging task - minimal score`() {

        // prepare
        val dailyFormInfo = DailyFormInfo(today, 0.0, null, StressLevel.TOO_HIGH, user)
        user.dailyForms.add(dailyFormInfo)
        val task = Task("", 10, TaskDifficulty.CHALLENGING, null, user)

        // do
        stressRule.init(user, today)
        val score = stressRule.getScore(task)

        // verify
        assertEquals(-100, score)
    }

    @Test
    fun `all levels of stress - 0 when normal and less, negative when higher`() {

        for (level in StressLevel.values()) {
            // prepare
            val dailyFormInfo = DailyFormInfo(today, 0.0, null, level, user)
            user.dailyForms.add(dailyFormInfo)
            val task = Task("", 10, TaskDifficulty.CHALLENGING, null, user)

            // do
            stressRule.init(user, today)
            val score = stressRule.getScore(task)

            // verify
            when(level) {
                StressLevel.INSIGNIFICANT, StressLevel.USUAL -> assertEquals(0, score)
                else -> assert(score < 0)
            }
            println("stress level: $level, task difficulty: challenging, score = $score")
            // cleanup
            user.dailyForms.removeAt(0)
        }
    }

    @Test
    fun `all levels of stress and all task difficulties - 0 when normal and less, negative when higher`() {

        for (level in StressLevel.values()) {
            // prepare
            val dailyFormInfo = DailyFormInfo(today, 0.0, null, level, user)
            user.dailyForms.add(dailyFormInfo)
            for (difficulty in TaskDifficulty.values()) {
                val task = Task("", 10, difficulty, null, user)

                // do
                stressRule.init(user, today)
                val score = stressRule.getScore(task)

                // verify
                when(level) {
                    StressLevel.INSIGNIFICANT, StressLevel.USUAL -> assertEquals(0, score)
                    else -> assert(score < 0)
                }
                println("stress level: $level, task difficulty: $difficulty, score = $score")
            }
            // cleanup
            user.dailyForms.removeAt(0)
        }
    }
}