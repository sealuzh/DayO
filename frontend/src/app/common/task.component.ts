import {Component, Input, OnInit} from "@angular/core";
import {Task} from "./task";
import {TasksService} from "../dashboard/right/tasks.service";

@Component({
  selector: 'task',
  template: `
  <ng-template #taskTooltip><span>{{difficultyToStr[task.difficulty]}} task <br> double-click to edit</span></ng-template>
    <div class="task small"
         [style.overflow]="overflow"
         [style.background-color]="difficultyToColor[task.difficulty]"
         [style.border-color]="difficultyToBorderColor[task.difficulty]"
         [ngbTooltip]="(short)? '' : taskTooltip" placement="left">
      <div [style.top.px]="topPadding+1" style="left: 10px">
        <input *ngIf="editable" type="checkbox" class="larger" 
               [checked]="taskChecked"
               (click)="taskChecked = !taskChecked; completeTask()" (mouseover)="checkboxMouseOver()"/>
      </div>
      <div [style.top.px]="topPadding" style="left: 30px" class="description" [style.overflow]="overflow">
        <ng-template #importanceTooltip>{{importanceToStr[task.importance]}}</ng-template>
        <span [ngbTooltip]="importanceTooltip">
          <i *ngFor="let star of importanceToStar[task.importance]" class="fa fa-star-o" style="padding-right: 1px"></i>
        </span>
        {{task.description}}
      </div>
      <ng-container *ngIf="!short">     
        <div [style.top.px]="topPadding-4" style="right: 10px">
          <button *ngIf="editable&&!task.deleted" class="close" (click)="deleteTask()">&#10799;</button>
          <button *ngIf="editable&&task.deleted" class="close" (click)="undoDelete()"><i class="fa fa-undo" aria-hidden="true"></i></button>
        </div>
        <!--<ng-template #tipContent>{{importanceToStr[task.importance]}}</ng-template>-->
        <!--<div style="bottom: 8px; left: 10px" [ngbTooltip]="tipContent" placement="bottom">-->
        <!--<i *ngFor = "let star of importanceToStar[task.importance]" class="fa fa-star-o" style="padding-right: 1px" ></i>-->
        <!--</div>-->
        <!--<div style="bottom: 8px; left: 60px">-->
        <!--{{task.difficulty}}-->
        <!--</div>-->
        <div style="bottom: 8px; right: 10px">
          {{task.dueDate}}
        </div>
      </ng-container>
    </div>
  `,
  styles: [`
    .col-2, .col-8, .col-4 {
      padding-left: 8px;
      padding-right: 8px
    }

    .description {
      max-width: 70%;
      max-height: 80%;
    }

    .task {
      background-color: #ddffdd;
      margin-bottom: 1em;
      border: 1px solid #009926;
      height: 100%;
      position: relative;
      // overflow: hidden;
    }

    .task:hover {
      min-height: 60px;
    }

    .task > div {
      position: absolute;
    }
  `]
})
export class TaskComponent implements OnInit {

  topPadding: number;
  overflow: string;

  @Input()
  task: Task;
  @Input()
  editable = true;

  @Input()
  short = false;
  taskChecked: Boolean;

  constructor(private taskService: TasksService) {
  }

  ngOnInit() {
    this.taskChecked = !!(this.task.completed);
    this.topPadding = (this.short && this.task.duration == 15) ? 0 : 8;
    this.overflow = this.allowOverflow();
  }


  importanceToStar = {
    "LOW": [1],
    "MEDIUM": [1, 2],
    "HIGH": [1, 2, 3]
  };

  importanceToStr = {
    "LOW": "low importance",
    "MEDIUM": "normal importance",
    "HIGH": "high importance"
  };

  difficultyToColor = {
    "EASY": "#ddffdd",
    "REGULAR": "#FFECDD",
    "CHALLENGING": "#FFDDF0"
  };

  difficultyToBorderColor = {
    "EASY": "#9DFF9D",
    "REGULAR": "#FFCA9D",
    "CHALLENGING": "#FF9DD4"
  };

  difficultyToStr = {
    "EASY": "easy",
    "REGULAR": "regular",
    "CHALLENGING": "challenging"
  };

  checkboxMouseOver() {
    if (!this.taskChecked) {
    } // todo show user that it can be checked
  }

  deleteTask() {
    this.taskService.deleteTask(this.task);
  }

  allowOverflow() {
    if (this.short) {
      return "hidden"
    } else {
      return "visible"
    }
  }

  completeTask() {
    if (this.taskChecked) {
      this.taskService.completeTask(this.task);
    } else {
      this.taskService.undoCompleteTask(this.task);
    }
  }

  undoDelete() {
    this.taskService.undoDelete(this.task);
  }
}
