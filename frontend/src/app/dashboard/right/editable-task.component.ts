import {Component, EventEmitter, Input, Output} from "@angular/core";
import {Task} from "../../common/task";

@Component({
  selector: 'editable-task',
  template: `
  <ng-container *ngIf="!isEditingNow; else editableTask">
    <task style="height: 100px; display: block" [task]="task" (dblclick)="editIfAllowed()" [editable]="isEditable"></task>
    <!--<button (click)="isEditable = true">Edit</button>-->
  </ng-container>
  <ng-template #editableTask>
    <task-form [task]="task" (taskEdited)="isEditingNow = false"></task-form>
  </ng-template>  
  `
})
export class EditableTaskComponent {
  @Input()
  task: Task = {} as Task;
  @Input()
  isEditable: Boolean;

  private _isEditingNow = false;
  @Input()
  get isEditingNow(): boolean {return this._isEditingNow;}
  set isEditingNow(value: boolean) {
    if (value !== undefined && value != this._isEditingNow) {
      this._isEditingNow = value;
      this.editing.next(value);
    }
  }

  editIfAllowed() {
    if (this.isEditable) {
      this.isEditingNow = true;
    }
  }

  @Output()
  editing = new EventEmitter<boolean>();
}
