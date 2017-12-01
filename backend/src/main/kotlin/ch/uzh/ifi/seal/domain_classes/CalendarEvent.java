package ch.uzh.ifi.seal.domain_classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the event in the calendar. Events will normally be imported from the user's calendar.
 * The id will be taken from the imported event and used later to merge the events when importing again.
 * All events are stored in the database with the reference to the user who owns them, owner.
 * <p>
 * Sometimes events will be created as part of the user settings, as for instance the daily lunch time.
 */
@Entity
@Table(name = "event")
public class CalendarEvent implements Comparable<CalendarEvent> {

	private String id;
	private LocalDate date;
	private LocalTime start;
	private String description;
	private LocalTime end;
	private List<Schedule> schedules = new ArrayList<>();
	private User owner;

	public CalendarEvent() {
	}

	public CalendarEvent(String id, LocalDate date, LocalTime start, String description, LocalTime end, User owner) {
		this.id = id;
		this.date = date;
		this.start = start;
		this.description = description;
		this.end = end;
		this.owner = owner;
	}

	// used for testing when events not persisted
	public CalendarEvent(LocalDate date, LocalTime start, LocalTime end, String description) {
		this.date = date;
		this.start = start;
		this.description = description;
		this.end = end;
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

	@ManyToMany(mappedBy = "calendarEvents")
	@JsonIgnore
	public List<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

	@Id
	@Column(nullable = false)
	public String getId() {
		return id;
	}

	public void setId(String id) {

		this.id = id;
	}

	@Column(nullable = false)
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	@Column(nullable = false)
	public LocalTime getStart() {
		return start;
	}

	public void setStart(LocalTime start) {
		this.start = start;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(nullable = false, name = "end_time")
	public LocalTime getEnd() {
		return end;
	}

	public void setEnd(LocalTime end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return "CalendarEvent " + description +
				" for date " + date +
				" starts at " + start +
				" ends at " + end + '\n';
	}

	@Override
	public int compareTo(@NotNull CalendarEvent e) {
		return start.compareTo(e.getStart());
	}
}