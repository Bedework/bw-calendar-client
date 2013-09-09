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
package org.bedework.webcommon.calsuite;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** Add a new calendar suite.
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
 * @author Mike Douglass   douglm@bedework.edu
 */
public class AddCalSuiteAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();

    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

    String name = request.getReqPar("name");

    if (name == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardNotAdded;
    }

    if (name.length() > BwCalSuite.maxNameLength) {
      form.getErr().emit(ValidationError.tooLongName);
      return forwardNotAdded;
    }

    String groupName = request.getReqPar("groupName");

    if (groupName == null) {
      form.getErr().emit(ValidationError.missingGroupName);
      request.setErrFlag(true);
      return forwardNotAdded;
    }

    BwCalSuiteWrapper suite = cl.addCalSuite(name, groupName,
                                      request.getReqPar("calPath"),
                                      request.getReqPar("subroot"));
    if (suite == null) {
      form.getErr().emit(ClientError.calsuiteNotAdded);
      return forwardNotAdded;
    }

    /* -------------------------- Context ----------------------------- */
    BwSystem syspars = cl.getSyspars();
    CalSuiteContextHelper contextHelper = new CalSuiteContextHelper(syspars);
    String newContextName = request.getReqPar("context");
    boolean newDefContext = "true".equals(request.getReqPar("defaultContext"));

    if (contextHelper.updateSuiteContext(suite, newContextName, newDefContext)) {
      cl.updateSyspars(syspars);
    }

    form.setCalSuiteName(name);

    return forwardSuccess;
  }
}
