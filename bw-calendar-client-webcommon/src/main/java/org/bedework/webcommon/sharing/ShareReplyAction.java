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
package org.bedework.webcommon.sharing;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.sharing.InviteNotificationType;
import org.bedework.caldav.util.sharing.InviteReplyType;
import org.bedework.calfacade.exc.CalFacadeForbidden;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** This action sends a sharing reply for the invite identified by the name.
 *
 * <p>Parameters are:</p>
 * <ul>
 *      <li>"name"                Of notification message</li>
 *      <li>"accept"              true/false</li>
 *      <li>"colName"             name of collection</li>
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
 * @author Mike Douglass   douglm@bedework.edu
 */
public class ShareReplyAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();

    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defense
    }

    NotificationType note = cl.findNotification(request.getReqPar("name"));

    if (note == null) {
      return forwardNotFound;
    }

    if (!(note.getNotification() instanceof InviteNotificationType)) {
      request.getErr().emit(ClientError.badRequest, "Not an invite");
      return forwardError;
    }

    InviteNotificationType invite = (InviteNotificationType)note.getNotification();

    Boolean accept = request.getBooleanReqPar("accept");
    if (accept == null) {
      request.getErr().emit(ClientError.badRequest, "Missing accept");
      return forwardError;
    }

    String colName = null;
    if (accept) {
      colName = request.getReqPar("colName");
      if (colName == null) {
        request.getErr().emit(ClientError.badRequest, "Missing colName");
        return forwardError;
      }
    }

    InviteReplyType reply = new InviteReplyType();

    reply.setHref(invite.getHref());
    reply.setAccepted(accept);
    reply.setHostUrl(invite.getHostUrl());
    reply.setInReplyTo(invite.getUid());
    reply.setSummary(colName);

    boolean error = false;

    try {
      cl.sharingReply(reply);
    } catch (CalFacadeForbidden cf) {
      request.getErr().emit(cf.getMessage());
      error = true;
    }

    cl.removeNotification(note);
    form.setNotificationInfo(null); // force a refresh
    cl.flushState();

    if (error) {
      return forwardError;
    }

    return forwardContinue;
  }
}

