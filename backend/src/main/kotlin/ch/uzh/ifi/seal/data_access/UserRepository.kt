package ch.uzh.ifi.seal.data_access

import ch.uzh.ifi.seal.domain_classes.User
import org.springframework.data.repository.CrudRepository

/**
 * Repository for getting the relevant information about the user
 * Including the settings of the user and the tasks which user has
 */
interface UserRepository : CrudRepository<User, Integer> {
    fun findByEmail(email: String): List<User>

    fun findByName(name: String): User?

    fun findById(id: Int): User?

}