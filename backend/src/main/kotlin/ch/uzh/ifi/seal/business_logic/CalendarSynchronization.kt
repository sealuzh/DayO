package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.APPLICATION_NAME
import ch.uzh.ifi.seal.domain_classes.CalendarEvent
import ch.uzh.ifi.seal.domain_classes.User
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import mu.KLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


/**
 * Calendar synchronization for getting user's events from Google calendar.
 * Credentials and authentication for the Google server are handled separately with the servlets.
 */
@Component
class CalendarSynchronization(private val flow: AuthorizationCodeFlow,
                              private val jsonFactory: JsonFactory,
                              private val httpTransport: HttpTransport) {

    companion object : KLogging()

    private fun getCalendarService(email: String): com.google.api.services.calendar.Calendar {
        val credential = flow.loadCredential(email)
        return com.google.api.services.calendar.Calendar.Builder(
                httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }

    fun getCalendarEvents(user: User): List<CalendarEvent> {
        val credential = flow.loadCredential(user.email)
        val service = getCalendarService(user.email)
        val now = DateTime(System.currentTimeMillis())
        val events = service.events().list("primary")
                .setMaxResults(15)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setOauthToken(credential.accessToken)
                .execute()

        val items = events.items

        return transformGoogleEventsToInternalEvents(items, user)
    }

    private fun transformGoogleEventsToInternalEvents(items: List<Event>, user: User): List<CalendarEvent> {
        val calendarEvents: MutableList<CalendarEvent> = mutableListOf()

        if (items.isEmpty()) run {
            logger.info("No upcoming events found.")
        } else {

            logger.info("Upcoming events:" + items)
            for (event in items) {
                val startEvent = event.start.dateTime
                val endEvent = event.end.dateTime
                // not interested in events for the whole day or longer than one day
                if (startEvent == null || startEvent.isDateOnly || endEvent == null) {
                    continue
                } else {
                    val startingDateTime: LocalDateTime = transformToLocalDateTime(startEvent)
                    val date = startingDateTime.toLocalDate()
                    val endDateTime = transformToLocalDateTime(endEvent)
                    val e = CalendarEvent(event.id, date, startingDateTime.toLocalTime(),
                            event.summary, endDateTime.toLocalTime(), user)
                    calendarEvents.add(e)
                }
            }
        }
        return calendarEvents
    }

    private fun transformToLocalDateTime(dateTime: DateTime): LocalDateTime {
        val zone = ZoneId.of("Europe/Berlin") //todo Zone is hardcoded

        /** date/time value expressed as the number of milliseconds since the Unix epoch */
        val epochMilli = dateTime.value
        val instant = Instant.ofEpochMilli(epochMilli)
        val localDateTime = LocalDateTime.ofInstant(instant, zone)

        return localDateTime
    }
}