package ch.uzh.ifi.seal.controllers

import ch.uzh.ifi.seal.LoggedInUserInfo
import ch.uzh.ifi.seal.data_access.TaskRepository
import ch.uzh.ifi.seal.domain_classes.Task
import mu.KLogging
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.transaction.Transactional

/**
 * Managing the tasks of the user
 */
@RestController
@Transactional
@RequestMapping("tasks")
class TaskController(val taskRepo: TaskRepository,
                     val loggedInUserInfo: LoggedInUserInfo) {

    companion object : KLogging()

    @GetMapping("")
    fun getAllTasks(): List<Task> {
        val user = loggedInUserInfo.getUser()

        /* returning the tasks including the recently deleted ones
           completed tasks are also returned, the representation will be managed on frontend */
        return user.tasks
                .filter {
                    (it.deleted == null || it.deleted.isAfter(LocalDate.now().minusDays(4)))
                            && (it.completed == null || it.completed.isAfter(LocalDate.now().minusDays(4)))
                }
    }

    @PostMapping("/add")
    fun addTask(@RequestBody newTask: Task): Task {
        val user = loggedInUserInfo.getUser()
        newTask.owner = user
        user.tasks.add(taskRepo.save(newTask))
        logger.info("saved new task $newTask for user $user")
        return newTask
    }

    @PutMapping("/update")
    fun updateTask(@RequestBody updatedTask: Task) {
        logger.info("got updated task: $updatedTask")
        val user = loggedInUserInfo.getUser()
        updatedTask.owner = user
        logger.info("Saving the task $updatedTask to the DB")
        taskRepo.save(updatedTask)
    }

    @PutMapping("/delete/{taskId}")
    fun deleteTask(@PathVariable taskId: Int, @RequestBody taskToDelete: Task) {
        val task = taskRepo.findById(taskId)
        // todo is the task already updated or do I need to save() it?
        task.deleted = LocalDate.now()
        logger.info("Task $task has been deleted")
    }

    @PutMapping("undoDelete/{taskId}")
    fun undoDelete(@PathVariable taskId: Int): Task {
        val task = taskRepo.findById(taskId)
        task.apply { deleted = null; owner = loggedInUserInfo.getUser() }
        return task
    }

    @PutMapping("complete/{taskId}")
    fun complete(@PathVariable taskId: Int) {
        val task = taskRepo.findById(taskId).apply { completed = LocalDate.now() }
        logger.info("Completed task $task")
    }

    @PutMapping("undoComplete/{taskId}")
    fun undoComplete(@PathVariable taskId: Int): Task {
        val task = taskRepo.findById(taskId).apply { completed = null }
        return task
    }
}