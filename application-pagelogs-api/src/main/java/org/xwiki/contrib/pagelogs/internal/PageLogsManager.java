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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.context.Execution;
import org.xwiki.logging.LogQueue;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.xpn.xwiki.XWikiContext;

@Component(roles = { PageLogsManager.class })
@Singleton
public class PageLogsManager implements Initializable
{
    @Inject
    private CacheManager cacheManager;

    @Inject
    private Logger logger;

    @Inject
    private Execution execution;

    @Inject
    private EntityReferenceSerializer entityReferenceSerializer;

    private Cache<LogQueue> logQueueCache;

    @Override
    public void initialize()
    {
        CacheConfiguration cacheConfiguration = new CacheConfiguration("pagelogs.logs");

        try {
            this.logQueueCache = this.cacheManager.createNewCache(cacheConfiguration);
        } catch (CacheException e) {
            this.logger.error("Failed to create page logs cache [{}]", cacheConfiguration.getConfigurationId(), e);
        }
    }

    public LogQueue getCache(DocumentReference userReference, DocumentReference documentReference)
    {
        return this.logQueueCache.get(getCacheKey(userReference, documentReference));
    }

    public void addCacheEntry(LogQueue logQueue)
    {
        this.logQueueCache.set(getCacheKey(), logQueue);
    }

    private String getCacheKey()
    {
        return getCacheKey(getCurrentUserReference(), getCurrentDocumentReference());
    }

    private String getCacheKey(DocumentReference userReference, DocumentReference documentReference)
    {
        return String.format("%s/%s", this.entityReferenceSerializer.serialize(userReference),
            this.entityReferenceSerializer.serialize(documentReference));
    }

    private DocumentReference getCurrentDocumentReference()
    {
        return getXWikiContext().getDoc().getDocumentReference();
    }

    private DocumentReference getCurrentUserReference()
    {
        return getXWikiContext().getUserReference();
    }

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
    }

}