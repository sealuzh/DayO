package ch.uzh.ifi.seal.domain_classes;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;

/**
 * TimeSlot is a variable of the scheduling problem to which the @PlanningEntity (Task) is assigned
 * Every day the new time slots for the day will be generated before running the optimization algorithm.
 * <p>
 * TimeSlot is not saved to the database.
 */
public class TimeSlot implements Comparable<TimeSlot> {
	private final LocalTime startTime;
	private final int id;

	public TimeSlot(LocalTime startTime, int id) {
		this.startTime = startTime;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	@Override
	public int compareTo(@NotNull TimeSlot o) {
		return Integer.compare(id, o.id);
	}

	@Override
	public String toString() {
		return "TimeSlot{" +
				"startTime='" + startTime + '\'' +
				", id=" + id +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TimeSlot timeSlot = (TimeSlot) o;

		if (id != timeSlot.id) return false;
		return startTime != null ? startTime.equals(timeSlot.startTime) : timeSlot.startTime == null;
	}

	@Override
	public int hashCode() {
		int result = startTime != null ? startTime.hashCode() : 0;
		result = 31 * result + id;
		return result;
	}
}
