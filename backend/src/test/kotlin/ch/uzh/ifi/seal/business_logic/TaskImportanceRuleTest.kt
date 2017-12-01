package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.TaskImportance
import ch.uzh.ifi.seal.domain_classes.User
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskImportanceRuleTest {

    // class under test
    private val taskImportanceRule = TaskImportanceRule()

    @Test
    fun getWeight() {
        val weight = taskImportanceRule.weight
        assert(weight < 1)
    }

    @Test
    fun getScore() {

        // prepare
        val user = User("TestUser")
        val task = Task()

        // do
        TaskImportance.values().forEach {
            importance ->
            task.importance = importance
            val score = taskImportanceRule.getScore(task)

            // verify
            when (importance) {
                TaskImportance.LOW -> assertEquals(-100, score)
                TaskImportance.MEDIUM -> assertEquals(0, score)
                TaskImportance.HIGH -> assertEquals(100, score)
            }
        }
    }

}