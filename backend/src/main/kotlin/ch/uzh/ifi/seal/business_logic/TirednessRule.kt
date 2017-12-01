package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.*
import ch.uzh.ifi.seal.domain_classes.SleepQuality
import ch.uzh.ifi.seal.domain_classes.SleepQuality.*
import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.TaskDifficulty
import ch.uzh.ifi.seal.domain_classes.User
import com.google.common.annotations.VisibleForTesting
import java.time.LocalDate

/**
 * Rule incorporates the logic about the impact of sleep duration and quality on the productivity level.
 * Factors:
 *      - sleep quality
 *      - sleep duration
 *      - task difficulty
 *
 * Generally, the larger sleep debt (hours slept - suggested sleep duration as defined in the ConfigurationConstants)
 *                                  => the lower the productivity
 *            the worse quality of sleep => the lower is the productivity
 *            the harder is the task => the more impact the previous two factors will have (positive as well as negative)
 *
 * Mostly this rule will give a large penalty for the difficult task, if the sleep duration or quality were insufficient
 * If sleep quality and duration were good, then score will be positive, showing the boost to productivity.
 *
 * The range of the score returned will be in the interval [-SCORE_SPAN, +SCORE_SPAN] whereas it will never be reaching
 * the upper bound, since the boost to the productivity is not as large as penalty.
 */
class TirednessRule (override var weight: Double = TIREDNESS_RULE_DEFAULT_WEIGHT): Rule {
    override fun init(user: User, today: LocalDate) {
        this.user = user
    }

    /* the cutoff for the sleep duration everything below this sleep duration
     *  will be treated as having equally negative impact on the productivity */
    internal val sleepDurationCutoff = 4.0

    lateinit var user: User

    override fun getScore(task: Task): Int {
        // information about the user related to user's tiredness
        user.dailyForms.sortBy { it.date } // sorting the daily forms by date to have the most recent one as last entry
        val dailyFormInfo = user.dailyForms.last()
        val sleepDuration = dailyFormInfo.sleepDuration
        val sleepQuality = dailyFormInfo.sleepQuality

        val sleepScore = calculateSleepScore(sleepDuration, sleepQuality)
        val sleepAndTaskDifficultyScore = task.difficulty.score * sleepScore

        return sleepAndTaskDifficultyScore.round()
    }

    @VisibleForTesting
    internal fun calculateSleepScore(sleepDuration: Double, sleepQuality: SleepQuality): Double {

        val normalizedSleepDuration = Math.max(sleepDurationCutoff, sleepDuration)
        val sleepDurationScore = getSleepDurationScore(normalizedSleepDuration)

        val res = if (sleepDurationScore < 0) {
            when (sleepQuality) {
            // good sleep quality will mitigate the negative effect of the shorter sleep duration
                GOOD, NORMAL ->
                    sleepDurationScore / sleepQuality.score
            // bad sleep quality magnifies the negative effect of the lack of sleep
                BAD, VERY_BAD ->
                    sleepDurationScore * Math.abs(sleepQuality.score)
            }
            /* if sleep duration is in sleep tolerance zone, the effect is of sleep quality
                depends largely on quality of sleep */
        } else {
            if (sleepQuality == NORMAL) 0.0 else sleepDurationScore + sleepQuality.score
        }
        return res * getNormalizationMultiplier()
    }

    /** Absolute value by which the calculated results will be multiplied to bring them to the same scale with other rules */
    private fun getNormalizationMultiplier() : Double {
        val minimalScore = getSleepDurationScore(sleepDurationCutoff) * VERY_BAD.score * TaskDifficulty.CHALLENGING.score

        return Math.abs(ConfigurationConstants.SCORE_SPAN / minimalScore)
    }

}
