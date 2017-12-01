import {Component, EventEmitter, Input, Output} from "@angular/core";
import {Task, TaskDifficulty, TaskImportance} from "../../common/task";
import {TasksService} from "./tasks.service";
import {timeFormat, timeParse} from "d3-time-format";

let datePattern = "%Y-%m-%d";

const dateFormat = timeFormat(datePattern);
const dateParse = timeParse(datePattern);

@Component({
  selector: 'task-form',
  styles: [`
    /*.task-form {*/
      /*padding: 1em;*/
      /*margin-bottom: 1em;*/
    /*}*/
    .close-margin {
      margin-top: 10px;
      margin-right: 10px;
    }
  `],
  template: `
    <div class="card card-outline-success" style="margin-bottom: 10px">
      <div class="card-img-top">
        <button class="close close-margin" (click)="finishAddingTask()">&#10006;</button>
      </div>
      <div class="card-block">
        <div class="task-form">
          <form (submit)="submitForm()">
            <div class="form-group" style="margin-top: -10px">
              <textarea class="form-control" [(ngModel)]="task.description" placeholder="Description" name="description"
                        required></textarea>
            </div>
            <div class="form-group">
              <label>Estimated Duration <small class="form-text text-muted pull-right" style="padding-left: 5px"><span *ngIf="task.duration">{{task.duration / 60 | number:'1.1-2'}} hours </span></small></label>
              <div class="input-group">
                <input type="number" class="form-control" step="15" min="15" [(ngModel)]="task.duration"
                       (change)="validateDuration()"
                       name="duration">
                <span class="input-group-addon">minutes</span>
              </div>              
            </div>
            <div class="form-group">
              <div class="form-check" *ngFor="let diff of difficulties">
                <label class="form-check-label"><input type="radio" [(ngModel)]="task.difficulty" [value]="diff" name="difficulty">
                  {{difficultyToString[diff]}}
                </label>
              </div>
            </div>
            <hr>
            <div class="form-group">
              <div class="form-check" *ngFor="let importance of importanceList">
                <label class="form-check-label">
                  <input type="radio" [(ngModel)]="task.importance" [value]="importance"
                         name="importance">
                  {{importanceToStr[importance]}}
                </label>
              </div>
            </div>
            <div class="form-group" style="margin-top: 15px">
              <label>Due Date <i class="text-muted">(optional)</i></label>
              <input class="form-control" [min]="today" type="date" name="dueDate" [(ngModel)]="task.dueDate"
                     (change)="validateDueDate()">
            </div>
            <div class="text-right">
              <input type="submit" class="btn btn-primary " [attr.disabled]="canSubmitTask()">
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export class TaskFormComponent {
  difficulties: TaskDifficulty[] = ["EASY", "REGULAR", "CHALLENGING"];
  importanceList: TaskImportance[] = ["LOW", "MEDIUM", "HIGH"];
  today = dateFormat(new Date());

  importanceToStr = {
    "LOW": "Not important",
    "MEDIUM": "Normal",
    "HIGH": "Very important"
  };

  difficultyToString = {
    "EASY": "Easy task",
    "REGULAR": "Regular task",
    "CHALLENGING": "Challenging task"
  };

  @Input()
  task: Task = {} as Task;

  @Output()
  taskEdited = new EventEmitter<null>();

  constructor(private taskService: TasksService) {
  }
  finishAddingTask(){
    this.taskEdited.next(null);
    this.taskService.finishAddingTask();
  }

  submitForm() {
    if (this.task.id) {
      this.taskService.updateTask(this.task);
      this.taskEdited.next(null);
    } else {
      this.taskService.submitNewTask(this.task);
      this.taskService.finishAddingTask();
      this.task = {} as Task;
    }
  }

  canSubmitTask() {
    return ((this.task.description == null) ||
      (this.task.duration == null) ||
      (this.task.difficulty == null) ||
      (this.task.importance == null)) ? "disabled" : null;
  }

  validateDuration() {
    if (this.task.duration) {
      this.task.duration = Math.ceil(this.task.duration / 15) * 15;
    }
  }

  validateDueDate() {
    if (this.task.dueDate) {
      const due = dateParse(this.task.dueDate);
      const today = dateParse(this.today);
      this.task.dueDate = dateFormat(due < today ? today: due);
    }
  }
}
