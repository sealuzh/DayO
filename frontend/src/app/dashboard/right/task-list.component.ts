import {Component, OnDestroy} from "@angular/core";
import {Task} from "../../common/task";
import {FilterState, TasksService} from "./tasks.service";
import {Subscription} from "rxjs/Subscription";

@Component({
  selector: 'task-list',
  template: `
    <editable-task *ngIf="addingNewTask" [task]="" [isEditable]="true" [isEditingNow]="true"></editable-task>
    <editable-task [class.fixed-height]="!tasksBeingEdited[i]" class="task-display" 
                   (editing)="startEditingTask(i, $event)" 
                   *ngFor="let task of filteredTasks; let i = index" [task]="task" [isEditable]="true"
                   [isEditingNow]="tasksBeingEdited[i]"></editable-task>
  `,
  styles: [`
.task-display {
    display: block;
    margin-bottom: 15px;
}
  `]
})
export class TaskListComponent implements OnDestroy{
  ngOnDestroy(): void {
    this.subscriptions.forEach(it => it.unsubscribe());
  }

  startEditingTask(index: number, isEditing: boolean) {
    this.setTasksBeingEditedToAllFalse();
    setTimeout(() => this.tasksBeingEdited[index] = isEditing);
  }

  setTasksBeingEditedToAllFalse() {
    for (let i = 0; i < this.filteredTasks.length; i++) {
      this.tasksBeingEdited[i] = false;
    }
  }

  originalTasks: Task[] = [];
  filteredTasks: Task[] = [];
  tasksBeingEdited: Boolean[] = [];

  filterState: FilterState = "Active";
  addingNewTask: Boolean = false;

  subscriptions: Subscription[];

  constructor(private taskService: TasksService) {
    this.getTasks();
    this.subscriptions = [
      taskService.getNewTaskEvent().subscribe((b: Boolean) => this.addingNewTask = b ),
      taskService.getTaskChangedObservable().subscribe(() => this.getTasks()),
      taskService.taskFilterState.subscribe((filterState) => {
        this.filterState = filterState;
        this.filterTasks();
      })
    ];
  }

  filterTasks() {
    this.filteredTasks = this.originalTasks.filter(task => {
      return (this.filterState == "Deleted" && task.deleted) ? true
        : (this.filterState == "Completed" && task.completed) ? true
        : (this.filterState == "Active" && !task.completed && !task.deleted);
    });
    this.setTasksBeingEditedToAllFalse();
  }

  getTasks() {
    console.log("Call to service to get originalTasks");
    this.taskService.getAllTasks()
      .then((originalTasks: Task[]) => {
        console.log("received originalTasks ", originalTasks);
        this.originalTasks = originalTasks;
        this.filterTasks();
        console.log("saved originalTasks", this.originalTasks);
      })
  }
}
