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

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.webcommon.BwRequest;

/** Add a new calendar suite.
 * ADMIN ONLY
 *
 * <p>Parameters are:<ul>
 *      <li>"name"             Name of suite</li>
 *      <li>"groupName"        Name of owning group.</li>
 *      <li>"calPath"          Path of root calendar.</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notAdded"     duplicate or bad name.</li>
 *      <li>"success"      created ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class AddCalSuiteAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl) {
    final String name = request.getReqPar("name");

    if (name == null) {
      request.error(ValidationError.missingName);
      return forwardNotAdded;
    }

    if (name.length() > BwCalSuite.maxNameLength) {
      request.error(ValidationError.tooLongName);
      return forwardNotAdded;
    }

    final String groupName = request.getReqPar("groupName");

    if (groupName == null) {
      request.error(ValidationError.missingGroupName);
      request.setErrFlag(true);
      return forwardNotAdded;
    }

    final BwCalSuiteWrapper suite =
            cl.addCalSuite(name, groupName,
                           request.getReqPar("calPath"),
                           request.getReqPar("description"));
    if (suite == null) {
      request.error(ClientError.calsuiteNotAdded);
      return forwardNotAdded;
    }

    return forwardSuccess;
  }
}
