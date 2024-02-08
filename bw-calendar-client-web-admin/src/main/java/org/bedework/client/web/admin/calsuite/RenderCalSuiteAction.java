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
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.webcommon.BwRequest;

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
public class RenderCalSuiteAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl,
                      final BwAdminActionForm form) {
    final String name = form.getEditCalSuiteName();

    if (name == null) {
      request.error(ValidationError.missingName);
      return forwardRetry;
    }

    final BwCalSuiteWrapper cs = cl.getCalSuite(name);

    if (cs == null) {
      request.error(ClientError.unknownCalendarSuite);
      return forwardNotFound;
    }

    cs.setContext(null);
    cs.setDefaultContext(false);
    form.setCalSuite(cs);

    final String reqpar = request.getReqPar("delete");

    if (reqpar != null) {
      return forwardDelete;
    }

    return forwardSuccess;
  }
}
