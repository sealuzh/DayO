package ch.uzh.ifi.seal.domain_classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Task is an actionable item which user has created in the application. Tasks are stored in the database
 * with the reference to the owner which cannot be null at the creation time.
 * <p>
 * For running of the optimization algorithm Task has a reference to the TimeSlot, which will not be persisted.
 * If the task is assigned, the new entity of AssignedTask will be created with the reference to the task and
 * startingTime which will be set to the startingTime of the respective TimeSlot.
 * <p>
 * Optional parameter: dueDate - user can choose, if the Task has a due date
 * Task state will be signified by two fields:
 * - completed - the date of the Task's completion, null if not completed
 * - deleted - date of the Task's deletion, null for not deleted tasks
 * <p>
 * All Tasks including completed and deleted will be saved in the database. User will also be able to see
 * the recently deleted tasks and undo the deletion, the same for completed tasks.
 */
@PlanningEntity//(difficultyComparatorClass = TaskDurationComparator.class)
@Entity
@Table(name = "task")
public class Task {

	// identification of the task in the database, auto-generated
	private int id;

	private String description;
	private int duration; //in minutes
	private TaskDifficulty difficulty;
	private TaskImportance importance;

	private LocalDate dueDate; //optional
	private LocalDate completed; // the date when task was completed, otherwise - null
	private LocalDate deleted; // the date when the task was deleted, otherwise - null
	private User owner; // reference to the owner, cannot be null
	private List<AssignedTask> assignments = new ArrayList<>();

	// not persisted
	private TimeSlot startingTimeSlot;

	public Task() {
	}

	public Task(String description, int duration, TaskDifficulty difficulty, TaskImportance importance, User owner) {
		this.description = description;
		this.duration = duration;
		this.difficulty = difficulty;
		this.importance = importance;
		this.owner = owner;
	}

	@PlanningVariable(valueRangeProviderRefs = "allSlots")
	@Transient
	public TimeSlot getStartingTimeSlot() {
		return startingTimeSlot;
	}

	public void setStartingTimeSlot(TimeSlot startingTimeSlot) {
		this.startingTimeSlot = startingTimeSlot;
	}

	@JsonIgnore
	@ManyToOne
	@JoinColumn(nullable = false)
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public LocalDate getCompleted() {
		return completed;
	}

	public void setCompleted(LocalDate completed) {
		this.completed = completed;
	}

	@Column(nullable = false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(nullable = false)
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Column(nullable = false)
	public TaskDifficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(TaskDifficulty difficulty) {
		this.difficulty = difficulty;
	}

	@Column(nullable = false)
	public TaskImportance getImportance() {
		return importance;
	}

	public void setImportance(TaskImportance importance) {
		this.importance = importance;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	@OneToMany(mappedBy = "task")
	@JsonIgnore
	public List<AssignedTask> getAssignments() {
		return assignments;
	}

	public void setAssignments(List<AssignedTask> assignments) {
		this.assignments = assignments;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "deletion_date")
	public LocalDate getDeleted() {
		return deleted;
	}

	public void setDeleted(LocalDate deleted) {
		this.deleted = deleted;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", id)
				.add("description", description)
				.add("duration", duration)
				.add("difficulty", difficulty)
				.add("importance", importance)
				.add("dueDate", dueDate)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Task task = (Task) o;

		if (duration != task.duration) return false;
		if (difficulty != task.difficulty) return false;
		if (importance != task.importance) return false;
		if (dueDate != null ? !dueDate.equals(task.dueDate) : task.dueDate != null) return false;
		return startingTimeSlot != null ? startingTimeSlot.equals(task.startingTimeSlot) : task.startingTimeSlot == null;
	}

	@Override
	public int hashCode() {
		int result = duration;
		result = 31 * result + difficulty.hashCode();
		result = 31 * result + importance.hashCode();
		result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
		result = 31 * result + (startingTimeSlot != null ? startingTimeSlot.hashCode() : 0);
		return result;
	}
}

