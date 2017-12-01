import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {SettingsFormComponent} from "./settings/settings-form.component";
import {AuthenticationGuard} from "./common/authentication-guard.service";
import {WaitingComponent} from "./dashboard/waiting.component";
import {DailyFormComponent} from "./dashboard/left/daily-form.component";
import {ChooseScheduleComponent} from "./dashboard/left/choose-schedule.component";

const appRoutes: Routes = [
  {path: '', redirectTo: 'login', pathMatch: 'full'},
  {path: 'login', canActivate: [AuthenticationGuard], children: []},
  {path: 'dashboard/:state', component: DashboardComponent},
  {path: 'settings', component: SettingsFormComponent},
  {path: 'dailyForm', component: DailyFormComponent},
  {path: 'chooseSchedule', component: ChooseScheduleComponent},
  {path: 'waiting', component: WaitingComponent}
];

@NgModule({
  imports: [ RouterModule.forRoot(appRoutes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {
}
