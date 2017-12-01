package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.ConfigurationConstants
import ch.uzh.ifi.seal.ME_RULE_DEFAULT_WEIGHT
import ch.uzh.ifi.seal.domain_classes.MorningnessEveningnessType
import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.TaskDifficulty
import ch.uzh.ifi.seal.domain_classes.User
import ch.uzh.ifi.seal.round
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Gives the score for the task depending on the time of day when the task is assigned
 * and the morningness-eveningness settings of the user.
 * Weight of the rule shows how important it is as compared to other rules.
 *
 * Generally, it is strongly preferred to assign the most difficult tasks during productive periods.
 * Therefore, the score of this rule will be conforming to the following statement:
 *      score(CHALLENGING) > score(REGULAR) > score(EASY)
 *      given tasks with similar duration assigned to the same time.
 *
 * Score will belong to the interval of [- SCORE_SPAN, + SCORE_SPAN]
 */
class MorningnessEveningnessRule(override var weight: Double = ME_RULE_DEFAULT_WEIGHT) : Rule {
    override fun init(user: User, today: LocalDate) {
        this.user = user
    }

    private val afternoonStartProdPeriod = LocalTime.of(14, 0)
    private val afternoonEndProdPeriod = LocalTime.of(17, 0)

    private val morningStartProdPeriod = LocalTime.of(8, 30)
    private val morningEndProdPeriod = LocalTime.of(11, 30)

    lateinit var user: User

    private val cache = ConcurrentHashMap<Task, Int>()

    override fun getScore(task: Task): Int = cache.computeIfAbsent(task, this::doGetScore)

    private fun doGetScore(task: Task): Int {
        val productivityType = user.settings.daytimeProductivityType
        var score = when (productivityType) {
            MorningnessEveningnessType.EVENING_TYPE ->
                calcProdScore(afternoonStartProdPeriod, afternoonEndProdPeriod, task)
            MorningnessEveningnessType.NEITHER_TYPE -> 0
            MorningnessEveningnessType.MORNING_TYPE ->
                calcProdScore(morningStartProdPeriod, morningEndProdPeriod, task)
            else ->
                throw IllegalArgumentException("daytimeProductivityType of the user must be not null!")
        }

        // boost to difficult tasks => positive score
        // CHALLENGING preferred to REGULAR preferred to EASY => 1 > 0 > -1
        score *= when (task.difficulty!!) {
            TaskDifficulty.EASY -> -1
            TaskDifficulty.REGULAR -> 0
            TaskDifficulty.CHALLENGING -> 1
        }

        return score
    }

    private fun calcProdScore(startProdPeriod: LocalTime, endProdPeriod: LocalTime, task: Task): Int {
        val taskStartsAt = task.startingTimeSlot.startTime
        val taskEndsAt = taskStartsAt.plusMinutes(task.duration.toLong())
        val maxScore = ConfigurationConstants.SCORE_SPAN

        // this rule has nothing to say about tasks which do not overlap with productive time
        if (taskEndsAt <= startProdPeriod || taskStartsAt >= endProdPeriod) {
            return 0
            // task is completed during productive time
        } else if (taskStartsAt >= startProdPeriod && taskEndsAt <= endProdPeriod) {
            return maxScore
            // task start before productive time and ends during productive time
        } else if (taskStartsAt < startProdPeriod && taskEndsAt <= endProdPeriod) {
            val factor = getFraction(startProdPeriod, taskEndsAt, task)
            return Math.round(maxScore * factor).toInt()
            // task starts during productive time and end after productive time
        } else if (taskStartsAt >= startProdPeriod && taskEndsAt > endProdPeriod) {
            val factor = getFraction(taskStartsAt, endProdPeriod, task)
            return (factor * maxScore).round()
            // task starts earlier and ends later than productive period
        } else {
            val fraction = getFraction(startProdPeriod, endProdPeriod, task)
            return (fraction * maxScore).round()
        }
    }

    /** @return fraction of a task which is fitting into productive period */
    private fun getFraction(from: LocalTime?, to: LocalTime?, task: Task): Double {
        val factor = Duration.between(from, to).toMinutes() / task.duration.toDouble()
        return factor
    }

}