package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.round

/**
 * Calculates the soft score for the assignment of the tasks for a particular day
 * using the information about the user (from daily form and settings) and about the task
 */
class SoftScoreCalculator {

    fun calculateSoftScore(tasks: List<Task>): Int {
        iter++

        val score = tasks.sumByDouble { task ->
            rules.sumByDouble { rule ->
                rule.getScore(task).toDouble() * rule.weight
            }
        }

        return score.round()
    }

    fun calculateScore(task: Task) : Int {
        return rules.sumByDouble { rule ->
            rule.getScore(task).toDouble() * rule.weight
        }.round()
    }
}

val urgencyRule = TaskUrgencyRule()
val rules = listOf(MorningnessEveningnessRule(), urgencyRule, TaskImportanceRule(), TirednessRule())

fun printSoftScoreStatistics() {
    println("Soft score calculated $iter times")
}

private var iter = 0