package ch.uzh.ifi.seal.domain_classes;

/**
 * Describes the types of productivity based on the common definitions of the morningness-eveningness research as
 * - morning - productive in the first half of the day
 * - neither - not more or less productive during different times of day
 * - evening - more productive during second half of the day
 * <p>
 * Based on this types which user will have the MorningnessEveningnessRule will calculate its score
 */
public enum MorningnessEveningnessType {
	MORNING_TYPE,
	NEITHER_TYPE,
	EVENING_TYPE
}
