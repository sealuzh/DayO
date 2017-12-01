import {Component} from "@angular/core";
import {FilterState, TasksService} from "./tasks.service";

@Component({
  selector: 'task-control-panel',
  template: `
    <button class="btn btn-secondary btn-block" (click)="addNewTask()">Add New Task</button>
    <div class="row" style="padding-top: 10px;">
      <label class="label col col-md-auto">Show: </label>
      <ng-container *ngFor="let state of filterStates">
        <div class="col col-md-auto">
          <div class="form-check" style="white-space: nowrap">
            <label class="form-check-label">
              <input type="radio" class="form-check-input" name="optionsRadios" [value]="state"
                     [(ngModel)]="filterState" required> {{state}}
            </label>
          </div>
        </div>
      </ng-container>
    </div>
  `,
  styles: [`
    .col, .col-md-auto{
      padding-left: 15px;
      padding-right: 0px;
    }
  `]
})
export class TaskControlPanelComponent {
  private _filterState: FilterState = "Active";
  get filterState(): FilterState {
    return this._filterState;
  }

  set filterState(value: FilterState) {
    this._filterState = value;
    this.taskService.taskFilterState.next(value);
  }

  filterStates: FilterState[] = ["Active", "Completed", "Deleted"];

 constructor (private taskService: TasksService){}

  addNewTask() {
    console.log("want to add new task");
    this.taskService.addNewTask();
  }
}
