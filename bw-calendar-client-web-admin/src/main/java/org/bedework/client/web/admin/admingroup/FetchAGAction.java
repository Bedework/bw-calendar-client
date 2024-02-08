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
package org.bedework.client.web.admin.admingroup;

import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.webcommon.BwRequest;

/** This action fetches an admin group
 * ADMIN ONLY
 *
 * <p>Forwards to:<ul>
 *      <li>forwardNoAccess     user not authorised.</li>
 *      <li>forwardNotFound     no such event.</li>
 *      <li>forwardContinue     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FetchAGAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl,
                      final BwAdminActionForm form) {
    /* Check access
     */
    if (!cl.isSuperUser()) {
      return forwardNoAccess;
    }

    cl.setChoosingGroup(false); // reset

    /* User requested an admin group from the list or by entering the name.
     */
    final String account = request.getReqPar("adminGroupName");
    if (account == null) {
      return forwardNotFound;
    }

    final BwAdminGroup ag = cl.findAdminGroup(account);

    if (debug()) {
      if (ag == null) {
        info("No group with name " + account);
      } else {
        info("Retrieved admin group " + ag.getAccount());
      }
    }

    if (ag == null) {
      return forwardNotFound;
    }

    cl.getAdminGroupMembers(ag);
    form.setUpdAdminGroup(ag);
    form.assignAddingAdmingroup(false);

    return forwardContinue;
  }
}

