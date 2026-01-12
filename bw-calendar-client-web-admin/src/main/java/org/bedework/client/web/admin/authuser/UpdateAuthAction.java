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

import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.webcommon.BwRequest;

/** This action updates an authorised user
 * ADMIN ONLY
 *
 * <p>Forwards to:<ul>
 *      <li>forwardNoAccess     user not authorised.</li>
 *      <li>forwardRetry        invalid entitly.</li>
 *      <li>forwardContinue     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class UpdateAuthAction extends AdminActionBase {
  @Override
  public String doAction(final BwRequest request,
                         final AdminClient cl) {
    /* Check access
     */
    if (!cl.isSuperUser()) {
      return forwardNoAccess;
    }

    /* We are just updating from the current form values.
     */
    final BwAuthUser au = getAdminForm().getEditAuthUser();

    if (au == null) {
      return forwardRetry;
    }

    au.setUsertype(getAdminForm().getEditAuthUserType());

    if (debug()) {
      debug("Update authUser " + au);
    }

    cl.updateAuthUser(au);

    request.message(ClientMessage.updatedAuthuser);

    return forwardContinue;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setEditAuthUserPublicEvents(final boolean val) {
    getAdminForm().setEditAuthUserPublicEvents(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setEditAuthUserContentAdmin(final boolean val) {
    getAdminForm().setEditAuthUserContentAdmin(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setEditAuthUserApprover(final boolean val) {
    getAdminForm().setEditAuthUserApprover(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public boolean getEditAuthUserPublicEvents() {
    return getAdminForm().getEditAuthUserPublicEvents();
  }

  @SuppressWarnings("UnusedDeclaration")
  public boolean getEditAuthUserContentAdmin() {
    return getAdminForm().getEditAuthUserContentAdmin();
  }

  @SuppressWarnings("UnusedDeclaration")
  public boolean getEditAuthUserApprover() {
    return getAdminForm().getEditAuthUserApprover();
  }
}

