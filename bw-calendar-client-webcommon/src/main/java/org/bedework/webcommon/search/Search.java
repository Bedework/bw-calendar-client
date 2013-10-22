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
import org.bedework.calsvci.indexing.SearchResult;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

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
    String query = request.getReqPar("query");
    String filter = request.getReqPar("filter");

    Client cl = request.getClient();

    if (cl.getPublicAdmin() || cl.isGuest()) {
      publick = true;
    } else {
      publick = request.present("public");
    }

    if (query == null) {
      return forwardNoAction;
    }

    String start = null;
    String end = null;

    String lim = form.getSearchLimits();
    if (lim != null) {
      if ("none".equals(lim)) {
        // no limits
      } else if ("beforeToday".equals(lim)) {
        end = DateTimeUtil.isoDate(DateTimeUtil.yesterday());
      } else if ("fromToday".equals(lim)) {
        start = DateTimeUtil.isoDate(new java.util.Date());
      }
    }

    SearchResult sres = cl.search(publick, query, filter, start, end);

    int pageSize = cl.getPreferences().getPageSize();

    form.setResultSize((int)sres.getFound());
    form.setResultStart(0);
    form.setResultCt(pageSize);
    form.setQuery(query);

    if (sres.getFound() == 0) {
      form.setCurPage(0);
      form.setNumPages(0);
    } else {
      form.setCurPage(1);
      form.setNumPages((((int)sres.getFound() + pageSize) - 1) / pageSize);
    }

    /* Ensure we have categories embedded in session */
    request.getSess().embedCategories(request, false);

    return forwardSuccess;
  }
}
