package ch.uzh.ifi.seal.entities_tests

import ch.uzh.ifi.seal.domain_classes.Schedule
import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.User
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
 * Testing the creation and persistence of the schedule in the database
 * and also other classes which are associated with it
 * Entities under test:
 *      Schedule
 *      Task
 */
@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class SchedulePersistenceTest {

    @PersistenceContext
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var testUtil: TestUtil

    // creating a schedule without any events or tasks assigned to it
    fun createEmptySchedule(user: User): Schedule {
        val schedule = Schedule(LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(8))
        schedule.owner = user
        schedule.score = HardSoftScore.valueOf(50, 60)
        return schedule
    }


    @Test
    fun persistSchedule() {

        // given
        val expected = createEmptySchedule(testUtil.createUser("testUser"))

        // do
        em.persist(expected.owner)
        em.persist(expected)

        // verify
        val savedSchedule = em.createQuery("from Schedule", Schedule::class.java).singleResult!!
        assertEquals(expected, savedSchedule)
    }

    @Test
    fun scheduleWithTasks() {

        // given
        val user = testUtil.createUser("lada")
        val emptySchedule = createEmptySchedule(user)
        val tasks = testUtil.generateTasks(user)


        // do
        em.persist(user)
        em.persist(emptySchedule)
        emptySchedule.allTasks.addAll(tasks)
        for (task in tasks) {
            em.persist(task)
        }

        // verify
        val savedSchedule = em.createQuery("from Schedule", Schedule::class.java).singleResult!!
        val savedTasks = em.createQuery("from Task t where t.owner = :user", Task::class.java)
                .setParameter("user", user).resultList
        assertEquals(tasks, savedSchedule.allTasks)
        assertEquals(tasks, savedTasks)
    }

}