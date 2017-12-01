package ch.uzh.ifi.seal.domain_classes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalTime;

/**
 * Class representing the Task which has been assigned. Has the assigned date
 * and the starting time. References the Task.
 */
@Entity
public class AssignedTask {
	private int id;
	private LocalTime startingTime;
	private Task task; // Task containing the information about the task itself
	private Schedule schedule; // schedule to which the AssignedTask was assigned

	public AssignedTask() {
	}

	public AssignedTask(LocalTime startingTime, Task task) {
		this.startingTime = startingTime;
		this.task = task;
	}

	public LocalTime getStartingTime() {
		return startingTime;
	}

	public void setStartingTime(LocalTime startingTime) {
		this.startingTime = startingTime;
	}

	@ManyToOne
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	@ManyToOne
	@JsonIgnore
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "AssignedTask{" +
				"id=" + id +
				", startingTime=" + startingTime +
				", task=" + task +
				'}';
	}
}
