package org.nexuse2e.ui2.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.nexuse2e.ConversationStatus;
import org.nexuse2e.Engine;
import org.nexuse2e.MessageStatus;
import org.nexuse2e.dao.TransactionDAO;
import org.nexuse2e.messaging.Constants;
import org.nexuse2e.pojo.ConversationPojo;
import org.nexuse2e.pojo.MessagePojo;
import org.nexuse2e.reporting.StatisticsConversation;
import org.nexuse2e.reporting.StatisticsMessage;
import org.nexuse2e.util.DateWithTimezoneSerializer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TransactionReportingHandler implements Handler {
    @Override
    public boolean canHandle(String path, String method) {
        return ("GET".equalsIgnoreCase(method) && "/messages".equalsIgnoreCase(path)) ||
                ("GET".equalsIgnoreCase(method) && "/messages/count".equalsIgnoreCase(path)) ||
                ("GET".equalsIgnoreCase(method) && "/conversations".equalsIgnoreCase(path)) ||
                ("GET".equalsIgnoreCase(method) && "/conversations/count".equalsIgnoreCase(path));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getPathInfo();
        if (path != null) {
            switch (StringUtils.lowerCase(path)) {
                case "/messages":
                    this.getMessages(request, response);
                    break;
                case "/messages/count":
                    this.getMessagesCount(request, response);
                    break;
                case "/conversations":
                    this.getConversations(request, response);
                    break;
                case "/conversations/count":
                    this.getConversationsCount(request, response);
                    break;
            }
        }
    }

    private void getMessagesCount(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String status = request.getParameter("status");
        String type = request.getParameter("type");
        String conversationId = request.getParameter("conversationId");
        String messageId = request.getParameter("messageId");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        long messagesCount = Engine.getInstance().getTransactionService().getMessagesCount(
                getMessageStatusNumberFromName(status),
                getMessageTypeFromName(type),
                0, 0, conversationId, messageId,
                getDateFromString(startDate),
                getDateFromString(endDate));
        String message = new Gson().toJson(messagesCount);
        response.getOutputStream().print(message);
    }

    private String getMessageStatusNumberFromName(String statusName) {
        try {
            return String.valueOf(MessageStatus.valueOf(statusName).getOrdinal());
        } catch (Exception e) {
            return null;
        }
    }

    private String getConversationStatusNumberFromName(String statusName) {
        try {
            return String.valueOf(ConversationStatus.valueOf(statusName).getOrdinal());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getMessageTypeFromName(String typeName) {
        if (typeName != null) {
            switch (typeName) {
                case "ACKNOWLEDGEMENT":
                    return Constants.INT_MESSAGE_TYPE_ACK;
                case "NORMAL":
                    return Constants.INT_MESSAGE_TYPE_NORMAL;
                case "ERROR":
                    return Constants.INT_MESSAGE_TYPE_ERROR;
            }
        }
        return null;
    }

    private Date getDateFromString(String dateString) {
        if (dateString != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            try {
                return formatter.parse(dateString);
            } catch (ParseException e) {
                return null;
            }
        }
        return null;
    }

    private void getMessages(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String pageIndex = request.getParameter("pageIndex");
        String itemsPerPage = request.getParameter("itemsPerPage");
        String status = request.getParameter("status");
        String type = request.getParameter("type");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String conversationId = request.getParameter("conversationId");
        String messageId = request.getParameter("messageId");
        if (NumberUtils.isNumber(pageIndex) && NumberUtils.isNumber(itemsPerPage)) {
            List<MessagePojo> reportMessages = Engine.getInstance().getTransactionService().getMessagesForReport(
                    getMessageStatusNumberFromName(status),
                    0, 0, conversationId, messageId,
                    getMessageTypeFromName(type),
                    getDateFromString(startDate),
                    getDateFromString(endDate),
                    Integer.parseInt(itemsPerPage),
                    Integer.parseInt(pageIndex),
                    TransactionDAO.SORT_CREATED,
                    false);

            List<StatisticsMessage> messages = new LinkedList<>();
            for (MessagePojo messagePojo : reportMessages) {
                messages.add(new StatisticsMessage(messagePojo));
            }

            Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateWithTimezoneSerializer()).create();
            String messageJson = gson.toJson(messages);
            response.getOutputStream().print(messageJson);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameters: pageIndex and itemsPerPage");
        }
    }

    private void getConversationsCount(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String status = request.getParameter("status");
        String conversationId = request.getParameter("conversationId");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        long conversationsCount = Engine.getInstance().getTransactionService().getConversationsCount(
                getConversationStatusNumberFromName(status),
                0, 0, conversationId,
                getDateFromString(startDate),
                getDateFromString(endDate),
                0, false);
        String message = new Gson().toJson(conversationsCount);
        response.getOutputStream().print(message);
    }

    private void getConversations(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String pageIndex = request.getParameter("pageIndex");
        String itemsPerPage = request.getParameter("itemsPerPage");
        String status = request.getParameter("status");
        String conversationId = request.getParameter("conversationId");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        if (NumberUtils.isNumber(pageIndex) && NumberUtils.isNumber(itemsPerPage)) {
            List<ConversationPojo> reportConversations = Engine.getInstance().getTransactionService().getConversationsForReport(
                    getConversationStatusNumberFromName(status),
                    0, 0, conversationId,
                    getDateFromString(startDate),
                    getDateFromString(endDate),
                    Integer.parseInt(itemsPerPage),
                    Integer.parseInt(pageIndex),
                    TransactionDAO.SORT_CREATED,
                    false);
            List<StatisticsConversation> conversations = new LinkedList<>();
            for (ConversationPojo conversationPojo : reportConversations) {
                conversations.add(new StatisticsConversation(conversationPojo));
            }

            Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateWithTimezoneSerializer()).create();
            String conversationJson = gson.toJson(conversations);
            response.getOutputStream().print(conversationJson);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameters: pageIndex and itemsPerPage");
        }
    }
}