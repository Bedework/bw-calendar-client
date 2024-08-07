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

import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.webcommon.BwRequest;

/** This action sets the state ready for updating an admingroup.
 * ADMIN ONLY
 *
 * <p>Forwards to:<ul>
 *      <li>forwardNoAccess     user not authorised.</li>
 *      <li>forwardContinue     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class InitUpdateAGAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl) {
    /* Check access
     */
    if (!cl.isSuperUser()) {
      return forwardNoAccess;
    }

    cl.refreshAdminGroups();
    request.embedAdminGroups();

    /* Set the objects to null so we get new ones.
     */
    getAdminForm().assignAddingAdmingroup(false);
    getAdminForm().assignCalSuites(request.getClient().getContextCalSuites());

    return forwardContinue;
  }
}

