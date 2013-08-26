/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.webcommon.search;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import edu.rpi.cct.misc.indexing.SearchLimits;

/**
 * Action to carry out a search.
 *
 * <p>Request parameters<ul>
 *      <li>"public"         Present if we are to search public events.</li>
 *      <li>"user"           user to search otherwise</li>
 *      <li>"query"          the query.</li>
 *      <li>"searchLimits"         "beforeToday", "fromToday", "none"</li>
 * </ul>
 *
 * <p>If only query is present wil search current user for non-public client
 * or public events for public client.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAction"     no query.</li>
 *      <li>"success"      ok.</li>
 * </ul>

 * @author Mike Douglass
 */
public class Search extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    boolean publick;
    String userStr = null;
    String query = request.getReqPar("query");

    Client cl = request.getClient();

    if (cl.getPublicAdmin() || cl.isGuest()) {
      publick = true;
    } else {
      publick = request.getReqPar("public") != null;
      userStr = request.getReqPar("user");
    }

    if (query == null) {
      return forwardNoAction;
    }

    SearchLimits limits = null;

    String lim = form.getSearchLimits();
    if (lim != null) {
      if ("none".equals(lim)) {
        // no limits
      } else if ("beforeToday".equals(lim)) {
        limits = cl.beforeToday();
      } else if ("fromToday".equals(lim)) {
        limits = cl.fromToday();
      }
    }

    String principal = null;
    if (!publick && (userStr != null)) {
      BwPrincipal p = cl.getUser(userStr);
      if (p == null) {
        // Ignore the request - probing for users?
        return forwardNoAction;
      }

      principal = p.getPrincipalRef();
    }

    long rsize = cl.search(publick, principal, query, limits);
    int pageSize = cl.getPreferences().getPageSize();

    form.setResultSize((int)rsize);
    form.setResultStart(0);
    form.setResultCt(pageSize);
    form.setQuery(query);

    if (rsize == 0) {
      form.setCurPage(0);
      form.setNumPages(0);
    } else {
      form.setCurPage(1);
      form.setNumPages((((int)rsize + pageSize) - 1) / pageSize);
    }

    return forwardSuccess;
  }
}
