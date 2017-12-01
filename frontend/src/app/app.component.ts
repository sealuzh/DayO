import {Component} from "@angular/core";
import {User} from "./common/user";
import {HttpWrapper} from "./common/http-wrapper.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'app';
  user: User = {} as User;

  constructor(private http: HttpWrapper) {
    http.get("user")
      .subscribe((res) => {
        this.user = res.json() as User
      })
  }
}
