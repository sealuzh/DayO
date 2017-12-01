import {Component} from "@angular/core";
import {HttpWrapper} from "../../common/http-wrapper.service";
import {DailyForm} from "./daily-form";
import "rxjs/add/operator/map";
import "rxjs/add/operator/toPromise";
import {Router} from "@angular/router";

@Component({
  selector: 'daily-form',
  template: `
<div class="card card-outline-warning">

      <div class="card-block">
        <h4 class="card-title" style="margin-bottom: 25px">Please, fill in the daily form</h4>
      <form>
    <div class="form-group">
      <label for="duration">How long did you sleep?</label>
      <small *ngIf="dailyForm.sleepDuration" class="form-text text-muted pull-right">{{dailyForm.sleepDuration}} hours</small>
      <input id="slider" class="form-control" name="duration" [(ngModel)]="dailyForm.sleepDuration" type="range"
             min="0" max="10" step="0.5">
      <!--<input id="slider" class="slider" name="duration" -->
      <!--[(ngModel)]="dailyForm.sleepDuration" -->
            <!--type="text"-->
            <!--data-provide="slider"-->
            <!--data-slider-id="duration"-->
            <!--data-slider-min="1"-->
            <!--data-slider-max="10"-->
            <!--data-slider-step="0.5"-->
            <!--data-slider-value="3"-->
            <!--data-slider-tooltip="show"-->
            <!--value="0">-->
      <!--<label>{{dailyForm.sleepDuration}}</label>-->
    </div>
    <fieldset class="form-group" name="quality">
      <label for="quality">How well did you sleep?</label>
      <div class="form-check">
        <label class="form-check-label">
          <input type="radio" class="form-check-input" name="optionsRadios" value="GOOD"
                 [(ngModel)]="dailyForm.sleepQuality" required>
          Good <small class="form-text text-muted">didn't wake up, feel rested and satisfied</small>
        </label>
      </div>
      <div class="form-check">
        <label class="form-check-label">
          <input type="radio" class="form-check-input" name="optionsSleep" value="NORMAL"
                 [(ngModel)]="dailyForm.sleepQuality" required>
          Ok <small class="form-text text-muted">didn't wake up, feel as usual</small>
        </label>
      </div>
      <div class="form-check">
        <label class="form-check-label">
          <input type="radio" class="form-check-input" name="optionsSleep" value="BAD"
                 [(ngModel)]="dailyForm.sleepQuality" required>
          Bad <small class="form-text text-muted">had disturbed sleep, feel less rested than usual</small>
        </label>
      </div>
      <div class="form-check">
        <label class="form-check-label">
          <input type="radio" class="form-check-input" name="optionsSleep" value="VERY_BAD"
                 [(ngModel)]="dailyForm.sleepQuality" required>
          Very bad <small class="form-text text-muted">had very disturbed sleep, don't feel rested</small>
        </label>
      </div>
    </fieldset>
    <fieldset class="form-group">
      <label for="stress">How stressed do you feel today?</label>
        <ul class="likert" style="margin-left: -20px">
          <li>Not at all</li>
          <li><input type="radio" name="stress-level" value="INSIGNIFICANT" [(ngModel)]="dailyForm.stressLevel"/></li>
          <li><input type="radio" name="stress-level" value="USUAL" [(ngModel)]="dailyForm.stressLevel"/></li>
          <li><input type="radio" name="stress-level" value="HIGHER" [(ngModel)]="dailyForm.stressLevel"/></li>
          <li><input type="radio" name="stress-level" value="VERY_HIGH" [(ngModel)]="dailyForm.stressLevel"/></li>
          <li><input type="radio" name="stress-level" value="TOO_HIGH" [(ngModel)]="dailyForm.stressLevel"/></li>
          <li>Very stressed</li>
        </ul>          
    </fieldset>
  </form>
  <div class=" text-right">
    <button type="submit" [attr.disabled]="canNotSubmit()" class="btn btn-primary" (click)="submit()">Submit Daily Form</button>
  </div>
</div>
  
  </div>
  `,
  styles: [`
    .likert>li {
      float: left;
      padding-right: 6px;
      list-style-type: none;
    }
    .likert{
      display: block;
    }
  `]
})
export class DailyFormComponent {
  dailyForm: DailyForm;

  constructor(private httpWrapper: HttpWrapper, private router: Router) {
    this.dailyForm = {sleepDuration: 8.0} as DailyForm;
  }

  submit() {
    if (this.canNotSubmit() == "disabled") return;
    this.httpWrapper.post('dailyForms/add', this.dailyForm).subscribe(() => {});
    this.router.navigate(["waiting"])
  }

  canNotSubmit(): string {
    return (this.dailyForm.sleepDuration !== undefined
      && this.dailyForm.sleepQuality !== undefined
      && this.dailyForm.stressLevel !== undefined) ? null : "disabled";
  }
}
