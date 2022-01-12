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
import org.apache.logging.log4j.Level;
//import org.apache.logging.log4j.spi.LoggingEvent;
import org.nexuse2e.BeanStatus;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.nexuse2e.configuration.ParameterType;
import org.nexuse2e.pojo.ChoreographyPojo;
import org.nexuse2e.pojo.ConversationPojo;
import org.nexuse2e.service.Service;
import org.nexuse2e.service.mail.SmtpSender;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.PatternSyntaxException;

/**
 * @author mbreilmann
 */
public class EmailLogger extends AbstractLogger {

    private static final String NEW_LINE = " \n ";
    private static final String SERVICE_PARAM_NAME = "service";
    private static final String RECIPIENT_PARAM = "recipient";
    private static final String SUBJECT_PARAM = "subject";
    private static final String CHOREOGRAPHY_FILTER_PARAM = "choreographyFilter";
    private static final String TEXT_INCLUDED_FILTER_PARAM = "textIncludedFilter";
    private static final String REGEX_INCLUDED_FILTER_PARAM = "regexIncludedFilter";
    private static final String TEXT_EXCLUDED_FILTER_PARAM = "textExcludedFilter";
    private static final String REGEX_EXCLUDED_FILTER_PARAM = "regexExcludedFilter";
    private String serviceName = null;
    private String recipient = null;
    private String subject = null;
    private String choreographyFilter = null;
    private boolean checkChoreography = false;
    private BlockingQueue<String[]> mailQueue;
    private String includeFilter = null;
    private String excludeFilter = null;
    private boolean includeRegex;
    private boolean excludeRegex;
    private boolean includeRequired;
    private boolean excludeRequired;


    /**
     * Default constructor.
     */
    public EmailLogger() {

        parameters = new HashMap<String, Object>();
        parameterMap = new LinkedHashMap<String, ParameterDescriptor>();
        parameterMap.put(SERVICE_PARAM_NAME, new ParameterDescriptor(ParameterType.SERVICE, "Service",
                "The name of the SMTP service that shall be used by the sender", SmtpSender.class));
        parameterMap.put(RECIPIENT_PARAM, new ParameterDescriptor(ParameterType.STRING, "Recipient",
                "The recipient(s) of the email", ""));
        parameterMap.put(SUBJECT_PARAM, new ParameterDescriptor(ParameterType.STRING, "Subject",
                "The subject line of the email", ""));
        parameterMap.put(CHOREOGRAPHY_FILTER_PARAM, new ParameterDescriptor(ParameterType.STRING,
                "Choreography Filter", "The ID of a choreography to display messages for", ""));
        parameterMap.put(REGEX_INCLUDED_FILTER_PARAM, new ParameterDescriptor(ParameterType.BOOLEAN,
                "Treat text as regex", "When checked, the text below is treated as regex, otherwise only as contains.", false));
        parameterMap.put(TEXT_INCLUDED_FILTER_PARAM, new ParameterDescriptor(ParameterType.STRING,
                "Text Inclusion Filter", "Mails are only generated if the log message contains this text. Include overrides Exclude in this case.", ""));
        parameterMap.put(REGEX_EXCLUDED_FILTER_PARAM, new ParameterDescriptor(ParameterType.BOOLEAN,
                "Treat text as regex", "When checked, the text below is treated as regex, otherwise only as contains.", false));
        parameterMap.put(TEXT_EXCLUDED_FILTER_PARAM, new ParameterDescriptor(ParameterType.STRING,
                "Text Exclusion Filter", "Mails are not generated if the log message contains the text.", ""));
        status = BeanStatus.INSTANTIATED;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#initialize(org.nexuse2e.configuration.EngineConfiguration)
     */
    public void initialize(EngineConfiguration config) {

        String serviceName = getParameter(SERVICE_PARAM_NAME);
        String recipient = getParameter(RECIPIENT_PARAM);
        String subject = getParameter(SUBJECT_PARAM);
        String choreography = getParameter(CHOREOGRAPHY_FILTER_PARAM);

        includeFilter = getParameter(TEXT_INCLUDED_FILTER_PARAM);
        excludeFilter = getParameter(TEXT_EXCLUDED_FILTER_PARAM);
        includeRegex = getParameter(REGEX_INCLUDED_FILTER_PARAM);
        excludeRegex = getParameter(REGEX_EXCLUDED_FILTER_PARAM);


        if (StringUtils.isBlank(serviceName)) {
            System.err.println("EmailLogger.initialize(): Service name not specified. Please check your configuration");
            return;
        }

        if ((recipient == null) || (recipient.length() == 0)) {
            System.err.println("EmailLogger.initialize(): Recipient(s) not specified. Please check your configuration");
            return;
        }
        if ((subject == null) || (subject.length() == 0)) {
            System.err.println("EmailLogger.initialize(): Subject not specified. Please check your configuration");
            return;
        }

        if (!StringUtils.isEmpty(choreography)) {
            System.err.println("EmailLogger.initialize(): Filtering on choreography: " + choreography);
            choreographyFilter = choreography;
            checkChoreography = true;
        }

        this.serviceName = serviceName;
        this.recipient = recipient;
        this.subject = subject;

        includeRequired = StringUtils.isNotBlank(includeFilter);
        excludeRequired = StringUtils.isNotBlank(excludeFilter);

        status = BeanStatus.INITIALIZED;
    }

    public void activate() {
        // ensure mail queue thread is killed
        if (mailQueue != null) {
            mailQueue.offer(new String[0]);
        }
        mailQueue = new LinkedBlockingQueue<String[]>();
        new LoggerThread(mailQueue).start();
        super.activate();
    }

    public void deactivate() {
        // kill mail queue thread
        if (mailQueue != null) {
            mailQueue.offer(new String[0]);
        }
        super.deactivate();
    }

    private SmtpSender findService() {

        SmtpSender smtpSender = null;

        if (serviceName != null && serviceName.trim().length() > 0) {
            EngineConfiguration engineConfiguration = Engine.getInstance().getCurrentConfiguration();
            if (engineConfiguration != null) {
                Service service = engineConfiguration.getStaticBeanContainer().getService(serviceName);
                if (service == null) {
                    System.err.println("EmailLogger.initialize(): Service \"" + serviceName
                            + "\" not found. Please check your configuration");
                    return null;
                }
                if (!(service instanceof SmtpSender)) {
                    System.err.println("EmailLogger.initialize(): Service \"" + serviceName
                            + "\" not of type SmtpSender. Please check your configuration");
                    return null;
                }
                smtpSender = (SmtpSender) service;
            }
        } else {
            System.err.println("No SMTP service found. Please check your configuration");
            return null;
        }

        return smtpSender;
    }

    /* (non-Javadoc)
     * @see org.apache.logging.log4j.AppenderSkeleton#append(org.apache.logging.log4j.spi.LoggingEvent)
     */
//    @Override
//    protected void append(LoggingEvent loggingEvent) {
//
//        try {
//            ChoreographyPojo choreographyPojo = null;
//            boolean matchedChoreography = false;
//
//            if (status != BeanStatus.ACTIVATED) {
//                return;
//            }
//
//
//            if (!loggingEvent.getLevel().isGreaterOrEqual(Level.toLevel(getLogThreshold(), Level.ERROR))) {
//                return;
//            }
//            if (checkChoreography && (loggingEvent.getMessage() instanceof LogMessage)) {
//                LogMessage logMessage = (LogMessage) loggingEvent.getMessage();
//                if (logMessage.getConversationId() != null) {
//                    ConversationPojo conversationPojo;
//                    try {
//                        conversationPojo = Engine.getInstance().getTransactionService().getConversation(
//                                logMessage.getConversationId());
//                        if (conversationPojo != null) {
//                            choreographyPojo = conversationPojo.getChoreography();
//                            if (choreographyPojo != null) {
//                                matchedChoreography = choreographyFilter.equals(choreographyPojo.getName());
//                            }
//                        }
//                    } catch (NexusException e) {
//                        System.err.println("Error identifying choreography when filtering email notification: " + e);
//                    }
//                }
//            }
//
//            if ((!checkChoreography || matchedChoreography) && mailQueue != null) {
//                if (matchLogText(loggingEvent.getRenderedMessage())) {
//                    mailQueue.offer(new String[]{loggingEvent.getRenderedMessage()});
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("An Excpetion occured while creating email notification: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//    }

    private boolean matchLogText(String renderedMessage) {
        if (StringUtils.isNotBlank(renderedMessage)) {
            if (includeRequired) {
                if (includeRegex) {
                    try {
                        return renderedMessage.matches(includeFilter);
                    } catch (PatternSyntaxException pse) {
                        System.err.println("Pattern configuration for Email logger is not valid: " + pse.getMessage());
                    }
                } else {
                    return renderedMessage.contains(includeFilter);
                }
            }
            if (excludeRequired) {
                if (excludeRegex) {
                    try {
                        return !renderedMessage.matches(excludeFilter);
                    } catch (PatternSyntaxException pse) {
                        System.err.println("Pattern configuration for Email logger is not valid: " + pse.getMessage());
                    }
                } else {
                    return !renderedMessage.contains(excludeFilter);
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.logging.log4j.AppenderSkeleton#close()
     */
    @Override
    public void close() {
    }

    class LoggerThread extends Thread {

        BlockingQueue<String[]> mailQueue;

        public LoggerThread(BlockingQueue<String[]> mailQueue) {
            super("EmailLogger thread");
            this.mailQueue = mailQueue;
        }

        public void run() {

            // loop until shutdown signal (string array of length 0 passed to queue)
            try {
                for (String[] logEntry = mailQueue.take(); logEntry.length != 0; logEntry = mailQueue.take()) {
                    SmtpSender smtpSender = findService();

                    StringBuilder message = new StringBuilder(logEntry[0]);

                    // get messages from queue if available without blocking
                    for (String[] entry = mailQueue.peek(); entry != null; entry = mailQueue.peek()) {
                        if (entry.length == 0) { // logger shut down
                            break;
                        } else {
                            message.append(NEW_LINE + NEW_LINE + entry[0]);
                            mailQueue.poll(); // make sure entry is removed
                        }
                    }

                    if (smtpSender != null) {
                        if (smtpSender.getStatus() == BeanStatus.STARTED) {
                            try {
                                smtpSender.sendMessage(recipient, subject, message.toString());
                            } catch (NexusException e) {
                                System.err.println("Error sending log email: " + e);
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.err.println("SMTP service not available!");
                    }
                }
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
            //System.out.println( "shutdown mail thread" );
        }
    }
} // EmailLogger
