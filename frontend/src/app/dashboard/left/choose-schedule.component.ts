import {Component} from "@angular/core";
import {Schedule} from "./schedule";
import {HttpWrapper} from "../../common/http-wrapper.service";
import {Response} from "@angular/http";
import {Router} from "@angular/router";

@Component({
  template: `
    <div class="row">
      <div class="col-12">
        <h1 style="margin-bottom: 30px; margin-top: 30px">Please choose the schedule for today:</h1>
      </div>
      <div class="col-4" *ngFor="let schedule of scheduleOptions, let i = index">
        <h4>Option {{i+1}}</h4>
        <!--<h4>Score: <span>{{schedule.hardScore}}</span> / <span>{{schedule.softScore}}</span></h4>-->
        <schedule [schedule]="schedule" [editable]="false" (click)="selectSchedule(schedule.id)"></schedule>
      </div>
    </div>
  `,
  styles: [`
    schedule:hover {
      display: block;
      cursor: pointer;
      background-color: rgba(92, 170, 126, 0.4);
    }
  `]
})
export class ChooseScheduleComponent {
  scheduleOptions: Schedule[];

  selectSchedule(id: number) {
    this.http.post(`user/schedule/${id}/select`, null).subscribe(() => this.router.navigate(["/"]));
  }

  constructor(private http: HttpWrapper, private router: Router){
    http.get("user/schedule/options").subscribe((res: Response) =>
      this.scheduleOptions = res.json() as Schedule[]);
  }
}
