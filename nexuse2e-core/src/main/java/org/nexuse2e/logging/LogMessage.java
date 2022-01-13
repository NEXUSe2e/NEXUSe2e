/**
 *  NEXUSe2e Business Messaging Open Source
 *  Copyright 2000-2021, direkt gruppe GmbH
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation version 3 of
 *  the License.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.nexuse2e.logging;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.message.Message;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.pojo.MessagePojo;

/**
 * @author mbreilmann, gesch
 */
public class LogMessage implements Message {

    private String description    = null;
    private String conversationId = "unknown";
    private String messageId      = "unknown";
    private String choreograhyId  = "unknown";
    private String actionId       = "unknown";
    private Throwable throwable;

    /**
     * @param description
     * @param conversationId
     * @param messageId
     */
    public LogMessage(String description, String conversationId, String messageId, Throwable t) {

        if (description != null) {
            this.description = description;
        }
        if (conversationId != null) {
            this.conversationId = conversationId;
        }
        if (messageId != null) {
            this.messageId = messageId;
        }
        if (messageId != null) {
            this.throwable = t;
        }
    }

    /**
     * @param description
     * @param messagePojo
     * @param t
     */
    public LogMessage(String description, MessagePojo messagePojo, Throwable t) {

        this.description = description;
        if (messagePojo != null) {
            if (messagePojo.getAction() != null && StringUtils.isNotBlank(messagePojo.getAction().getName())) {
                this.actionId = messagePojo.getAction().getName();
            }
            if (messagePojo.getConversation() != null) {
                if (messagePojo.getConversation().getChoreography() != null && StringUtils.isNotBlank(messagePojo.getConversation().getChoreography().getName())) {
                    this.choreograhyId = messagePojo.getConversation().getChoreography().getName();
                }
                if (StringUtils.isNotBlank(messagePojo.getConversation().getConversationId())) {
                    this.conversationId = messagePojo.getConversation().getConversationId();
                }
            }
            if (StringUtils.isNotBlank(messagePojo.getMessageId())) {
                this.messageId = messagePojo.getMessageId();
            }
        }
        if (t != null) {
            this.throwable = t;
        }
    }

    /**
     * @param description
     */
    public LogMessage(String description, Throwable t) {
        this(description, null, null, t);
    }

    /**
     * @param description
     * @param messageContext
     */
    public LogMessage(String description, MessageContext messageContext, Throwable t) {
        this(description, (messageContext != null ? messageContext.getMessagePojo() : null), t);
    }

    /**
     * @param description
     */
    public LogMessage(String description) {
        this(description, null, null, null);
    }

    /**
     * @param description
     * @param conversationId
     * @param messageId
     */
    public LogMessage(String description, String conversationId, String messageId) {
        this(description, conversationId, messageId, null);
    }

    /**
     * @param description
     * @param messagePojo
     */
    public LogMessage(String description, MessagePojo messagePojo) {
        this(description, messagePojo, null);
    }

    /**
     * @param description
     * @param messageContext
     */
    public LogMessage(String description, MessageContext messageContext) {
        this(description, (messageContext != null ? messageContext.getMessagePojo() : null));
    }

    @Override
    public String getFormattedMessage() {
        return toString(true);
    }

    @Override
    public String getFormat() {
        return this.getFormattedMessage();
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    /**
     * @return
     */
    public String getConversationId() {

        return conversationId;
    }

    /**
     * @param conversationId
     */
    public void setConversationId(String conversationId) {

        this.conversationId = conversationId;
    }

    /**
     * @return
     */
    public String getDescription() {

        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * @return
     */
    public String getMessageId() {

        return messageId;
    }

    /**
     * @param messageId
     */
    public void setMessageId(String messageId) {

        this.messageId = messageId;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return toString(true);
    }

    /**
     * Gets the <code>Throwable</code> associated with this <code>LogMessage</code>.
     *
     * @return The <code>Throwable</code>, or <code>null</code> if no <code>Throwable</code> is associated with this
     * <code>LogMessage</code>.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Extracts an error message from this <code>LogMessage</code>'s <code>Throwable</code>.
     *
     * @return An error message, or <code>null</code> if no <code>Throwable</code> is associated with this
     * <code>LogMessage</code>.
     */
    protected String getErrorMessage() {

        Throwable t = throwable;
        while (t != null) {
            if (t.getCause() != null) {
                t = t.getCause();
            } else {
                break;
            }
        }
        if (t != null) {
            if (t.getMessage() != null && t.getMessage().length() > 0) {
                return t.getMessage();
            } else {
                return t.getClass().getSimpleName();
            }
        }
        return null;
    }

    /**
     * by default toString prepends message and conversation id. Use full=false
     * to suppress unnecessary ids.
     *
     * @param full
     * @return
     */
    public String toString(boolean full) {

        String errorMessage = getErrorMessage();
        if (full) {
            return conversationId + "/" + messageId + " (" + choreograhyId + "->" + actionId + "): " + description + (errorMessage != null ?
                ": " + errorMessage :
                "");
        } else {
            return description + (errorMessage != null ? ": " + errorMessage : "");
        }
    }
}
