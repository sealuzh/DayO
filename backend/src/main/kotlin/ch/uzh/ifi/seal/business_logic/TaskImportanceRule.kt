package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.ConfigurationConstants
import ch.uzh.ifi.seal.TASK_IMPORTANCE_RULE_DEFAULT_WEIGHT
import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.TaskImportance.*
import ch.uzh.ifi.seal.domain_classes.User
import java.time.LocalDate

/**
 * Task importance is defined by the importance attribute the user has set for the task.
 * The TaskImportanceRule gives maximum score to the high priority tasks and minimum score to the low priority tasks.
 *
 * Scores must satisfy this relation: high priority > medium priority > low priority
 *      the inequality must be strict.
 * Scores belong to the interval [- SCORE_SPAN, + SCORE_SPAN] which can be set in {@link ConfigurationConstants}
 */
class TaskImportanceRule (override var weight: Double = TASK_IMPORTANCE_RULE_DEFAULT_WEIGHT) : Rule {
    override fun init(user: User, today: LocalDate) {}

    override fun getScore(task: Task) = when (task.importance) {
            LOW -> - ConfigurationConstants.SCORE_SPAN
            MEDIUM -> 0
            HIGH -> ConfigurationConstants.SCORE_SPAN
            else -> throw IllegalArgumentException("task priority must not be null!")
        }
}