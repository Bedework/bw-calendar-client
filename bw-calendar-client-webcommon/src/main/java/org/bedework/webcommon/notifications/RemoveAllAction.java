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
package org.bedework.webcommon.notifications;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.exc.CalFacadeAccessException;
import org.bedework.calfacade.exc.CalFacadeForbidden;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import javax.servlet.http.HttpServletResponse;

/** This action removes a notification identified by the name.
 *
 * <p>Parameters are:</p>
 * <ul>
 *      <li>"name"                Of notification message</li>
 * </ul>
 *
 * <p>Forwards to:</p>
 * <ul>
 *      <li>"noAccess"     user not authorized.</li>
 *      <li>"error"        for problems.</li>
 *      <li>"notFound"     no such notification (may have been handled by
 *                         another client).</li>
 *      <li>"success"      all done.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class RemoveAllAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();
    final HttpServletResponse response = request.getResponse();

    if (cl.isGuest()) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return forwardNull;
    }

    final String principalHref;

    if (cl.getPublicAdmin()) {
      principalHref = cl.getCalSuite().getGroup().getOwnerHref();
    } else {
      principalHref = cl.getCurrentPrincipalHref();
    }

    int status;

    try {
      cl.removeAllNotifications(principalHref);
      status = HttpServletResponse.SC_OK;
    } catch (final CalFacadeAccessException ca) {
      form.getErr().emit(ca.getMessage());
      status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    } catch (final CalFacadeForbidden cf) {
      form.getErr().emit(cf.getMessage());
      status = HttpServletResponse.SC_FORBIDDEN;
    }

    form.setNotificationInfo(null); // force a refresh
    cl.flushState();

    response.setStatus(status);
    return forwardNull;
  }
}

