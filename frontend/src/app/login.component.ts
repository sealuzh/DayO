import {Component} from "@angular/core";
import {API_URL} from "./common/constants";


@Component({
  selector: 'login',
  template: `
    <a [href]="loginUrl" >Login</a>
    <!--<button (click)="submit()">Click to login</button>-->
  `,
  styles: [`

  `]
})
export class LoginComponent {

  constructor() { }

  loginUrl = API_URL + "/login";
}
