package ch.uzh.ifi.seal.DTOs

import ch.uzh.ifi.seal.domain_classes.AssignedTask
import ch.uzh.ifi.seal.domain_classes.Task
import ch.uzh.ifi.seal.domain_classes.TaskDifficulty
import ch.uzh.ifi.seal.domain_classes.TaskImportance
import java.time.LocalDate
import java.time.LocalTime

/**
 * Class for passing the information about the task and task's assignment to client.
 * Has id which references the Task.id to allow manipulation on tasks
 */
data class TaskDTO(val id: Int, // task's id
                   val description: String,
                   val duration: Int,
                   val difficulty: TaskDifficulty,
                   val importance: TaskImportance,
                   val startingTime: LocalTime?,
                   val dueDate: LocalDate?,
                   val completed: LocalDate?,
                   val deleted: LocalDate?)

fun AssignedTask.toDTO(): TaskDTO {
    val task = this.task
    return TaskDTO(task.id, task.description, task.duration, task.difficulty,
            task.importance, startingTime = this.startingTime, dueDate = task.dueDate,
            completed = task.completed, deleted = task.deleted)
}

fun Task.toDTO(duration: Int? = null, startingTime: LocalTime? = null): TaskDTO {
    return TaskDTO(id, description, duration ?: this.duration, difficulty, importance, startingTime = startingTime,
            dueDate = dueDate, completed = completed, deleted = deleted)
}

