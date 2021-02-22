import { Component } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { LoginComponent } from "./login/login.component";
import { PageNotFoundComponent } from "./page-not-found/page-not-found.component";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"],
})
export class AppComponent {
  title = "frontend";
  showHeaderNav = false;

  constructor(translate: TranslateService) {
    translate.use("en");
  }

  // eslint-disable-next-line
  determineVisibility(event: any) {
    this.showHeaderNav = !(
      event instanceof LoginComponent || event instanceof PageNotFoundComponent
    );
  }
}