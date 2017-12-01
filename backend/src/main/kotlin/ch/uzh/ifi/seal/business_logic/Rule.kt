package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.User
import java.time.LocalDate

/**
 * Rules are for calculating how much some aspect of the user or the task will influence the score.
 * Each rule will have a weight showing how important that rule is compared to other rules.
 * All weights across rules need to be normalized.
 */
interface Rule {
    var weight: Double
    fun getScore(task: Task) : Int
    fun init(user: User, today: LocalDate)
}