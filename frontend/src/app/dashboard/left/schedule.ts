import {Task} from "../../common/task";
import {CalendarEvent} from "./calendar-event";

export interface Schedule {
  id: number
  startOfDay: string
  endOfDay: string
  events: CalendarEvent[]
  tasks: Task[]
  hardScore: number
  softScore: number
}
