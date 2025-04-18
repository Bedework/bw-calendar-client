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

package org.bedework.webcommon.admingroup;

import org.bedework.calfacade.responses.AdminGroupsResponse;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwRequest;

/** This action fetches admin groups and writes them as a json object.
 * READ-ONLY + ADMIN
 *
 * <p>Forwards to:<ul>
 *      <li>null     always.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm   spherical cow
 */
public class FetchAdminGroupsAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request) {
    request.setNocache(false);

    if (!request.contentChanged()) {
      return forwardNull;
    }

    final var cl = request.getClient();
    final var vals = cl.getAdminGroups();

    final var adgs = new AdminGroupsResponse()
            .setGroups(vals).ok();

    outputJson(request.getResponse(),
               cl.getCurrentChangeToken(),
               null, adgs);

    return forwardNull;
  }
}
