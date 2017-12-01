package ch.uzh.ifi.seal

import ch.uzh.ifi.seal.data_access.UserRepository
import ch.uzh.ifi.seal.domain_classes.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 *
 */
@Component
class LoggedInUserInfo(private val userRepository: UserRepository) {

    fun getUser(): User {
        val name = SecurityContextHolder.getContext().authentication.name
        return userRepository.findByName(name)!!
    }
}