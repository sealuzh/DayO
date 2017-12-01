import {Component, ElementRef, Input, ViewChild, ViewEncapsulation} from "@angular/core";
import {scaleTime, ScaleTime} from "d3-scale";
import {timeFormat, timeParse} from "d3-time-format";
import {select} from "d3-selection";
import {axisLeft} from "d3-axis";
import {timeMinute} from "d3-time";
import {CalendarEvent} from "./calendar-event";
import {Task} from "../../common/task";
import {Schedule} from "./schedule";

@Component({
  encapsulation: ViewEncapsulation.None,
  selector: 'schedule',
  template: `
    <!-- relatively positioned container for the schedule items and scale
         absolutely positioned events and tasks -->
    <div class="schedule-container" style="position: relative">
      <svg width="960" height="1100">
        <g #eventContainer transform="translate(40, 8)"></g>
      </svg>
      <ng-container *ngIf="schedule">
        <schedule-item-event *ngFor="let event of schedule.events; let i = index" class="schedule-item"
                             [style.top.px]="eventCoordinates[i].y"
                             [style.height.px]="eventCoordinates[i].height"
                             [event]="event"></schedule-item-event>
        <schedule-item-task *ngFor="let task of schedule.tasks; let i = index" class="schedule-item"
                            [style.top.px]="taskCoordinates[i].y"
                            [style.height.px]="taskCoordinates[i].height"
                            [task]="task"
                            [short]="true"
                            [editable]="editable"></schedule-item-task>
      </ng-container>
    </div>

  `,
  styles: [`
    
    .schedule-item {
      position: absolute;
      /*overflow: hidden;*/
      padding-left: 50px;
      padding-right: 10px;
      width: 100%;
      margin-top: 8px; /*must match svg>g transform translate*/
    }

    .schedule-container {
      overflow: hidden;
    }

    .tick > line {
      stroke: #ccc;
    }

    .major.tick > line {
      stroke: #666;
    }

  `]
})
export class ScheduleComponent {

  private _schedule: Schedule;
  @Input()
  get schedule(): Schedule {return this._schedule;}
  set schedule(value: Schedule) {
    if (!value) return; // don't render undefined schedule
    this._schedule = value;
    this.render();
  }

  @Input()
  editable = true;

  @ViewChild("eventContainer")
  container: ElementRef;

  dayScale: ScaleTime<number, number>;

  render() {
    const svg = select(this.container.nativeElement);
    const from = parseTime(this.schedule.startOfDay);
    const to = parseTime(this.schedule.endOfDay);
    this.dayScale = scaleTime()
      .domain([from, to])
      .range([0,700]);
    const axis = axisLeft(this.dayScale)
      .ticks(timeMinute.every(15))
      .tickFormat(timeFormat("%H:%M "))
      .tickSize(-1500);
    svg
      .call(axis)
      .selectAll(".tick")
      .classed("major", (date: Date) => date.getMinutes() == 0);

    this.calculateCalendarBoxCoordinates(from);
  }

  taskCoordinates: CalendarBoxCoords[];
  eventCoordinates: CalendarBoxCoords[];

  calculateCalendarBoxCoordinates(dayStart: Date) {
    console.log("Getting coordinates for tasks %o and events %o", this._schedule.tasks, this._schedule.events);
    this.taskCoordinates = this.getCoordinates(dayStart, this._schedule.tasks);
    this.eventCoordinates = this.getCoordinates(dayStart, this._schedule.events);
  }

  private getCoordinates(dayStart: Date, source: Array<Task | CalendarEvent>) {
    return source.map(scheduleItem => {
      const baseDatePlusDuration = timeMinute.offset(dayStart, scheduleItem.duration);
      return {
        y: this.dayScale(parseTime(scheduleItem.startingTime)),
        height: this.dayScale(baseDatePlusDuration)
      };
    });
  }
}

const parseTime = timeParse("%H:%M:%S");

// in pixels
interface CalendarBoxCoords {
  y: number
  height: number
}
