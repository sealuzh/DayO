import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import {AppComponent} from "./app.component";
import {LoginComponent} from "./login.component";
import {AppRoutingModule} from "./app-routing.module";
import {FormsModule} from "@angular/forms";
import {HttpWrapper} from "./common/http-wrapper.service";
import {HttpModule} from "@angular/http";
import {DailyFormComponent} from "./dashboard/left/daily-form.component";
import {ScheduleComponent} from "./dashboard/left/schedule.component";
import {SettingsFormComponent} from "./settings/settings-form.component";
import {TaskControlPanelComponent} from "./dashboard/right/task-control-panel.component";
import {TaskListComponent} from "./dashboard/right/task-list.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {TaskComponent} from "./common/task.component";
import {ScheduleItemTaskComponent} from "./dashboard/left/schedule-item-task.component";
import {ScheduleItemEventComponent} from "./dashboard/left/schedule-item-event.component";
import {EditableTaskComponent} from "./dashboard/right/editable-task.component";
import {AuthenticationGuard} from "./common/authentication-guard.service";
import {TasksService} from "./dashboard/right/tasks.service";
import {TaskFormComponent} from "./dashboard/right/task-form.component";
import {ChooseScheduleComponent} from "./dashboard/left/choose-schedule.component";
import {WaitingComponent} from "./dashboard/waiting.component";


@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    DailyFormComponent,
    ScheduleComponent,
    SettingsFormComponent,
    TaskControlPanelComponent,
    TaskListComponent,
    DashboardComponent,
    TaskComponent,
    EditableTaskComponent,
    TaskFormComponent,
    ScheduleItemTaskComponent,
    ScheduleItemEventComponent,
    ChooseScheduleComponent,
    WaitingComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpModule,
    NgbModule.forRoot()
  ],
  providers: [
    HttpWrapper,
    AuthenticationGuard,
    TasksService
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule {
}
