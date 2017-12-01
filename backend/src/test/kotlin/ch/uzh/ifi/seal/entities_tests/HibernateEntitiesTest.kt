package ch.uzh.ifi.seal.entities_tests

import ch.uzh.ifi.seal.data_access.UserRepository
import ch.uzh.ifi.seal.domain_classes.DailyFormInfo
import ch.uzh.ifi.seal.domain_classes.Settings
import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.TaskDifficulty.CHALLENGING
import ch.uzh.ifi.seal.domain_classes.TaskImportance.HIGH
import ch.uzh.ifi.seal.domain_classes.User
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


/**
 * Testing, if the entities are saved into the database with the expected parameters
 * Entities under test:
 *      User
 *      Settings
 *      DailyFormInfo
 *      Task
 */
@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class HibernateEntitiesTest {
    @PersistenceContext
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var testUtil: TestUtil

    @Test
    fun testSettings() {
        // given
        val user = testUtil.createUser("testUser")
        val expected = Settings(LocalTime.parse("08:00"), LocalTime.parse("18:00"))
        expected.owner = user

        // do
        em.persist(user)
        em.persist(expected)

        // verify
        val actual = em.createQuery("from Settings", Settings::class.java).singleResult!!
        assertEquals(user, actual.owner)
        assertEquals(LocalTime.parse("08:00"), actual.startOfDay)
        assertEquals(LocalTime.parse("18:00"), actual.endOfDay)
    }

    @Test
    fun testUserCreation() {
        // given
        val lada = testUtil.createUser("testUser")

        // do
        em.persist(lada)

        // verify
        val actual = em.createQuery("from User where name='testUser'", User::class.java).singleResult!!
        assertEquals("testUser", actual.name)
        assertEquals("testUser@someMail.com", actual.email)
    }

    @Test
    fun testDao() {
        userRepo.save(testUtil.createUser("Dima"))
        val res = userRepo.findByEmail("Dima@someMail.com")

        println(res)
    }

    @Test
    fun `save and get Settings from User`() {

        // given
        val user = testUtil.createUser("Lada")
        em.persist(user)
        val expected = Settings(LocalTime.parse("08:00"), LocalTime.parse("18:00"))
        expected.owner = user

        // do
        user.settings = expected
        em.persist(user)
        em.persist(expected)

        // verify
        val actual = em.createQuery("select u.settings from User u", Settings::class.java).singleResult!!
        assertEquals(expected.owner, actual.owner)
        assertEquals(expected.startOfDay, actual.startOfDay)
        assertEquals(expected.endOfDay, actual.endOfDay)
    }

    @Test
    fun `Task persistence`() {

        // given
        val user = testUtil.createUser("SomeUser")
        val expected = Task("some description", 30, CHALLENGING, HIGH, user)

        // do
        em.persist(user)
        em.persist(expected)

        // verify
        val actual = em.createQuery("select t from Task t", Task::class.java).singleResult!!
        assertEquals("some description", actual.description)
        assertEquals(30, actual.duration)
        assertEquals(CHALLENGING, actual.difficulty)
        assertEquals(HIGH, actual.importance)
        assertEquals(expected.owner, actual.owner)
    }

    @Test
    fun `persist and retrieve Task from user`() {

        // given
        val user = testUtil.createUser("testUser")
        val generatedTasks = testUtil.generateTasks(user)

        // do
        em.persist(user)
        for (task in generatedTasks) {
            em.persist(task)
        }
        val testUserId = user.id

        // verify
        val savedUser = em.createQuery("from User u where u.id = :id", User::class.java)
                .setParameter("id", testUserId)
                .singleResult!!
        val savedTasks = savedUser.tasks
        val tasksFromDB = em.createQuery("from Task t where t.owner = :user", Task::class.java)
                .setParameter("user", user).resultList
        val ownerOfTasks = tasksFromDB.first().owner
        // println("Tasks from the database: \n$tasksFromDB")

        assertEquals(3, savedTasks.size)
        assertEquals(generatedTasks, savedTasks)
        assertEquals(generatedTasks, tasksFromDB)
        assertEquals(user, ownerOfTasks)
        assertEquals(testUserId, savedUser.id)
    }

    @Test
    fun `persist and retrieve DailyFormInfo`() {

        // given
        val user = testUtil.createUser("testUser")
        val dailyFormInfo = testUtil.createDailyForm(user)
        user.dailyForms = ArrayList<DailyFormInfo>()
        user.dailyForms.add(dailyFormInfo)

        // do
        em.persist(user)
        em.persist(dailyFormInfo)

        // verify
        val actual = em.createQuery("from DailyFormInfo", DailyFormInfo::class.java).singleResult!!
        val fromUser = em.createQuery("from User where name='testUser'", User::class.java).singleResult!!.dailyForms.first()
        assertEquals(dailyFormInfo, actual)
        assertEquals(dailyFormInfo, fromUser)
    }

}