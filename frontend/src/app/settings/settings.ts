export interface Settings {
  startOfDay: string
  endOfDay: string
  daytimeProductivityType : MorningnessEveningnessType
}

export type MorningnessEveningnessType = "MORNING_TYPE" | "NEITHER_TYPE" | "EVENING_TYPE"
