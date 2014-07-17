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

/** This is a no-op action
 *
 * @author Mike Douglass  douglm - rpi.edu
 */
public class MainAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    request.refresh();

    final Client cl = request.getClient();

    /* If not in list mode set up later */
    if (!Client.listViewMode.equals(cl.getViewMode())) {
      return forwardSuccess;
    }

    final BwModuleState mstate = request.getModule().getState();
    final TimeView tv = mstate.getCurTimeView();

    SearchParams params = cl.getSearchParams();

    if (params == null) {
      /* Set up the search parameters */

      params = new SearchParams();
      final int forward = setSearchParams(request, params, true);

      if (forward != forwardSuccess) {
        return forward;
      }

      if (tv != null) {
        tv.refreshEvents();
      }

      /* Do the search */
      mstate.setSearchResult(cl.search(params));
      request.setRequestAttr(BwRequest.bwSearchResultName,
                             mstate.getSearchResult());
    }

    if (tv != null) {
      tv.getEvents(cl, mstate.getRefresh());
    }

    mstate.setRefresh(false);

    return forwardListEvents;
  }
}
