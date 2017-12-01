import {Component, Input, OnInit} from "@angular/core";
import {CalendarEvent} from "./calendar-event";

@Component({
  selector: 'schedule-item-event',
  template: `
    <div class="event-item" [style.padding-top.px]="topPadding">
      {{event.description}}
    </div>

  `,
  styles: [`
    .event-item {
      height: 100%;
      background-color: rgba(192,192,192, 0.8);
      padding-left: 10px;
      font-size: small;
    }    
  `]
})
export class ScheduleItemEventComponent implements OnInit {
  @Input()
  event: CalendarEvent;

  topPadding: number;

  ngOnInit() {
    this.topPadding = (this.event.duration == 15) ? 0 : 8;
  }
}
