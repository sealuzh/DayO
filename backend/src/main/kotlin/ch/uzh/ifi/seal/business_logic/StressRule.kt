package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.ConfigurationConstants
import ch.uzh.ifi.seal.STRESS_RULE_DEFAULT_WEIGHT
import ch.uzh.ifi.seal.domain_classes.StressLevel
import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.TaskDifficulty
import ch.uzh.ifi.seal.domain_classes.User
import ch.uzh.ifi.seal.round
import ch.uzh.ifi.seal.score
import java.time.LocalDate

class StressRule(override var weight: Double = STRESS_RULE_DEFAULT_WEIGHT) : Rule {
    lateinit var user: User
    lateinit var today: LocalDate

    override fun init(user: User, today: LocalDate) {
        this.user = user
        this.today = today
    }

    override fun getScore(task: Task): Int {
        // the maximal baseScore for normalization
        val maxScore = Math.abs(StressLevel.TOO_HIGH.score * TaskDifficulty.CHALLENGING.score)
        val stressLevel = user.dailyForms.find { it.date == today }!!.stressLevel
        val baseScore = stressLevel.score * task.difficulty.score
        return (baseScore * ConfigurationConstants.SCORE_SPAN.toDouble()/maxScore).round()
    }
}