package ch.uzh.ifi.seal.domain_classes

import java.util.*

class TaskDurationComparator : Comparator<Task> {
    override fun compare(o1: Task, o2: Task): Int = o1.duration.compareTo(o2.duration)
}

