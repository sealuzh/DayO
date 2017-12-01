package ch.uzh.ifi.seal.domain_classes

import ch.uzh.ifi.seal.entities_tests.TestUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


/**
 * Testing the persistence in the DB
 */
@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class AssignedTaskTest {

    @PersistenceContext
    private lateinit var em: EntityManager
    @Autowired
    private lateinit var testUtil: TestUtil

    @Test
    fun `persist the AssignedTask`() {

        // prepare
        val user = testUtil.createUser("tester")
        val tasks = testUtil.generateTasks(user)
        val events = testUtil.generateEvents(user)
        val schedule = Schedule(LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(16, 0))
                .apply {
                    allTasks = tasks
                    calendarEvents = events
                    score = HardSoftScore.valueOf(10, 100)
                    owner = user
                }
        em.persist(user)
        events.forEach { em.persist(it) }
        tasks.forEach { em.persist(it) }
        user.tasks.addAll(tasks)

        // do
        val assignedTasks = listOf(AssignedTask(LocalTime.of(10, 0), tasks[0]),
                AssignedTask(LocalTime.of(12, 0), tasks[1]),
                AssignedTask(LocalTime.of(14, 0), tasks[2]))

        em.persist(schedule)

        assignedTasks.forEach {
            it.schedule = schedule
            em.persist(it)
        }

        schedule.assignedTasks.addAll(assignedTasks)

        // verify
        assertEquals(assignedTasks, schedule.assignedTasks)
        println(schedule)

        println("Assigned tasks are: $assignedTasks")
    }

}