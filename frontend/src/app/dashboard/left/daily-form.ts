export interface DailyForm {
  sleepDuration: number
  sleepQuality: SleepQuality
  stressLevel: StressLevel
}

export type SleepQuality = "GOOD" | "NORMAL" | "BAD" | "VERY_BAD"
export type StressLevel = "INSIGNIFICANT" | "USUAL" | "HIGHER" | "VERY_HIGH" | "TOO_HIGH"
