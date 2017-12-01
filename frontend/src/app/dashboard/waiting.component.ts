import {Component} from "@angular/core";
import "rxjs/add/observable/interval";
import "rxjs/add/observable/timer";
import "rxjs/add/operator/switchMap";
import {HttpWrapper} from "../common/http-wrapper.service";
import {Observable} from "rxjs/Observable";
import {Schedule} from "./left/schedule";
import {Router} from "@angular/router";

@Component({
  template: `
    <div class="row justify-content-md-center" style="margin-top: 40px">
    <div class="col-12">
      <h1>We're optimizing schedule for you</h1>
      <h2 *ngIf="!takesTooLong">Expected waiting time is {{minutes}}:{{seconds}}</h2>
      <h2 *ngIf="takesTooLong">It takes longer than expected. Please stay on this page...</h2>
      <div class="spinner">
        <div class="dot1"></div>
        <div class="dot2"></div>
      </div>
    </div>
    </div>
  `, styles: [`

    .spinner {
      margin: 100px auto;
      width: 40px;
      height: 40px;
      position: relative;
      text-align: center;

      -webkit-animation: sk-rotate 2.0s infinite linear;
      animation: sk-rotate 2.0s infinite linear;
    }

    .dot1, .dot2 {
      width: 60%;
      height: 60%;
      display: inline-block;
      position: absolute;
      top: 0;
      background-color: #333;
      border-radius: 100%;

      -webkit-animation: sk-bounce 2.0s infinite ease-in-out;
      animation: sk-bounce 2.0s infinite ease-in-out;
    }

    .dot2 {
      top: auto;
      bottom: 0;
      -webkit-animation-delay: -1.0s;
      animation-delay: -1.0s;
    }

    @-webkit-keyframes sk-rotate { 100% { -webkit-transform: rotate(360deg) }}
    @keyframes sk-rotate { 100% { transform: rotate(360deg); -webkit-transform: rotate(360deg) }}

    @-webkit-keyframes sk-bounce {
      0%, 100% { -webkit-transform: scale(0.0) }
      50% { -webkit-transform: scale(1.0) }
    }

    @keyframes sk-bounce {
      0%, 100% {
        transform: scale(0.0);
        -webkit-transform: scale(0.0);
      } 50% {
          transform: scale(1.0);
          -webkit-transform: scale(1.0);
        }
    }  
  `]
})
export class WaitingComponent {
  secondsTotal = 60;
  minutes: number;
  seconds: number;
  takesTooLong = false;


  constructor(http: HttpWrapper, router: Router) {
    this.updateMinutesSeconds();
    const handler = setInterval(() => {
      if (this.secondsTotal <= 0) {
        clearInterval(handler);
        this.takesTooLong = true;
      } else {
        this.secondsTotal--;
        this.updateMinutesSeconds();
      }
    }, 1000);

    const subscription = Observable.timer(5000, 2000)
      .switchMap(() => http.get("user/schedule/options"))
      .map(res => res.json())
      .subscribe((res: Schedule[]) => {
        if (res.length > 0) {
          subscription.unsubscribe();
          router.navigate(["chooseSchedule"]);
        }
      });
  }

  private updateMinutesSeconds() {
    this.minutes = Math.floor(this.secondsTotal / 60);
    this.seconds = Math.floor(this.secondsTotal % 60);
  }
}
