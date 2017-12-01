import {Component} from "@angular/core";
import {Settings} from "./settings";
import {HttpWrapper} from "../common/http-wrapper.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  template: `
    <div class="row justify-content-md-center" style="padding-top: 10%">
      <div class="col-md-auto">
        <form id="settings-form" *ngIf="settings">
          <div class="form-group row">
            <label for="start" class="col-3 col-form-label">Workday starts at:</label>
            <div class="col-8">
              <input class="form-control" type="time" name="start" [(ngModel)]="settings.startOfDay">
            </div>
          </div>
          <div class="form-group row">
            <label for="end" class="col-3 col-form-label">Workday ends at:</label>
            <div class="col-8">
              <input class="form-control" type="time" name="end" [(ngModel)]="settings.endOfDay">
            </div>
          </div>
          <fieldset class="form-group" name="chronotypes">
            <legend>Chronotype</legend>
            <div class="form-check">
              <label class="form-check-label">
                <input type="radio" class="form-check-input" name="optionsRadios" value="MORNING_TYPE"
                       [(ngModel)]="settings.daytimeProductivityType">
                Morning Type &mdash; feel the most productive in the morning
              </label>
            </div>
            <div class="form-check">
              <label class="form-check-label">
                <input type="radio" class="form-check-input" name="optionsRadios" value="NEITHER_TYPE"
                       [(ngModel)]="settings.daytimeProductivityType">
                Neither Type &mdash; don't feel more or less productive depending on the time of the day
              </label>
            </div>
            <div class="form-check">
              <label class="form-check-label">
                <input type="radio" class="form-check-input" name="optionsRadios" value="EVENING_TYPE"
                       [(ngModel)]="settings.daytimeProductivityType">
                Evening Type &mdash; feel the most productive in the evening
              </label>
            </div>
          </fieldset>
          <button type="submit" class="btn btn-primary" [attr.disabled]="canSubmit()" (click)="submit()">Save Settings</button>
        </form>
      </div>
    </div>
  `
})
export class SettingsFormComponent {

  settings: Settings;

  constructor(private httpWrapper: HttpWrapper,
              private router: Router,
              private activeRoute: ActivatedRoute) {
    // getting the current settings when navigated to the settings page
    this.httpWrapper.get('user/settings') // todo: rethink the navigation policy
      .subscribe(res => {
        if (res.text()) {
          console.log("got settings from server");
          this.settings = res.json();
        } else {
          console.log("got NO settings from server");
          this.settings = {} as Settings;
        }
      });
  }

  submit() {
    console.log('Submitting settings to the server ', this.settings);
    this.httpWrapper.put('user/createOrUpdateSettings', this.settings)
      .map(res => {
          console.log(res);
          if (res.status == 200) {
            this.router.navigate(['dashboard'])
          } else {
            throw new Error('This request has failed with status' + res.status);
          }
        }
      ).subscribe(() => this.router.navigate(["/"]));
  }

  canSubmit(): string {
    return (this.settings.startOfDay !== undefined
    && this.settings.endOfDay !== undefined
    && this.settings.daytimeProductivityType !== undefined) ? null : "disabled";
  }
}
