package ch.uzh.ifi.seal.DTOs

import ch.uzh.ifi.seal.domain_classes.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Test the transformation of the Task and AssignedTask to DTO
 */
class TaskDTOTest {

    @Test
    fun assignedTaskToDto() {

        // prepare
        val task = Task("", 10, TaskDifficulty.REGULAR, TaskImportance.MEDIUM, User())
                .apply {
                    id = 1
                    dueDate = LocalDate.now()
                }
        val assignedTask = AssignedTask(LocalTime.of(9, 0), task)

        // do
        val actual = assignedTask.toDTO()

        // verify
        assertEquals(1, actual.id)
        assertEquals("", actual.description)
        assertEquals(10, actual.duration)
        assertEquals(LocalDate.now(), actual.dueDate)
        assertEquals(TaskDifficulty.REGULAR, actual.difficulty)
        assertEquals(TaskImportance.MEDIUM, actual.importance)
        assertEquals(LocalTime.of(9,0), actual.startingTime)
        assertEquals(null, actual.completed)
        assertEquals(null, actual.deleted)
    }

    @Test
    fun taskToDto() {

        // prepare
        val task = Task("", 10, TaskDifficulty.REGULAR, TaskImportance.MEDIUM, User())
                .apply {
                    id = 1
                    dueDate = LocalDate.now()
                }

        // do
        val actual = task.toDTO()

        // verify
        assertEquals(1, actual.id)
        assertEquals("", actual.description)
        assertEquals(10, actual.duration)
        assertEquals(LocalDate.now(), actual.dueDate)
        assertEquals(TaskDifficulty.REGULAR, actual.difficulty)
        assertEquals(TaskImportance.MEDIUM, actual.importance)
        assertEquals(null, actual.startingTime)
        assertEquals(null, actual.completed)
        assertEquals(null, actual.deleted)
    }
}
