package ch.uzh.ifi.seal.controllers

import ch.uzh.ifi.seal.DTOs.ScheduleDTO
import ch.uzh.ifi.seal.DTOs.toDTO
import ch.uzh.ifi.seal.data_access.DailyFormInfoRepository
import ch.uzh.ifi.seal.data_access.UserRepository
import ch.uzh.ifi.seal.domain_classes.Schedule
import ch.uzh.ifi.seal.domain_classes.Settings
import ch.uzh.ifi.seal.domain_classes.User
import mu.KLogging
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

/**
 * From this controller information about the user can be retrieved and managed.
 * (settings -- retrieving, updating; state -- retrieving).
 */
@RestController
@Transactional
@RequestMapping("/user")
class UserController(private val userRepo: UserRepository,
                     private val dailyFormRepository: DailyFormInfoRepository) {

    companion object : KLogging()

    @PersistenceContext
    private lateinit var em: EntityManager

    @GetMapping("")
    fun getCurrentUser(): User {
        return getLoggedInUser()
    }

    @GetMapping("state")
    fun getState(): UserState {
        val user = getLoggedInUser()
        val dailyForm = dailyFormRepository.findByOwnerAndDate(user, LocalDate.now())
        return UserState(user.settings != null, user.tasks.isNotEmpty(), dailyForm != null)
    }


    @PostMapping("/create")
    fun createUser(@RequestBody input: User): User {
        val newUser = userRepo.save(input)
        return newUser
    }

    @GetMapping("/settings")
    fun getSettings(): Settings? {
        return getLoggedInUser().settings
    }

    @PutMapping("/createOrUpdateSettings")
    fun createOrUpdateSettings(@RequestBody settings: Settings) {
        val user = getLoggedInUser()
        if (user.settings == null) {
            user.settings = settings
            settings.owner = user
            logger.info("saved settings $settings")
            em.persist(settings)
        } else {
            user.settings = em.merge(settings)
            user.settings.owner = user
        }
    }

    @GetMapping("/schedule/options")
    fun getScheduleOptions(): List<ScheduleDTO> = getLoggedInUser().schedules
            .filter { it.forDate == LocalDate.now() }
            .map { it.toDTO() }

    @GetMapping("/schedule/chosen")
    fun getScheduleForToday(): ScheduleDTO? = getLoggedInUser().schedules
            .singleOrNull { it.forDate == LocalDate.now() && it.isChosen }
            ?.toDTO()

    @RequestMapping("/schedule/{scheduleId}/select") // todo switch to @GetMapping
    fun chooseScheduleOption(@PathVariable scheduleId: Int) {
        em.find(Schedule::class.java, scheduleId).isChosen = true
    }

    private fun getLoggedInUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication !is AnonymousAuthenticationToken) {
            val currentUserName = authentication.name
            val currentUser = userRepo.findByName(currentUserName)
            if (currentUser == null) {
                throw IllegalStateException("User with name $currentUserName was not found in the database!")
            } else {
                return currentUser
            }
        }
        throw IllegalStateException("There is no logged in user!")
    }
}

class UserState(val hasSettings: Boolean, val hasTasks: Boolean, val hasDailyForm: Boolean)