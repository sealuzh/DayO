package ch.uzh.ifi.seal

import ch.uzh.ifi.seal.data_access.UserRepository
import ch.uzh.ifi.seal.domain_classes.User
import mu.KLogging
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Creates users defined in the file ConfigurationConstants.kt,
 * if such user don't exist in the database yet.
 */
@Component
@Transactional
open class PrecreatedUsers(private val userRepository: UserRepository) {
    companion object : KLogging()

    @EventListener(ContextRefreshedEvent::class)
    fun populateUsersIfNeeded() {
        users.forEach {
            if (userRepository.findByName(it.login) == null) {
                userRepository.save(User(it.login, it.email))
                logger.info { "Saved new user: ${it.login} " }
            } else {
                logger.info { "User ${it.login} already exists in database" }
            }
        }
    }
}