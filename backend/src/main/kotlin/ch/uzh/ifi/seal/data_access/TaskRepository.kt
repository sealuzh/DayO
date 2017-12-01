package ch.uzh.ifi.seal.data_access

import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.User
import org.springframework.data.repository.CrudRepository

/**
 * Methods and queries for retrieving of the tasks from the database
 */
interface TaskRepository : CrudRepository<Task, Int> {

    /* returns all tasks of the user */
    fun findByOwner(owner: User): List<Task>

    /* returns one task with given id */
    fun findById(id: Int): Task

}
