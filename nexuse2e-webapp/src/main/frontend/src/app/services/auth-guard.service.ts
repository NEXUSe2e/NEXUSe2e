import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, } from "@angular/router";
import { Injectable } from "@angular/core";
import { DataService } from "./data.service";

@Injectable({
  providedIn: "root",
})
export class AuthGuardService implements CanActivate {
  constructor(private router: Router, private dataService: DataService) {}

  async canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean> {
    if (await this.dataService.isLoggedIn()) {
      return true;
    } else {
      this.router.navigate(["/login"], {
        queryParams: { returnUrl: state.url },
      });
      return false;
    }
  }
}
