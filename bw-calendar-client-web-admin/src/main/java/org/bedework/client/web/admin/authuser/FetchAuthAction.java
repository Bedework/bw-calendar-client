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
package org.bedework.client.web.admin.authuser;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.webcommon.BwRequest;

/** This action fetches an authorised user
 * ADMIN ONLY
 *
 * <p>Forwards to:<ul>
 *      <li>forwardNoAccess     user not authorised.</li>
 *      <li>forwardNotFound     no such user.</li>
 *      <li>forwardContinue     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FetchAuthAction extends AdminActionBase {
  @Override
  public String doAction(final BwRequest request,
                         final AdminClient cl) {
    /* Check access
     */
    if (!cl.isSuperUser()) {
      return forwardNoAccess;
    }

    /* User requested a user from the list or by entering the id.
     */
    final String userId = request.getReqPar("editAuthUserId");

    final BwAuthUser au = cl.getAuthUser(cl.getUserAlways(userId));

    if (au == null) {
      request.error(ClientError.unknownUserid, userId);
      return forwardNotFound;
    }

    if (debug()) {
      info("Retrieved auth user " + au);
    }

    getAdminForm().setEditAuthUser(au);

    return forwardContinue;
  }
}

