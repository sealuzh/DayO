import {Http, RequestOptions, Response} from "@angular/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/operator/catch';
import {API_URL} from "./constants";


const withCredentials = new RequestOptions({withCredentials: true});

@Injectable()
export class HttpWrapper {
  constructor(private http: Http) {}

  post(relativeUrl: string, body: any): Observable<Response> {
    return this.http.post(`${API_URL}/${relativeUrl}`, body, withCredentials).catch(errorHandler);
  }

  put(relativeUrl: string, body: any): Observable<Response> {
    return this.http.put(`${API_URL}/${relativeUrl}`, body, withCredentials).catch(errorHandler);
  }

  get(relativeUrl: string): Observable<Response> {
    return this.http.get(`${API_URL}/${relativeUrl}`, withCredentials).catch(errorHandler);
  }
}

const errorHandler = err => {
  window.location.replace(API_URL);
  throw new Error("Should never happen");
};
