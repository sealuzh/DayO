package ch.uzh.ifi.seal.domain_classes;

/**
 * Importance of the tasks. Priority is combined  of the importance and urgency.
 * In this app urgency is represented by the DueDate of the task.
 * Tasks which don't have a due date are not considered urgent.
 */
public enum TaskImportance {
	LOW,
	MEDIUM,
	HIGH
}