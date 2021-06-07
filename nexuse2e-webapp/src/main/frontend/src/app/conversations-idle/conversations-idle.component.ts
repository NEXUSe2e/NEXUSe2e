import { Component, Input, OnInit } from "@angular/core";
import { IDLE_CONV_CONFIG } from "./conversations-idle.config";
import { ConversationMinimum } from "../types";

@Component({
  selector: "app-conversations-idle",
  templateUrl: "./conversations-idle.component.html",
  styleUrls: ["./conversations-idle.component.scss"],
})
export class ConversationsIdleComponent implements OnInit {
  @Input() dashboardTimeFrameInDays = 0;
  @Input() idleGracePeriodInMinutes = 0;
  idleConversations: ConversationMinimum[] | undefined;
  idleConversationsConfig = IDLE_CONV_CONFIG;
  loaded = false;

  constructor() {}

  ngOnInit(): void {
    this.loaded = true;
  }
}