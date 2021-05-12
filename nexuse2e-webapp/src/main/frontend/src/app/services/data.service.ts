import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { environment } from "../../environments/environment";
import {
  ActiveFilterList,
  Conversation,
  ConversationDetail,
  EngineLog,
  Message,
  MessageDetail,
} from "../types";

@Injectable({
  providedIn: "root",
})
export class DataService {
  API_URL: string;

  private cache: { [key: string]: Promise<unknown> } = {};

  constructor(private http: HttpClient) {
    if (environment.API_URL.length) {
      this.API_URL = environment.API_URL;
    } else {
      this.API_URL = "../api";
    }
  }

  private get<T>(path: string, cache = true, params?: HttpParams): Promise<T> {
    if (cache) {
      let key = path;
      if (params) {
        key += JSON.stringify(params);
      }
      if (this.cache[key]) {
        return this.cache[key] as Promise<T>;
      }
      return (this.cache[key] = this.http
        .get<T>(this.API_URL + path, { params: params })
        .toPromise());
    }
    return this.http
      .get<T>(this.API_URL + path, { params: params })
      .toPromise<T>();
  }

  public clearCache() {
    this.cache = {};
  }

  getFullUsername(): Promise<string> {
    return this.get<string>("/full-username");
  }

  getLoggedIn() {
    // This response should not be cached because otherwise,
    // the user would not be logged out if their session expired
    return this.get("/logged-in", false);
  }

  getPermittedActions(): Promise<string[]> {
    return this.get("/allowed-endpoints");
  }

  getMachineName(): Promise<string> {
    return this.get<string>("/machine-name");
  }

  private static buildFilterParams(activeFilters: ActiveFilterList) {
    let httpParams = new HttpParams();
    for (const fieldName in activeFilters) {
      if (activeFilters.hasOwnProperty(fieldName)) {
        const value = activeFilters[fieldName];
        if (value) {
          if (typeof value === "string") {
            httpParams = httpParams.append(fieldName, value);
          } else {
            // type is DateRange
            if (value.startDate) {
              httpParams = httpParams.append(
                "startDate",
                value.startDate.toISOString()
              );
            }
            if (value.endDate) {
              httpParams = httpParams.append(
                "endDate",
                value.endDate.toISOString()
              );
            }
          }
        }
      }
    }
    return httpParams;
  }

  getMessages(
    pageIndex: number,
    itemsPerPage: number,
    activeFilters: ActiveFilterList
  ): Promise<Message[]> {
    let params = DataService.buildFilterParams(activeFilters);
    params = params.append("pageIndex", String(pageIndex));
    params = params.append("itemsPerPage", String(itemsPerPage));
    return this.get<Message[]>("/messages", false, params);
  }

  getMessageById(nxMessageId: string) {
    let httpParams = new HttpParams();
    httpParams = httpParams.append("nxMessageId", String(nxMessageId));
    return this.get<MessageDetail>("/message", false, httpParams);
  }

  getMessagesCount(activeFilters: ActiveFilterList = {}): Promise<number> {
    return this.get<number>(
      "/messages/count",
      false,
      DataService.buildFilterParams(activeFilters)
    );
  }

  requeueMessages(messageIds: string[]): Promise<void> {
    return this.post("/messages/requeue", messageIds);
  }

  stopMessages(messageIds: string[]): Promise<void> {
    return this.post("/messages/stop", messageIds);
  }

  getConversations(
    pageIndex: number,
    itemsPerPage: number,
    activeFilters: ActiveFilterList
  ): Promise<Conversation[]> {
    let params = DataService.buildFilterParams(activeFilters);
    params = params.append("pageIndex", String(pageIndex));
    params = params.append("itemsPerPage", String(itemsPerPage));
    return this.get<Conversation[]>("/conversations", false, params);
  }

  getConversationsCount(activeFilters: ActiveFilterList = {}): Promise<number> {
    return this.get<number>(
      "/conversations/count",
      false,
      DataService.buildFilterParams(activeFilters)
    );
  }

  getConversationById(nxConversationId: string) {
    let httpParams = new HttpParams();
    httpParams = httpParams.append(
      "nxConversationId",
      String(nxConversationId)
    );
    return this.get<ConversationDetail>("/conversation", false, httpParams);
  }

  deleteConversations(conversationIds: string[]): Promise<void> {
    return this.post("/conversations/delete", conversationIds);
  }

  getEngineLogs(
    pageIndex: number,
    itemsPerPage: number,
    activeFilters: ActiveFilterList
  ): Promise<EngineLog[]> {
    let params = DataService.buildFilterParams(activeFilters);
    params = params.append("pageIndex", String(pageIndex));
    params = params.append("itemsPerPage", String(itemsPerPage));
    return this.get<EngineLog[]>("/engine-logs", false, params);
  }

  getEngineLogsCount(activeFilters: ActiveFilterList = {}): Promise<number> {
    return this.get<number>(
      "/engine-logs/count",
      false,
      DataService.buildFilterParams(activeFilters)
    );
  }

  getPartnerIds(): Promise<string[]> {
    return this.get<string[]>("/participants/ids");
  }

  getChoreographyIds(): Promise<string[]> {
    return this.get<string[]>("/choreographies/ids");
  }

  getVersion(): Promise<string[]> {
    return this.get<string[]>("/version");
  }

  downloadPayload(message: MessageDetail, payloadId?: number) {
    return (
      "/DataSaveAs.do?type=content&choreographyId=" +
      message.choreographyId +
      "&participantId=" +
      message.partnerId +
      "&conversationId=" +
      message.conversationId +
      "&messageId=" +
      message.messageId +
      (payloadId !== undefined ? "&no=" + payloadId : "")
    );
  }

  private post(path: string, body: unknown): Promise<void> {
    return this.http.post<void>(this.API_URL + path, body).toPromise();
  }

  postLogin(body: { user: string; password: string }): Promise<void> {
    return this.post("/login", body);
  }

  postLogout(): Promise<void> {
    return this.post("/logout", null);
  }
}
