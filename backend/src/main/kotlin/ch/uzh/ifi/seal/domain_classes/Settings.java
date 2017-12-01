package ch.uzh.ifi.seal.domain_classes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalTime;

/**
 * User Settings store the information which is not expected to change often, but has a significant
 * influence on the planning of the day schedule.
 * <p>
 * Settings are expected to have a reference to the owner (User) which cannot be null at the persistence time.
 */
@Entity
@Table(name = "settings")
public class Settings {

	private int id;
	private LocalTime startOfDay;
	private LocalTime endOfDay;
	private MorningnessEveningnessType daytimeProductivityType;
	private User owner;

	public Settings() {
	}

	public Settings(LocalTime startOfDay, LocalTime endOfDay) {
		this.startOfDay = startOfDay;
		this.endOfDay = endOfDay;
	}

	public Settings(LocalTime startOfDay, LocalTime endOfDay, MorningnessEveningnessType daytimeProductivityType) {
		this.startOfDay = startOfDay;
		this.endOfDay = endOfDay;
		this.daytimeProductivityType = daytimeProductivityType;
	}

	@OneToOne
	@JsonIgnore
	@JoinColumn(nullable = false)
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
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

	@Column(name = "daytime_prod_type")
	public MorningnessEveningnessType getDaytimeProductivityType() {
		return daytimeProductivityType;
	}

	public void setDaytimeProductivityType(MorningnessEveningnessType daytimeProductivityType) {
		this.daytimeProductivityType = daytimeProductivityType;
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
		return "Settings{" +
				"startOfDay=" + startOfDay +
				", endOfDay=" + endOfDay +
				", daytimeProductivityType=" + daytimeProductivityType +
				'}';
	}
}
