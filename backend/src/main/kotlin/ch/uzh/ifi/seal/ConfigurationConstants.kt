package ch.uzh.ifi.seal

import ch.uzh.ifi.seal.domain_classes.SleepQuality
import ch.uzh.ifi.seal.domain_classes.SleepQuality.*
import ch.uzh.ifi.seal.domain_classes.StressLevel
import ch.uzh.ifi.seal.domain_classes.StressLevel.*
import ch.uzh.ifi.seal.domain_classes.TaskDifficulty
import ch.uzh.ifi.seal.domain_classes.TaskDifficulty.*
import ch.uzh.ifi.seal.domain_classes.TaskImportance
import ch.uzh.ifi.seal.domain_classes.TaskImportance.*
import java.time.DayOfWeek.*

object ConfigurationConstants {
    const val TIME_SLOT_DURATION = 15 // in minutes
    const val IDEAL_SLEEP_DURATION = 8 // in hours
    const val SLEEP_TOLERANCE_ZONE = 0.5 // in hours

    // denotes the MAX and MIN value the score calculated by the Rule will take
    // 100 means that scores will span the interval of -100..100
    const val SCORE_SPAN = 100 // if the span changes, need to adjust the TaskUrgencyRule!
}

val MIN_TASK_PARTITION_DURATION = 30 // in minutes

/* change this URLs when the server or client location changes */
val SERVER_URL = "http://localhost:8080/"
val CLIENT_URL = "http://localhost:4200/"

/*
*  For testing purposes users are created by inserting names and emails to database when server is started
*  by calling function populateUsersIfNeeded() from PrecreatedUsers (will be automatically executed).
*  Passwords for users are set statically in DayOPrototypeApplication.kt
*
*  New users can be added here by adding new UserDefinition to this list.
*  The new users will be inserted after server is restarted.
*  !! make sure that login (the name of the user) is unique
*/
val users: List<UserDefinition> = listOf(
        UserDefinition("Lada", "pass5", "lada11lada@gmail.com"),
        UserDefinition("Andre", "pass4", "casaout00@gmail.com"),
        UserDefinition("Jurgen", "pass6", "citostyle@gmail.com")
)
val APPLICATION_NAME = "day-o-prototype"

val WORKING_DAYS = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

/** Default weights for the rules */
val TIREDNESS_RULE_DEFAULT_WEIGHT = 0.2
val TASK_URGENCY_RULE_DEFAULT_WEIGHT = 0.2
val TASK_IMPORTANCE_RULE_DEFAULT_WEIGHT = 0.2
val ME_RULE_DEFAULT_WEIGHT = 0.2
val STRESS_RULE_DEFAULT_WEIGHT = 0.2


val TaskImportance.score
    get() = when (this) {
        LOW -> 1
        MEDIUM -> 2
        HIGH -> 3
    }

val SleepQuality.score
    get() = when (this) {
        GOOD -> 2
        NORMAL -> 1
        BAD -> -2
        VERY_BAD -> -3

    }

val TaskDifficulty.score
    get() = when (this) {
        EASY -> 1
        REGULAR -> 2
        CHALLENGING -> 3
    }

val StressLevel.score
    get() = when (this) {
        HIGHER -> -1
        VERY_HIGH -> -2
        TOO_HIGH -> -3
        else -> 0
    }

fun getSleepDurationScore(sleepDuration: Double): Double {
    val lower = ConfigurationConstants.IDEAL_SLEEP_DURATION - ConfigurationConstants.SLEEP_TOLERANCE_ZONE
    val upper = ConfigurationConstants.IDEAL_SLEEP_DURATION + ConfigurationConstants.SLEEP_TOLERANCE_ZONE

    // tolerance zone -> no effects of the duration
    if ((sleepDuration >= lower) && (sleepDuration <= upper)) {
        return 1.0
    } else if (sleepDuration < lower) {
        return sleepDuration - ConfigurationConstants.IDEAL_SLEEP_DURATION
    } else {
        return ConfigurationConstants.IDEAL_SLEEP_DURATION - sleepDuration
    }
}

class UserDefinition(val login: String, val password: String, val email: String)