package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.DailyFormInfo
import ch.uzh.ifi.seal.domain_classes.SleepQuality
import org.junit.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TirednessRuleTest {

    // class under test
    private val tirednessRule = TirednessRule()

    @Test
    fun `SleepQuality BAD and SleepDuration less than 7 hours - score = negative result`() {

        // given
        val dailyForm = DailyFormInfo(null, 6.5, SleepQuality.BAD, null, null)

        // do
        val sleepScore = calcScore(dailyForm.sleepDuration, dailyForm.sleepQuality)

        // verify
        assert(sleepScore < 0)
    }

    @Test
    fun `SleepDuration between 7 an 8 hours for any SleepQuality - score sign depends on SleepQuality`() {

        // given
        val duration = 7.9

        // do
        SleepQuality.values().forEach {
            val sleepScore = calcScore(duration, it)

            // verify
            when (it) {
                SleepQuality.BAD, SleepQuality.VERY_BAD -> assert(sleepScore < 0)
                SleepQuality.NORMAL -> assert(sleepScore >= 0)
                SleepQuality.GOOD -> assert(sleepScore > 0)
            }
        }

    }

    @Test
    fun `SleepDuration more than or equal to 8 hours - positive score`() {

        val duration = 8.1

        SleepQuality.values().forEach {
            println(it)
            val sleepScore = tirednessRule.calculateSleepScore(duration, it)
            println(sleepScore)
            when (it) {
                SleepQuality.BAD, SleepQuality.VERY_BAD -> assert(sleepScore < 0)
                SleepQuality.NORMAL -> assert(sleepScore >= 0)
                SleepQuality.GOOD -> assert(sleepScore > 0)
            }

        }
    }

    @Test
    fun `SleepDuration less than 7 hours - negative score`() {

        var duration = 0.0
        while (duration < 7) {
            SleepQuality.values().forEach {
                val sleepScore = calcScore(duration, it)
                assert(sleepScore < 0)
            }
            duration += 0.4
        }
    }

    @Test
    fun `Test all possible combinations to see the value distribution`() {

        for (sleepQuality in SleepQuality.values()) {
            var i = 0.0
            while (i < 10) {
                val score = calcScore(i, sleepQuality)
                println("$i $sleepQuality ${round(score * 3)} ${round(score * 2)} ${round(score * 1)}")
                // println("duration = $i quality = $sleepQuality --> Difficult: ${round(score * 3)}. Normal: ${round(score * 2)}. Easy: ${round(score * 1)}")
                i += 0.5
            }
        }
    }

    private fun calcScore(duration: Double, sleepQual: SleepQuality) = tirednessRule.calculateSleepScore(duration, sleepQual)

    private fun round(number: Double): Double {
        return (Math.round(number * 100) / 100.0)
    }
}