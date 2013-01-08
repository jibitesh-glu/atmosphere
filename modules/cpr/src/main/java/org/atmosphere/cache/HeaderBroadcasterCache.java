/*
 * Copyright 2011 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.atmosphere.cache;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceImpl;

import static org.atmosphere.cpr.HeaderConfig.X_CACHE_DATE;

/**
 * Simple header based {@link org.atmosphere.cpr.BroadcasterCache}. The returned header is "X-Cache-Date" and
 * containg the time, in milliseconds, of the last broadcasted message.
 *
 * By default, message will be cached for 5 minutes. You can change the value by calling {@link #setMaxCachedinMs(int)}
 *
 * @author Jeanfrancois Arcand
 */
public class HeaderBroadcasterCache extends AbstractBroadcasterCache {

    /**
     * {@inheritDoc}
     */
    public void cache(String id, AtmosphereResource ar, CachedMessage cm) {
        long time = cm.next() == null ? cm.currentTime() : cm.next().currentTime();

        AtmosphereResourceImpl r = AtmosphereResourceImpl.class.cast(ar);
        if (r != null && r.isInScope() && !r.getResponse().isCommitted()) {
            r.getResponse().addHeader(X_CACHE_DATE, String.valueOf(time));
        }
    }

    /**
     * {@inheritDoc}
     */
    public CachedMessage retrieveLastMessage(String id, AtmosphereResource ar) {
        AtmosphereResourceImpl r = AtmosphereResourceImpl.class.cast(ar);

        if (!r.isInScope()) return null;

        AtmosphereRequest request = r.getRequest();
        return retrieveUsingHeader(request.getHeader(X_CACHE_DATE));
    }

    public CachedMessage retrieveUsingHeader(final String dateString) {

        if (dateString == null) return null;

        long currentTime = 0;
        try {
            currentTime = Long.valueOf(dateString);
        } catch (NumberFormatException ex) {
            logger.trace("", ex);
            return null;
        }

        CachedMessage prev = null;
        for (CachedMessage cm : queue) {
            if (cm.currentTime() > currentTime) {
                return prev;
            }
            prev = cm;
        }
        return prev;
    }

}