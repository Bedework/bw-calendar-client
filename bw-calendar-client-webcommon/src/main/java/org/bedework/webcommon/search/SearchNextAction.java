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
import org.bedework.calsvci.indexing.BwIndexer.Position;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

/**
 * Action to move to prev/next in a search.
 *
 * <p>Request parameters<ul>
 *      <li>"offset"         Move to given offset.</li>
 *      <li>"next"           Next page (default)</li>
 *      <li>"prev"           Previous page</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"success"      ok.</li>
 * </ul>

 * @author Mike Douglass
 */
public class SearchNextAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();

    int offset = request.getIntReqPar("offset", -1);

    if (offset > 0) {
      request.setRequestAttr(BwRequest.bwSearchListName,
                             cl.getSearchResult(offset,
                                                cl.getSearchParams()
                                                        .getPageSize()));
    } else {
      Position pos = Position.current;

      if (request.present("next")) {
        pos = Position.next;
      } else if (request.present("prev")) {
        pos = Position.previous;
      }

        request.setRequestAttr(BwRequest.bwSearchListName,
                             cl.getSearchResult(pos));
    }

    /* Ensure we have categories embedded in session */
    request.getSess().embedCategories(request, false,
                                      BwSession.ownersEntity);

    return forwardSuccess;
  }
}
