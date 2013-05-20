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
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.SubContext;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.SysparsI;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.RenderAction;

import java.util.Set;

/** Fetch a calendar suite for update/display/delete. Name is in form
 * property calsuiteName.
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such subscription.</li>
 *      <li>"retry"        try again.</li>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class RenderCalSuiteAction extends RenderAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    CalSvcI svc = form.fetchSvci();

    String name = form.getCalSuiteName();

    if (name == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardRetry;
    }

    BwCalSuiteWrapper cs = svc.getCalSuitesHandler().get(name);

    if (cs == null) {
      form.getErr().emit(ClientError.unknownCalendarSuite);
      return forwardNotFound;
    }

    SubContext suiteCtx = null;
    SysparsI sysi = form.fetchSvci().getSysparsHandler();
    BwSystem syspars = sysi.get();
    Set<SubContext> contexts = syspars.getContexts();
    for (SubContext subContext : contexts) {
      if (subContext.getCalSuite().equals(cs.getName())) {
        suiteCtx = subContext;
      }
    }

    if (suiteCtx != null) {
      cs.setContext(suiteCtx.getContextName());
      cs.setDefaultContext(suiteCtx.getDefaultContext());
    } else {
      cs.setContext(null);
      cs.setDefaultContext(false);
    }
    form.setCalSuite(cs);

    String reqpar = request.getReqPar("delete");

    if (reqpar != null) {
      return forwardDelete;
    }

    return forwardSuccess;
  }
}
