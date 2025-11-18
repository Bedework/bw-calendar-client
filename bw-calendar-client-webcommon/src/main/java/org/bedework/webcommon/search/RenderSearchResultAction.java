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
import org.bedework.calfacade.indexing.BwIndexer.Position;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;
import org.bedework.webcommon.RenderAction;

/** Implant the first segment of the search result in the form.
 *
 * <p>Forwards to:<ul>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class RenderSearchResultAction extends RenderAction {
  @Override
  public int doAction(final BwRequest request) {
    final Client cl = request.getClient();

    request.setRequestAttr(BwRequest.bwSearchListName,
                           cl.getSearchResult(Position.current));

    /* Ensure we have categories embedded in session */
    request.getSess().embedCategories(request, false,
                                      BwSession.ownersEntity);

    return forwardSuccess;
  }
}
