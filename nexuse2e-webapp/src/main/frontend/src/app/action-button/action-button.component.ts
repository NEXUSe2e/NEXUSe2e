import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { Action, NexusData } from "../types";
import { PermissionService } from "../services/permission.service";
import { ActionService } from "../services/action.service";
import { SelectionService } from "../services/selection.service";

@Component({
  selector: "app-action-button",
  templateUrl: "./action-button.component.html",
  styleUrls: ["./action-button.component.scss"],
})
export class ActionButtonComponent implements OnInit {
  @Input() action!: Action;
  @Input() affectedItems?: NexusData[];
  @Output() triggerReload: EventEmitter<void> = new EventEmitter();
  isPermitted = false;
  inProgress = false;
  disabled = false;

  constructor(
    private permissionService: PermissionService,
    private actionService: ActionService,
    public selectionService: SelectionService
  ) {}

  async ngOnInit() {
    this.isPermitted = this.permissionService.isUserPermitted(
      this.action.actionKey
    );
  }

  isActionImpossible() {
    switch (this.action.actionKey) {
      case "/messages/requeue":
      case "/messages/stop":
        return this.selectionService.isEmpty("message");
      case "/conversations/delete":
        return this.selectionService.isEmpty("conversation");
      default: return false;
    }
  }

  async performAction(): Promise<void> {
    this.inProgress = true;
    switch (this.action.actionKey) {
      case "/messages/stop":
        await this.actionService.stopMessages();
        break;
      case "/message/stop":
        await this.actionService.stopMessages(this.affectedItems);
        break;
      case "/messages/requeue":
        await this.actionService.requeueMessages();
        break;
      case "/message/requeue":
        this.disabled = true;
        await this.actionService.requeueMessages(this.affectedItems);
        break;
      case "/conversations/delete":
        await this.actionService.deleteConversations();
        break;
      case "/conversation/delete":
        await this.actionService.deleteConversations(this.affectedItems);
        break;
    }
    this.inProgress = false;
    this.triggerReload.emit();
  }
}
