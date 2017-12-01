package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.User
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class TaskUrgencyRuleTest {

    // class under test
    private val taskUrgencyRule = TaskUrgencyRule()

    @Test
    fun `recalculateWorkingDays today is Friday`() {
        val baseDate = LocalDate.now().with(DayOfWeek.FRIDAY)
        val res = taskUrgencyRule.recalculateWorkingDays(baseDate)

        assertEquals(0, res[baseDate])
        assertEquals(0, res[baseDate.plusDays(1)])
        assertEquals(0, res[baseDate.plusDays(2)])
        assertEquals(1, res[baseDate.plusDays(3)])
        assertEquals(5, res[baseDate.plusWeeks(1)])
        assertEquals(20, res[baseDate.plusWeeks(4)])
    }

    @Test
    fun `recalculateWorkingDays today is Saturday`() {
        val baseDate = LocalDate.now().with(DayOfWeek.SATURDAY)
        val res = taskUrgencyRule.recalculateWorkingDays(baseDate)

        assertEquals(0, res[baseDate])
        assertEquals(0, res[baseDate.plusDays(1)])
        assertEquals(1, res[baseDate.plusDays(2)])
        assertEquals(2, res[baseDate.plusDays(3)])
        assertEquals(5, res[baseDate.plusWeeks(1)])
        assertEquals(20, res[baseDate.plusWeeks(4)])
    }

    @Test
    fun `recalculateWorkingDays today is Tuesday`() {
        val baseDate = LocalDate.now().with(DayOfWeek.TUESDAY)
        val res = taskUrgencyRule.recalculateWorkingDays(baseDate)

        assertEquals(0, res[baseDate])
        assertEquals(1, res[baseDate.plusDays(1)])
        assertEquals(2, res[baseDate.plusDays(2)])
        assertEquals(3, res[baseDate.plusDays(3)])
        assertEquals(5, res[baseDate.plusWeeks(1)])
        assertEquals(20, res[baseDate.plusWeeks(4)])
    }

    @Test
    fun `calculate score`() {
        val today = LocalDate.now().with(DayOfWeek.MONDAY)

        // scores for working days:  -5   -1    0   1   4  10   15    20    21    30
        val expectedScores = listOf(100, 100, 100, 98, 79, -1, -70, -100, -100, -100)

        taskUrgencyRule.recalculateWorkingDays(today)
        taskUrgencyRule.init(User("testUser", "test@mail.com"), today)

        // calendar days corresponding to working days above
        val actual = listOf(-7, -3, 0, 1, 4, 14, 21, 28, 29, 42).map {
            val date = today.plusDays(it.toLong())
            val task = Task().apply { dueDate = date }
            taskUrgencyRule.getScore(task)
        }

        assertThat(actual, Matchers.equalTo(expectedScores))
    }

}
