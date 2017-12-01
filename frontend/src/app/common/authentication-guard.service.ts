import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {HttpWrapper} from "./http-wrapper.service";
import {API_URL} from "./constants";

@Injectable()
export class AuthenticationGuard implements CanActivate {
  constructor(private http: HttpWrapper, private router: Router){}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.http.get("user/state")
      .map(res => {
        console.log(res);
        let headers = res.headers;
        // if not logged in - redirect to server-side login page
        let contentType = headers.get("content-type");
        if (contentType && contentType.startsWith("text/html")) { // server returns REDIRECT and browser already follows it and sends us login page content
          window.location.replace(API_URL);
          return false;
        }

        const state = res.json() as UserState;
        if (!state.hasSettings) {
          console.log("No settings - redirecting to settings");
          this.router.navigate(['settings']);
        } else if (!state.hasTasks) {
          console.log("No tasks - redirecting to dashboard/NO_TASKS");
          this.router.navigate(['dashboard', 'NO_TASKS']);
        } else if (!state.hasDailyForm) {
          console.log("No daily form - redirecting to dashboard/ABSENT_DAILY_FORM");
          this.router.navigate(['dashboard', 'ABSENT_DAILY_FORM']);
        } else {
          console.log("User has tasks, settings and daily form - redirecting to dashboard/SCHEDULE_CHOSEN");
          this.router.navigate(['dashboard', 'SCHEDULE_CHOSEN']);
        }
        return true;
      });
  }
}

interface UserState {
  hasSettings: boolean
  hasTasks: boolean
  hasDailyForm: boolean
}
