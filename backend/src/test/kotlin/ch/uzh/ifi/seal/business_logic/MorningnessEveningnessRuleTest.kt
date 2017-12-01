package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.domain_classes.*
import ch.uzh.ifi.seal.domain_classes.MorningnessEveningnessType.*
import ch.uzh.ifi.seal.domain_classes.TaskDifficulty.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime


class MorningnessEveningnessRuleTest {

    // class under test
    private val meRule = MorningnessEveningnessRule()

    @Test
    fun `morning type && task assigned to productive period && fits - score max value`() {

        // prepare
        val user = getUser(MORNING_TYPE)
        val task = createTask(user, LocalTime.of(8, 30), 60)
        this.meRule.init(user, LocalDate.now())

        // do
        TaskDifficulty.values().forEach {
            task.difficulty = it
            val score = meRule.getScore(task)
            // verify
            when (task.difficulty) {
                CHALLENGING -> assert(score == 100)
                REGULAR -> assert(score == 0)
                EASY -> assert(score == -100)
            }
        }
    }


    @Test
    fun `neither type - score = 0`() {

        // prepare
        val user = getUser(NEITHER_TYPE)
        val task = createTask(user, LocalTime.of(8, 30), 60)
        this.meRule.init(user, LocalDate.now())

        // do
        TaskDifficulty.values().forEach {
            task.difficulty = it
            val score = meRule.getScore(task)
            println("score = $score")
            // verify
            assert(score == 0)
        }
    }

    @Test
    fun `morning type && task at the end of productive time - score = 0`() {

        // prepare
        val user = getUser(MORNING_TYPE)
        val task = createTask(user, LocalTime.of(11, 30), 60)
        this.meRule.init(user, LocalDate.now())

        // do
        TaskDifficulty.values().forEach {
            task.difficulty = it
            val score = meRule.getScore(task)
            // verify
            assert(score == 0)
        }
    }

    @Test
    fun `morning type && half of the task fits productive time - score less than maximum`() {

        // prepare
        val user = getUser(MORNING_TYPE)
        val task = createTask(user, LocalTime.of(11, 0), 60)
        this.meRule.init(user, LocalDate.now())

        // do
        TaskDifficulty.values().forEach {
            task.difficulty = it
            val score = meRule.getScore(task)
            // verify
            when (task.difficulty) {
                CHALLENGING -> assert(score == 50)
                REGULAR -> assert(score == 0)
                EASY -> assert(score == -50)
            }
        }
    }

    @Test
    fun `evening type && task partially fits productive time - score less than maximum`() {

        // prepare
        val user = getUser(EVENING_TYPE)
        val task = createTask(user, LocalTime.of(16, 30), 60)
        this.meRule.init(user, LocalDate.now())

        // do
        TaskDifficulty.values().forEach {
            task.difficulty = it
            val score = meRule.getScore(task)
            // verify
            when (task.difficulty) {
                CHALLENGING -> assert(score == 50)
                REGULAR -> assert(score == 0)
                EASY -> assert(score == -50)
            }
        }
    }

    @Test
    fun `evening type && task before the beginning of productive time - score = 0`() {

        // prepare
        val user = getUser(EVENING_TYPE)
        val task = createTask(user, LocalTime.of(11, 30), 60)
        this.meRule.init(user, LocalDate.now())

        // do
        TaskDifficulty.values().forEach {
            task.difficulty = it
            val score = meRule.getScore(task)
            // verify
            assert(score == 0)
        }
    }

    @Test
    fun `any type && task duration greater than productive time - score for challenging task not maximal`() {
        // todo test does not work with the NEITHER type
        MorningnessEveningnessType.values().forEach {
            // prepare
            val type = it
            val user = getUser(type)
            this.meRule.init(user, LocalDate.now())
            val task = if (type == EVENING_TYPE) {
                createTask(user, LocalTime.of(12, 0), 260, CHALLENGING)
            } else {
                createTask(user, LocalTime.of(8, 0), 260, CHALLENGING)
            }

            // do
            val score = meRule.getScore(task)
            println("score = $score")
            if (type != NEITHER_TYPE) {
                assert(score in 1..99)
            } else {
                assert(score == 0)
            }
        }
    }

    private fun createTask(user: User, startingTime: LocalTime, duration: Int, difficulty: TaskDifficulty = TaskDifficulty.REGULAR): Task {
        val task = Task("", duration, difficulty, TaskImportance.MEDIUM, user)
        task.startingTimeSlot = TimeSlot(startingTime, 1)
        return task
    }

    private fun getUser(prodType: MorningnessEveningnessType): User {
        val user = User("TestUser")
        user.settings = Settings()
        user.settings.daytimeProductivityType = prodType
        return user
    }
}