import {Component, Input} from "@angular/core";
import {Task} from "../../common/task";

@Component({
  selector: 'schedule-item-task',
  template: `
    <task [task]="task" [short]="true" [editable]="editable"></task>
  `
})
export class ScheduleItemTaskComponent {
  @Input() task: Task;
  @Input() editable = true;
  @Input() short = false;
}
