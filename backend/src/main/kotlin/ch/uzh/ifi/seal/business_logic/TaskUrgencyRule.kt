package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.ConfigurationConstants
import ch.uzh.ifi.seal.TASK_URGENCY_RULE_DEFAULT_WEIGHT
import ch.uzh.ifi.seal.WORKING_DAYS
import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.User
import ch.uzh.ifi.seal.round
import java.lang.Math.sin
import java.time.LocalDate

/**
 * Gives the tasks which are due soon boost to the soft score so that they will be preferred before others.
 * Scores will belong to the interval [- SCORE_SPAN, + SCORE_SPAN] with minimum score for tasks without deadline,
 * or with very remote deadline, as it will be specified by the threshold.
 *
 * Now the score is adjusted to fit into the given interval for the threshold of 4 weeks.
 * To change the threshold to some other value need to tweak the constants in the formula for calculating the score,
 * so it will produce the values in the desired range.
 * Same will need to be done, if the range changes from [-100, +100] to something else.
 */
class TaskUrgencyRule (override var weight: Double = TASK_URGENCY_RULE_DEFAULT_WEIGHT): Rule {
    override fun init(user: User, today: LocalDate) {
        this.today = today
    }

    private val threshold = 4 // in weeks

    lateinit var today: LocalDate

    override fun getScore(task: Task): Int {
        if (task.dueDate == null || task.dueDate > today.plusWeeks(threshold.toLong())) {
            return -ConfigurationConstants.SCORE_SPAN // minimal score
        }

        val normalizedDueDate = if (task.dueDate < today) today else task.dueDate
        val x = workingDaysCache[normalizedDueDate]!!
        return (-sin(x/6.5 + 99) * 100).round()
    }

    private val workingDaysCache = mutableMapOf<LocalDate, Int>()

    fun recalculateWorkingDays(today: LocalDate): Map<LocalDate, Int> {
        workingDaysCache.clear()
        workingDaysCache[today] = 0
        return (1..threshold * 7)
                .map { today.plusDays(it.toLong()) }
                .associateByTo(workingDaysCache, { it }, { date ->
                    val previousWorkingDays = workingDaysCache[date.minusDays(1)]!!
                    previousWorkingDays + if (date.dayOfWeek in WORKING_DAYS) 1 else 0
                })
    }

}