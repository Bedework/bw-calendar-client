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
package org.bedework.client.web.rw.sharing;

import org.bedework.appcommon.ClientError;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.sharing.InviteNotificationType;
import org.bedework.caldav.util.sharing.InviteReplyType;
import org.bedework.calfacade.exc.CalFacadeForbidden;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.BwRWWebGlobals;
import org.bedework.client.web.rw.RWActionBase;
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
 * @author Mike Douglass   douglm@rpi.edu
 */
public class ShareReplyAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl) {
    final var globals = (BwRWWebGlobals)request.getGlobals();
    final NotificationType note = cl.findNotification(request.getReqPar("name"));

    if (note == null) {
      return forwardNotFound;
    }

    if (!(note.getNotification() instanceof InviteNotificationType)) {
      request.error(ClientError.badRequest, "Not an invite");
      return forwardError;
    }

    final InviteNotificationType invite =
            (InviteNotificationType)note.getNotification();

    final Boolean accept = request.getBooleanReqPar("accept");
    if (accept == null) {
      request.error(ClientError.badRequest, "Missing accept");
      return forwardError;
    }

    String colName = null;
    if (accept) {
      colName = request.getReqPar("colName");
      if (colName == null) {
        request.error(ClientError.badRequest, "Missing colName");
        return forwardError;
      }
    }

    final InviteReplyType reply = new InviteReplyType();

    reply.setHref(invite.getHref());
    reply.setAccepted(accept);
    reply.setHostUrl(invite.getHostUrl());
    reply.setInReplyTo(invite.getUid());
    reply.setSummary(colName);

    boolean error = false;

    try {
      cl.sharingReply(reply);
    } catch (final CalFacadeForbidden cf) {
      request.error(cf.getMessage());
      error = true;
    }

    cl.removeNotification(note);
    globals.setNotificationInfo(null); // force a refresh
    cl.flushState();

    if (error) {
      return forwardError;
    }

    return forwardContinue;
  }
}

