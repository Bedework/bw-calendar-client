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
package org.bedework.webcommon;

import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.SearchParams;
import org.bedework.base.exc.BedeworkException;

import static org.bedework.webcommon.search.SearchUtil.setSearchParams;

/** Ensure the time view data is refreshed
 *
 * @author Mike Douglass  douglm - rpi.edu
 */
public class RenderMainAction extends RenderAction {
  @Override
  public String doAction(final BwRequest request) {
    final var mstate = request.getModule().getState();
    final var cl = request.getClient();
    final var tv = mstate.getCurTimeView();
    var fetch = mstate.getRefresh();

    var params = cl.getSearchParams();

    if (params == null) {
      /* Set up the search parameters */

      params = new SearchParams();

      if (tv != null) {
        params.setFromDate(tv.getViewStart());
        params.setToDate(tv.getViewEnd());
      }

      final var forward =
              setSearchParams(request, params,
                              Client.gridViewMode.equals(cl.getViewMode()));

      if (!forwardSuccess.equals(forward)) {
        return forward;
      }

      if (tv != null) {
        tv.refreshEvents();
      }

      fetch = true;
    }

    request.setRequestAttr(BwRequest.bwSearchParamsName, params);

    if (fetch) {
      /* Do the search */
      try {
        mstate.setSearchResult(cl.search(params));
        request.setRequestAttr(BwRequest.bwSearchResultName,
                               mstate.getSearchResult());
      } catch (final BedeworkException be) {
        request.error(be);
      }
    }

    if ((tv != null) && (fetch || !tv.hasEvents())) {
      tv.putEvents(cl.getSearchResult(0, -1));
    }

    mstate.setRefresh(false);

    return forwardSuccess;
  }
}

