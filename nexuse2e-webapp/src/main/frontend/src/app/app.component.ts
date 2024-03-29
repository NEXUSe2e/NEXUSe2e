import {Component, OnInit} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {LoginComponent} from "./login/login.component";
import {PageNotFoundComponent} from "./page-not-found/page-not-found.component";
import {PermissionService} from "./services/permission.service";
import {DataService} from "./services/data.service";
import {Subject} from "rxjs";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"],
})
export class AppComponent implements OnInit {
  showHeaderNav = false;
  navExpanded = false;

  constructor(
    translate: TranslateService,
    private permissionService: PermissionService,
    private dataService: DataService
  ) {
    translate.use("en");
  }

  targetElement: HTMLElement | null = null;

  pullToRefresh(event: Subject<any>) {
    setTimeout(() => {
      window.location.reload();
      event.next();
    }, 300);
  }

  async ngOnInit() {
    if (await this.dataService.isLoggedIn()) {
      this.permissionService.updatePermissions();
    }
    this.targetElement = document.querySelector('.content-box');
  }

  // eslint-disable-next-line
  determineVisibility(event: any) {
    this.showHeaderNav = !(
      event instanceof LoginComponent || event instanceof PageNotFoundComponent
    );
  }
}
