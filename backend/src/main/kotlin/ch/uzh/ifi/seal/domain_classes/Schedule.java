package ch.uzh.ifi.seal.domain_classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Schedule is an entity managed by Hibernate as well as planning solution for the OptaPlanner to operate.
 * <p>
 * Schedule holds following information:
 * - date, for which it is made
 * - start and end of the day (corresponding to the user settings which were valid at that time)
 * - fixed appointments for the day, which is subset of all events user has
 * - time slots which can be allocated to tasks
 * - all uncompleted and not yet deleted tasks which can be planned for the day
 * - tasks which were assigned (planned) for the day
 * - the score representing the "value" of the schedule
 * <p>
 * OptaPlanner relevant references are:
 * -> time slots (including the TimeSlot representing the "unassigned" slot with id=-1 and startingTime=00:00)
 * -> all not yet completed or deleted tasks as planning entity
 * both lists will be used for the optimization algorithm, but not persisted in the database
 * <p>
 * Persisted information includes:
 * - id (auto-generated)
 * - date
 * - start and end of the day
 * - tasks which were assigned for that day
 * - events of that day
 * <p>
 * Schedule also holds the reference to the owner (User) which cannot be null at the persistence time
 */
@Entity
@PlanningSolution
public class Schedule implements Solution<HardSoftScore> {

	private int id;

	private LocalDate forDate;
	private LocalTime startOfDay;
	private LocalTime endOfDay;

	// the lists are initialized to make the process of adding the elements easier
	private List<CalendarEvent> calendarEvents = new ArrayList<>(); //representing the events which are already given
	private List<AssignedTask> assignedTasks = new ArrayList<>();

	// used for solving of the planning optimization problem and NOT persisted into the database
	private List<TimeSlot> timeSlots = new ArrayList<>();
	private List<Task> allTasks = new ArrayList<>(); //all not completed tasks which can be assigned
	private HardSoftScore score;

	private int hardScore;
	private int softScore;
	private User owner;
	private boolean chosen; // user is presented with 3 options, then chooses one

	public Schedule() {
	}

	public Schedule(LocalTime startOfDay, LocalTime endOfDay, List<CalendarEvent> calendarEvents, List<TimeSlot> timeSlots, List<Task> allTasks) {
		this.startOfDay = startOfDay;
		this.endOfDay = endOfDay;
		this.calendarEvents = calendarEvents.stream().sorted().collect(Collectors.toList());
		this.timeSlots = timeSlots;
		this.allTasks = allTasks;
	}

	public Schedule(LocalDate forDate, LocalTime startOfDay, LocalTime endOfDay, List<CalendarEvent> calendarEvents, List<TimeSlot> timeSlots, List<Task> allTasks, User owner) {
		this.forDate = forDate;
		this.startOfDay = startOfDay;
		this.endOfDay = endOfDay;
		this.calendarEvents = calendarEvents;
		this.timeSlots = timeSlots;
		this.allTasks = allTasks;
		this.owner = owner;
	}

	public Schedule(LocalDate forDate, LocalTime startOfDay, LocalTime endOfDay) {
		this.forDate = forDate;
		this.startOfDay = startOfDay;
		this.endOfDay = endOfDay;
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

	public boolean isChosen() {
		return chosen;
	}

	public void setChosen(boolean chosen) {
		this.chosen = chosen;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "for_date", nullable = false)
	public LocalDate getForDate() {
		return forDate;
	}

	public void setForDate(LocalDate forDate) {
		this.forDate = forDate;
	}

	@Column(name = "start_of_day", nullable = false)
	public LocalTime getStartOfDay() {
		return startOfDay;
	}

	public void setStartOfDay(LocalTime startOfDay) {
		this.startOfDay = startOfDay;
	}

	@Column(name = "end_of_day", nullable = false)
	public LocalTime getEndOfDay() {
		return endOfDay;
	}

	public void setEndOfDay(LocalTime endOfDay) {
		this.endOfDay = endOfDay;
	}

	@ManyToMany
	@JoinTable(name = "EVENTS_SCHEDULES")
	public List<CalendarEvent> getCalendarEvents() {
		return calendarEvents;
	}

	public void setCalendarEvents(List<CalendarEvent> calendarEvents) {
		this.calendarEvents = calendarEvents;
	}

	@Transient
	@ValueRangeProvider(id = "allSlots")
	public List<TimeSlot> getTimeSlots() {
		return timeSlots;
	}

	public void setTimeSlots(List<TimeSlot> timeSlots) {
		this.timeSlots = timeSlots;
	}

	@Transient
	@PlanningEntityCollectionProperty
	public List<Task> getAllTasks() {
		return allTasks;
	}

	public void setAllTasks(List<Task> allTasks) {
		this.allTasks = allTasks;
	}

	@OneToMany(mappedBy = "schedule")
	public List<AssignedTask> getAssignedTasks() {
		return assignedTasks;
	}

	public void setAssignedTasks(List<AssignedTask> assignedTasks) {
		this.assignedTasks = assignedTasks;
	}

	@Transient
	@Override
	public HardSoftScore getScore() {
		return score;
	}

	@Override
	public void setScore(HardSoftScore score) {
		this.score = score;
	}

	private int getHardScore() {
		return hardScore;
	}

	private void setHardScore(int hardScore) {
		this.hardScore = hardScore;
	}

	private int getSoftScore() {
		return softScore;
	}

	private void setSoftScore(int softScore) {
		this.softScore = softScore;
	}

	@PrePersist
	public void preparePersistingScore() {
		setHardScore(score.getHardScore());
		setSoftScore(score.getSoftScore());
	}

	@PostLoad
	public void regenerateHardSoftScore() {
		score = HardSoftScore.valueOf(getHardScore(), getSoftScore());
	}

	@Transient
	@Override
	public Collection<?> getProblemFacts() {
		return null;
	}

	@Override
	public String toString() {
		return "Schedule{" +
				"id=" + id +
				", forDate=" + forDate +
				", startOfDay=" + startOfDay +
				", endOfDay=" + endOfDay +
				", calendarEvents=" + calendarEvents +
				", assignedTasks=" + assignedTasks +
				", timeSlots=" + timeSlots +
				", score=" + score +
				", chosen=" + chosen +
				'}';
	}
}
