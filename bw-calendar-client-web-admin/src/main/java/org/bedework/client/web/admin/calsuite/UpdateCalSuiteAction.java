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
package org.bedework.client.web.admin.calsuite;

import org.bedework.access.AccessException;
import org.bedework.access.Acl;
import org.bedework.appcommon.AccessXmlUtil;
import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.webcommon.BwRequest;

/** Update a calendar suite for a user.
 * ADMIN ONLY
 *
 * <p>Parameters are:<ul>
 *      <li>"delete"                   delete current calsuite</li>
 *      </ul>or<<ul>
 *      <li>"name"            Name of calsuite to update</li>
 *      <li>"groupName"       group name for calsuite</li>
 *      <li>"calPath"         root collection path</li>
 *      <li>"description"     </li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"retry"        try again.</li>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class UpdateCalSuiteAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl) {
    final BwCalSuiteWrapper csw = getAdminForm().getCalSuite();

    if (csw == null) {
      return forwardError;
    }

    if (request.present("delete")) {
      cl.deleteCalSuite(csw);

      return forwardSuccess;
    }

    cl.updateCalSuite(csw,
                      request.getReqPar("groupName"),
                      request.getReqPar("calPath"),
                      request.getReqPar("description"));

    /* -------------------------- Access ------------------------------ */

    final String aclStr = request.getReqPar("acl");
    if (aclStr != null) {
      try {
        final Acl acl = new AccessXmlUtil(null, cl).getAcl(aclStr,
                                                           true);

        cl.changeAccess(csw, acl.getAces(), true);
      } catch (final AccessException ae) {
        throw new BedeworkException(ae);
      }
    }

    return forwardSuccess;
  }
}
