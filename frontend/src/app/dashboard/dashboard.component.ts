import {Component} from "@angular/core";
import {DailyForm} from "./left/daily-form";
import {ActivatedRoute, Router} from "@angular/router";
import {Schedule} from "./left/schedule";
import {HttpWrapper} from "../common/http-wrapper.service";

@Component({
  template: `
  <div class="row">
      <!-- left column -->
      <div class="col-6" [ngSwitch]="state">
        <ng-container *ngSwitchCase="'SCHEDULE_CHOSEN'" >
            <h4 style="padding-left: 11px">Schedule for {{today | date:'EEEE, d MMMM'}}</h4>   
            <schedule [schedule]="schedule"></schedule>
        </ng-container>
        <daily-form *ngSwitchCase="'ABSENT_DAILY_FORM'"></daily-form>
        <ng-container *ngSwitchCase="'NO_TASKS'">
          <h4>Please add some tasks to your task list</h4>
          <editable-task  [isEditable]="true" [isEditingNow]="true"></editable-task>
          <button class="btn btn-primary btn-block" style="margin-top: 5px" (click)="doneWithTasks()">I'm done entering tasks</button>
        </ng-container>
      </div>
      
      <!-- right column -->
      <div class="col-5 offset-1">
        <div *ngIf="state != 'NO_TASKS'">
          <task-control-panel></task-control-panel>
        </div>
        <div>
          <task-list></task-list>
        </div>
      </div>
  </div>
  `
})
export class DashboardComponent {
  dailyForm: DailyForm = sampleDailyForm;
  state: DashboardState;
  today = new Date();
  schedule: Schedule;

  constructor(activatedRoute: ActivatedRoute, private router: Router, http: HttpWrapper) {
    activatedRoute.params.subscribe(p => {
      this.state = p['state'];
      if (this.state == "SCHEDULE_CHOSEN") {
        http.get("user/schedule/chosen").subscribe((res) => this.schedule = res.json() as Schedule);
      }
    });
  }

  doneWithTasks() {
    this.router.navigate(['/'])
  }
}


const sampleDailyForm = {} as DailyForm;

type DashboardState = "NO_TASKS" | "SCHEDULE_CHOSEN" | "ABSENT_DAILY_FORM";
