/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.pagelogs.internal;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.job.event.status.EndStepProgressEvent;
import org.xwiki.job.event.status.StartStepProgressEvent;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.Message;
import org.xwiki.logging.event.LoggerListener;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * Captures logs when the rendering for the main threads starts, save them for later display int he UI, and stop the
 * capture when the rendering ends.
 *
 * @version $Id$
 */
@Component
@Named("PageLogs")
public class PageLogsEventListener implements EventListener
{
    private static final String LOGGER_KEY = "PageLogsLogger";

    private static final String COUNTER_KEY = "PageLogsProgressCounter";

    @Inject
    private LoggerManager loggerManager;

    @Inject
    private Execution execution;

    @Override
    public String getName()
    {
        return "PageLogs";
    }

    @Override
    public List<Event> getEvents()
    {
        return Arrays.asList(new StartStepProgressEvent(), new EndStepProgressEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof StartStepProgressEvent) {
            handleStart(data);
        } else {
            handleStop();
        }
    }

    private void handleStart(Object data)
    {
        if (data instanceof Message) {
            Message message = (Message) data;
            if (message.getTranslationKey().equals("document.progress.render")) {
                LogQueue logQueue = new LogQueue();
                this.loggerManager.pushLogListener(new LoggerListener(getName(), logQueue));
                // Save the logger in the execution context so that we can save the logs in the end step
                this.execution.getContext().setProperty(LOGGER_KEY, logQueue);
            } else {
                // Start counting nested start progress so that we can find the matching end step event in
                // handleStop.
                Integer counter = getProgressCounter();
                counter++;
            }
        }
    }

    private void handleStop()
    {
        Integer counter = getProgressCounter();
        if (counter == 0) {
            // TODO: save in the permanent directory for advanced use cases... (to be defined)
            // FTM the Logger is in the execution context
            this.loggerManager.popLogListener();
            this.execution.getContext().removeProperty(COUNTER_KEY);
        } else {
            counter--;
        }
    }

    private int getProgressCounter()
    {
        Integer counter = (Integer) this.execution.getContext().getProperty(COUNTER_KEY);
        if (counter == null) {
            counter = 0;
            this.execution.getContext().setProperty(COUNTER_KEY, counter);
        }
        return counter;
    }
}
