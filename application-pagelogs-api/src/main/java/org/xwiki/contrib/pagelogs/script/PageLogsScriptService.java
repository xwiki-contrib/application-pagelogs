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
package org.xwiki.contrib.pagelogs.script;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.pagelogs.internal.PageLogsManager;
import org.xwiki.logging.LogQueue;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

/**
 * Provide scripting access to the Page Logs API.
 *
 * @version $Id$
 */
@Component
@Singleton
@Named("pagelogs")
public class PageLogsScriptService implements ScriptService
{
    @Inject
    private PageLogsManager manager;

    /**
     * @return the logs containing the execution of the specified page rendering for the specified user
     */
    public LogQueue getLogQueue(DocumentReference userReference, DocumentReference documentReference)
    {
        return this.manager.getCache(userReference, documentReference);
    }
}
