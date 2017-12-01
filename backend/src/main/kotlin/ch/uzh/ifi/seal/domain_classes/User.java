package ch.uzh.ifi.seal.domain_classes;


import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The User class is saved in the database and represents the user of the application and
 * references all relevant information about user.
 * Necessary info to initialize User is name and email. The id will be automatically generated
 * once user is persisted into the database.
 * <p>
 * Information saved about user includes:
 * - name
 * - email
 * - all internalEvents which have been imported from the user calendar (@OneToMany)
 * - daily forms that user has filled in (@OneToMany)
 * - all tasks user currently has (@OneToMany)
 * - one settings instance which saves the additional information (@OneToOne)
 * - all schedules which were generated for the user by the system (@OneToMany)
 * <p>
 * All the classes referenced by the user also hold the reference to the user in field "owner", so it is possible
 * to retrieve user from Settings, Task, Schedule, DailyFormInfo, CalendarEvent.
 * <p>
 * Additional constructors included for convenience purposes.
 */
@Entity
@Table(name = "app_user")
public class User {

	// generated value
	private int id;
	// not null values in the beginning
	private String name;
	private String email;
	// values which can be set up later, but all hold a reference to the user, which cannot be null
	private Settings settings;
	private List<Task> tasks = new ArrayList<>();
	private List<DailyFormInfo> dailyForms = new ArrayList<>();
	private List<Schedule> schedules = new ArrayList<>();

	public User() {
	}

	public User(String name) {
		this.name = name;
	}

	public User(String name, String email) {
		this.name = name;
		this.email = email;
	}

	@OneToMany(mappedBy = "owner")
	public List<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

	@OneToMany(mappedBy = "owner")
	public List<DailyFormInfo> getDailyForms() {
		return dailyForms;
	}

	public void setDailyForms(List<DailyFormInfo> dailyForms) {
		this.dailyForms = dailyForms;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "email", nullable = false)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@OneToMany(mappedBy = "owner")
	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	@OneToOne
	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		User user = (User) o;

		if (id != user.id) return false;
		if (name != null ? !name.equals(user.name) : user.name != null) return false;
		return email != null ? email.equals(user.email) : user.email == null;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (email != null ? email.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.add("email", email)
				.add("tasks", tasks)
				.add("settings", settings)
				.add("dailyForms", dailyForms)
				.toString();
	}
}
