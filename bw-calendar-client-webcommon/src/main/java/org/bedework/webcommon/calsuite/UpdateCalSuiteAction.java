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

import org.bedework.appcommon.AccessXmlUtil;
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.SysparsI;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import edu.rpi.cmt.access.Acl;

/** Update a calendar suite for a user.
 *
 * <p>Parameters are:<ul>
 *      <li>"delete"                   delete current calsuite</li>
 *      </ul>or<<ul>
 *      <li>"name"            Name of calsuite to update</li>
 *      <li>"groupName"       group name for calsuite</li>
 *      <li>"calPath"         root collection path</li>
 *      <li>"subroot"         submissions root path</li>
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
public class UpdateCalSuiteAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }

    CalSvcI svc = form.fetchSvci();

    BwCalSuiteWrapper csw = form.getCalSuite();

    if (csw == null) {
      return forwardError;
    }

    if (request.present("delete")) {
      svc.getCalSuitesHandler().delete(csw);
      // After deleting the calendar suite, make sure to remove that suite's
      // sub-context if it has one.
      SysparsI sysi = form.fetchSvci().getSysparsHandler();
      BwSystem syspars = sysi.get(sysi.getSystemName());
      String ctxName = csw.getContext();
      if (ctxName != null) {
        CalSuiteContextHelper contextHelper = new CalSuiteContextHelper(syspars);
        if (contextHelper.removeSuiteContext(ctxName)) {
          svc.getSysparsHandler().update(syspars);
        }
      }
      return forwardSuccess;
    }

    svc.getCalSuitesHandler().update(csw,
                                     request.getReqPar("groupName"),
                                     request.getReqPar("calPath"),
                                     request.getReqPar("subroot"));

    /* -------------------------- Access ------------------------------ */

    String aclStr = request.getReqPar("acl");
    if (aclStr != null) {
      Acl acl = new AccessXmlUtil(null, svc).getAcl(aclStr, true);

      svc.changeAccess(csw, acl.getAces(), true);
    }

    /* -------------------------- Context ----------------------------- */
    SysparsI sysi = form.fetchSvci().getSysparsHandler();
    BwSystem syspars = sysi.get(sysi.getSystemName());
    CalSuiteContextHelper contextHelper = new CalSuiteContextHelper(syspars);
    String newContextName = request.getReqPar("context");
    boolean newDefContext = "true".equals(request.getReqPar("defaultContext"));
    if (contextHelper.updateSuiteContext(csw, newContextName, newDefContext)) {
      svc.getSysparsHandler().update(syspars);
    }
    return forwardSuccess;
  }
}
