package ch.uzh.ifi.seal.controllers

import ch.uzh.ifi.seal.LoggedInUserInfo
import ch.uzh.ifi.seal.business_logic.CalendarSynchronization
import ch.uzh.ifi.seal.business_logic.ScheduleGenerator
import ch.uzh.ifi.seal.business_logic.rules
import ch.uzh.ifi.seal.business_logic.urgencyRule
import ch.uzh.ifi.seal.data_access.DailyFormInfoRepository
import ch.uzh.ifi.seal.data_access.UserRepository
import ch.uzh.ifi.seal.domain_classes.DailyFormInfo
import ch.uzh.ifi.seal.domain_classes.Schedule
import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.User
import mu.KLogging
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionOperations
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Responsible for receiving and saving of the daily from information
 */
@RestController
@RequestMapping("/dailyForms")
@Transactional
class DailyFormController(val dailyFormRepo: DailyFormInfoRepository, val userRepo: UserRepository,
                          val scheduleGenerator: ScheduleGenerator,
                          private val calSync: CalendarSynchronization,
                          private val loggedInUserInfo: LoggedInUserInfo,
                          private val txOps: TransactionOperations) {

    companion object : KLogging()

    private val executor = Executors.newSingleThreadExecutor()

    @PersistenceContext
    private lateinit var em: EntityManager

    @GetMapping("/")
    fun getAllDailyFormsForUser(auth: Authentication): List<DailyFormInfo> {
        val user = loggedInUserInfo.getUser()
        return user.dailyForms
    }

    @PostMapping("/add")
    fun addDailyForm(@RequestBody dailyForm: DailyFormInfo, auth: Authentication) {
        val today = LocalDate.now()
        val user = loggedInUserInfo.getUser()

        val existingDailyForm = dailyFormRepo.findByOwnerAndDate(user, today)
        if (existingDailyForm != null) {
            logger.warn { "Daily form for $today and for $user already present, not saving it second time" }
            return
        }

        println("the user name = ${auth.name} and user is $user")

        dailyForm.date = today
        dailyForm.owner = user
        user.dailyForms.add(dailyFormRepo.save(dailyForm))
        logger.info("saved new daily form $dailyForm linked to user $user")

        val userId = user.id

        CompletableFuture.runAsync(Runnable {
            txOps.execute {
                val userInner = userRepo.findById(userId)!!
                urgencyRule.recalculateWorkingDays(today)

                // when the daily form is submitted, the algorithm for generation of schedule is started
                // events are synchronized daily and passed to the schedule generator
                val events = calSync.getCalendarEvents(userInner)
                // rules need to be initialized!
                rules.forEach { it.init(userInner, today) }

                // generate and save the schedules for the further use
                val schedules = scheduleGenerator.generateScheduleOptions(userInner, today, events)

                persistSchedules(schedules, userInner)

                em.createQuery("select t from Task t where t.owner = :user", Task::class.java).setParameter("user", userInner)
                        .resultList.let { println("Tasks are " + it) }
            }
        }, executor).exceptionally {
            logger.error(it, { "Exception while asynchronously generating schedules" })
            null
        }
    }

    private fun persistSchedules(schedules: List<Schedule>, user: User) {

        user.schedules.addAll(schedules)

        schedules.forEach { schedule ->
            schedule.calendarEvents.forEach {
                it.schedules.add(schedule)
                em.persist(it)
            }
            schedule.assignedTasks.forEach {
                it.schedule = schedule
                em.persist(it)
            }
        }
        schedules.forEach(em::persist)
    }
}

