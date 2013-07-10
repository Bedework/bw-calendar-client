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
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.calfacade.exc.CalFacadeAccessException;
import org.bedework.calfacade.exc.CalFacadeForbidden;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

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
public class RemoveAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }

    Client cl = form.fetchClient();

    NotificationType note = cl.findNotification(request.getReqPar("name"));

    if (note == null) {
      return forwardNotFound;
    }

    int forward;

    try {
      cl.removeNotification(note);
      forward = forwardSuccess;
    } catch (CalFacadeAccessException ca) {
      form.getErr().emit(ca.getMessage());
      forward = forwardNoAccess;
    } catch (CalFacadeForbidden cf) {
      form.getErr().emit(cf.getMessage());
      forward = forwardNoAccess;
    }

    form.setNotificationInfo(null); // force a refresh
    cl.flushState();

    return forward;
  }
}

