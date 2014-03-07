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

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.webcommon.AdminUtil;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** This action switches the users Admin Groups.
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class SwitchAGAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    /** =================================================================
     *                   Selecting a group - any access if no group set
     *  ================================================================= */

    /** Check access - no don't

    if (!form.getCurUserContentAdminUser()) {
      return forwardNoAccess;
    }
    */

    final Client cl = request.getClient();

    cl.setGroupSet(false);
    cl.setChoosingGroup(false);

    cl.refreshAdminGroups();
    request.setSessionAttr(BwRequest.bwAdminGroupsInfoName,
                           cl.getAdminGroups());

    // Back to main menu. Abstract action will do the rest.

    final int temp = AdminUtil.checkGroup(request, false);
    if (temp == forwardNoAction) {
      form.getErr().emit(ClientError.chooseGroupSuppressed);
      return forwardError;
    }

    return forwardContinue;
  }
}

