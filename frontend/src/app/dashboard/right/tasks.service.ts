import {Injectable} from "@angular/core";
import {HttpWrapper} from "../../common/http-wrapper.service";
import {Task} from "app/common/task";
import {Subject} from "rxjs/Subject";
import {Observable} from "rxjs/Observable";
import {ReplaySubject} from "rxjs/ReplaySubject";

@Injectable()
export class TasksService {

  private newTaskSubject = new Subject<Boolean>();
  private taskListChanged = new Subject<any>();
  taskFilterState = new ReplaySubject<FilterState>(1);
  constructor(private http: HttpWrapper) {}

  addNewTask() {
    this.newTaskSubject.next(true);
  }

  finishAddingTask() {
    this.newTaskSubject.next(false);
    this.taskListChanged.next();
  }

  notifyTaskListChanged() {
    this.taskListChanged.next({});
  }

  getTaskChangedObservable() : Observable<any> {
    return this.taskListChanged;
  }

  getNewTaskEvent() : Observable<any> {
    return this.newTaskSubject;
  }

  getAllTasks() : Promise<Task[]> {
    console.log("Called server to get tasks");
    return this.http.get('tasks')
      .toPromise()
      .then(response => response.json() as Task[])
  }

  submitNewTask(task: Task) {
    this.http.post('tasks/add', task)
      .subscribe((res) => {
        if (res.status == 200) {
          this.notifyTaskListChanged();
          console.log("added new task", task)
        }
      });
  }

  updateTask(task: Task) {
    this.http.put('tasks/update', task)
      .subscribe((res) => {
        if (res.status == 200) {
          console.log("updated task", task)
          this.notifyTaskListChanged();
        }
      });

  }

  completeTask(task: Task){
    this.http.put(`tasks/complete/${task.id}`, null).subscribe(
      (res) => {
        if (res.status == 200) {
          this.notifyTaskListChanged();
          console.log("successfully completed task ", task)
        }
      }
    )
  }
  undoCompleteTask(task: Task){
    this.http.put(`tasks/undoComplete/${task.id}`, null).subscribe(
      (res) => {
        if (res.status == 200) {
          console.log("successfully undone completion of the task ", task);
          this.notifyTaskListChanged();
        }
      }
    )
  }
  deleteTask(task: Task) {
    this.http.put(`tasks/delete/${task.id}`, task).subscribe(
      (res) => {
        if (res.status == 200) {
          this.notifyTaskListChanged();
          console.log("successfully deleted task ", task)
        }
      }
    )
  }

  undoDelete(task: Task) {
    this.http.put(`tasks/undoDelete/${task.id}`, task).subscribe(
      (res) => {
        if (res.status == 200) {
          this.notifyTaskListChanged();
          console.log("successfully undone delete for task ", task)
        }
      }
    )
  }
}


export type FilterState = "Active" | "Deleted" | "Completed";
