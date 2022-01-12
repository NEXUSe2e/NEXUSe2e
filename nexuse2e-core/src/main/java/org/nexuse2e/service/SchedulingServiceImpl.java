/**
 * NEXUSe2e Business Messaging Open Source
 * Copyright 2000-2021, direkt gruppe GmbH
 * <p>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation version 3 of
 * the License.
 * <p>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.nexuse2e.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.Layer;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gesch
 *
 *
 */
public class SchedulingServiceImpl extends AbstractService implements SchedulingService {

    private final static String QUARTZGROUP = "NX_GROUP";
    private static Logger LOG = LogManager.getLogger(SchedulingServiceImpl.class);
    private HashMap<SchedulerClient, ScheduledFuture<?>[]> concurrentClients = new HashMap<SchedulerClient,
            ScheduledFuture<?>[]>();
    private HashMap<SchedulerClient, ScheduledExecutorService> schedulers = new HashMap<SchedulerClient,
            ScheduledExecutorService>();
    private Map<String, SchedulerClient> quartzClients = new HashMap<String, SchedulerClient>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    private Scheduler quartzScheduler = null;

    /* (non-Javadoc)
     * @see org.nexuse2e.service.AbstractService#start()
     */
    @Override
    public void start() {

        LOG.trace("starting");
        super.start();
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.service.AbstractService#stop()
     */
    @Override
    public void stop() {

        LOG.trace("stopping");
        super.stop();
    }

    @Override
    public void fillParameterMap(Map<String, ParameterDescriptor> parameterMap) {

    }

    @Override
    public void teardown() {

        for (ScheduledFuture<?>[] handles : concurrentClients.values()) {
            for (int i = 0; i < handles.length; i++) {
                handles[i].cancel(true);
            }
        }
        for (ScheduledExecutorService scheduledExecutorService : schedulers.values()) {
            scheduledExecutorService.shutdown();
        }

        super.teardown();
    }

    @Override
    public Layer getActivationLayer() {

        return Layer.CORE;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.service.SchedulingService#registerClient(org.nexuse2e.service.SchedulerClient, long)
     */
    public void registerClient(SchedulerClient client, long millseconds) throws IllegalArgumentException {

        LOG.trace("registerClient");
        LOG.debug("client: " + client + " Interval: " + millseconds + " millseconds");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        SchedulingThread thread = new SchedulingThread(client);
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(thread, 0, millseconds, TimeUnit.MILLISECONDS);

        ScheduledFuture<?>[] handles = {handle};
        concurrentClients.put(client, handles);
        schedulers.put(client, scheduler);

    }

    /* (non-Javadoc)
     * @see org.nexuse2e.service.SchedulingService#registerClient(org.nexuse2e.service.SchedulerClient, java.lang
     * .String)
     */
    public void registerClient(SchedulerClient client, String pattern) throws IllegalArgumentException {

        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(pattern);
        if (m.matches()) {
            registerClient(client, Long.parseLong(pattern));
            return;
        }
        if (pattern.contains(":")) {
            try {
                StringTokenizer st = new StringTokenizer(pattern, ",");
                ArrayList<Date> times = new ArrayList<Date>();
                while (st.hasMoreTokens()) {
                    String time = st.nextToken();
                    LOG.debug("Parsing scheduled time: " + time);
                    Date scheduledTime = simpleDateFormat.parse(time);
                    Calendar tempCalendar = new GregorianCalendar();
                    tempCalendar.setTime(scheduledTime);
                    Calendar scheduledCalendar = new GregorianCalendar();
                    scheduledCalendar.set(Calendar.HOUR_OF_DAY, tempCalendar.get(Calendar.HOUR_OF_DAY));
                    scheduledCalendar.set(Calendar.MINUTE, tempCalendar.get(Calendar.MINUTE));
                    scheduledCalendar.set(Calendar.SECOND, 0);
                    scheduledCalendar.set(Calendar.MILLISECOND, 0);
                    scheduledTime = new Date(scheduledCalendar.getTimeInMillis());
                    times.add(scheduledTime);
                    LOG.debug("Scheduled time: " + scheduledTime);
                }
                registerClient(client, times);
            } catch (ParseException e) {
                throw new IllegalArgumentException("invalid time pattern:" + pattern, e);
            }
            return;
        }

        try {
            String jobName = client.getClass() + "@" + client.hashCode();
            quartzClients.put(jobName, client);
            if (quartzScheduler == null) {
                SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
                quartzScheduler = schedulerFactory.getScheduler();
                quartzScheduler.start();
            }

            JobDetail jd = new JobDetail(jobName, QUARTZGROUP, SchedulingJob.class);
            CronTrigger ct = new CronTrigger();
            ct.setName("trigger" + jobName);
            jd.getJobDataMap().put("client", client);
            ct.setCronExpression(new CronExpression(pattern));
            quartzScheduler.scheduleJob(jd, ct);
            LOG.debug("next Schedule Date: " + ct.getNextFireTime());

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.nexuse2e.service.SchedulingService#registerClient(org.nexuse2e.service.SchedulerClient, java.util.Date)
     */
    public void registerClient(SchedulerClient client, List<Date> times) throws IllegalArgumentException {

        LOG.trace("registerClient");
        LOG.debug("client: " + client + " Times: " + times);

        if (!times.isEmpty()) {

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            ScheduledFuture<?>[] handles = new ScheduledFuture<?>[times.size()];
            int handleCount = 0;

            for (Date time : times) {
                Date now = new Date();
                long delay = time.getTime() - now.getTime();
                if (delay < 0) {
                    delay += (24 * 60 * 60 * 1000);
                }

                LOG.trace("delay: " + delay);

                SchedulingThread thread = new SchedulingThread(client);
                ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(thread, delay, (24 * 60 * 60 * 1000),
                        TimeUnit.MILLISECONDS);
                handles[handleCount++] = handle;
            }

            concurrentClients.put(client, handles);
            schedulers.put(client, scheduler);
        }
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.service.SchedulingService#deregisterClient(org.nexuse2e.service.SchedulerClient)
     */
    public void deregisterClient(SchedulerClient client) throws IllegalArgumentException {

        LOG.trace("deregistering client");
        ScheduledFuture<?>[] handles = concurrentClients.get(client);
        if (handles != null) {
            for (int i = 0; i < handles.length; i++) {
                ScheduledFuture<?> handle = handles[i];
                if (handle != null) {
                    handle.cancel(false);
                    LOG.debug("deregisterClient - processing cancelled!");
                }
            }
            try {
                ScheduledExecutorService scheduler = schedulers.remove(client);
                if (scheduler != null) {
                    LOG.debug("Shutting down scheduler...");
                    scheduler.shutdownNow();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            concurrentClients.remove(client);
        }

        try {
            if (quartzScheduler != null) {

                String jobName = client.getClass() + "@" + client.hashCode();
                quartzScheduler.deleteJob(jobName, QUARTZGROUP);

                quartzClients.remove(jobName);
                if (quartzClients.size() == 0) {
                    quartzScheduler.shutdown();
                    quartzScheduler = null;
                }
            }
        } catch (SchedulerException e) {
            throw new IllegalArgumentException(e);
        }

    }

    /**
     * @author gesch
     *
     */
    public class SchedulingThread implements Runnable {

        private SchedulerClient client;

        /**
         * @param client
         */
        public SchedulingThread(SchedulerClient client) {

            this.client = client;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {

            // LOG.trace( "running" );
            if (client != null) {
                client.scheduleNotify();
            } else {
                LOG.error("SchedulerClient == null");
            }
        }

    }
}
