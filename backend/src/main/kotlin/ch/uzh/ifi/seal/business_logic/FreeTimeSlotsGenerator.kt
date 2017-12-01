package ch.uzh.ifi.seal.business_logic

import ch.uzh.ifi.seal.ConfigurationConstants
import ch.uzh.ifi.seal.domain_classes.CalendarEvent
import ch.uzh.ifi.seal.domain_classes.Settings
import ch.uzh.ifi.seal.domain_classes.TimeSlot
import java.time.Duration
import java.time.LocalTime
import java.util.*

/**
 * Function produces the list of TimeSlots which are not overlapping with the existing events and fitting into the day.
 * @param settings of the user providing information about start and end of the day
 * @param fixedAppointments events for the day of interest imported from the user's calendar
 * @return list of free TimeSlots in the day
 */
fun generateFreeTimeSlots(settings: Settings, fixedAppointments: List<CalendarEvent>): MutableList<TimeSlot> {
    val freeTimeSlots = ArrayList<TimeSlot>()
    val duration = Duration.between(settings.startOfDay, settings.endOfDay).toMinutes()
    val numberOfSlotsInDay = duration / ConfigurationConstants.TIME_SLOT_DURATION

    var curTime = settings.startOfDay
    var slotId = 0
    for (i in 0..(numberOfSlotsInDay - 1)) {
        if (!collision(curTime, fixedAppointments)) freeTimeSlots.add(TimeSlot(curTime, slotId++))
        curTime = curTime.plusMinutes(ConfigurationConstants.TIME_SLOT_DURATION.toLong())
    }
    return freeTimeSlots
}

private fun collision(curTime: LocalTime, fixedAppointments: List<CalendarEvent>): Boolean {

    if (fixedAppointments.isEmpty()) return false

    val sortedEvents = fixedAppointments.sorted()
    val beforeFirstEvent = curTime.isBefore(sortedEvents[0].start)
    val afterLastEventFinished = curTime.isAfter(sortedEvents[sortedEvents.size - 1].end)

    if (beforeFirstEvent || afterLastEventFinished) {
        return false
    } else {
        for (event in sortedEvents) {
            if (curTime.isBefore(event.start))
                return false
            if (curTime == event.start)
                return true
            if (curTime.isAfter(event.start) && curTime.isBefore(event.end))
                return true
        }
        return false
    }
}