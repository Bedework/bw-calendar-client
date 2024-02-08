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

import org.bedework.appcommon.ClientError;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.AdminBwModule;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.webcommon.BwRequest;

/** This action switches the users Admin Groups.
 * ADMIN ONLY
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class SwitchAGAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl,
                      final BwAdminActionForm form) {
    /* ============================================================
     *              Selecting a group - any access if no group set
     * ============================================================ */

    /* Check access - no don't

    if (!form.getCurUserContentAdminUser()) {
      return forwardNoAccess;
    }
    */

    cl.setGroupSet(false);
    cl.setChoosingGroup(false);

    cl.refreshAdminGroups();

    form.setNotificationInfo(null);

    // Back to main menu. Abstract action will do the rest.

    final int temp = ((AdminBwModule)request.getModule())
            .checkGroup(request, false);
    if (temp == forwardNoAction) {
      request.error(ClientError.chooseGroupSuppressed);
      return forwardError;
    }

    return forwardContinue;
  }
}

