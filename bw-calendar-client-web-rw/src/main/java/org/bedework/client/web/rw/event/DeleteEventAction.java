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
package org.bedework.client.web.rw.event;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.base.exc.BedeworkAccessException;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.misc.Util;
import org.bedework.base.response.Response;
import org.bedework.webcommon.BwRequest;

import java.util.List;

import static org.bedework.client.web.rw.EventCommon.notifySubmitter;
import static org.bedework.webcommon.event.EventUtil.findEvent;

/**
 * Action to delete an event
 * <p>Request parameters:<ul>
 *      <li>  reference to an event (href)</li>.
 *      <li>  soft=true really delete - needs access: false mark only</li>.
 * </ul>
 * <p>Forwards to:<ul>
 *      <li>"noAction"     for guest mode.</li>
 *      <li>"success"      deleted ok.</li>
 * </ul>
 * <p>Errors:<ul>
 *      <li>org.bedework.message.deleted.events - when
 *            event is deleted - 1 parameter: count</li>
 *      <li>org.bedework.message.deleted.locations - when
 *            location is deleted - 1 parameter: count</li>
 *      <li>org.bedework.message.nosuchevent</li>
 * </ul>
 */
public class DeleteEventAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl) {
    final var form = getRwForm();
    final var publicAdmin = cl.getPublicAdmin();
    final var publicEvents = publicAdmin || cl.getWebSubmit();
    final var soft = request.getBooleanReqPar("soft", false) ||
            form.getMarkDeleted();

    String submitterEmail = null;

    final EventInfo ei = findEvent(request, Rmode.entityOnly,
                                   getLogger());

    if (ei == null) {
      // Do nothing
      return forwardNoAction;
    }

    final BwEvent ev = ei.getEvent();

    if (publicAdmin) {
      // We might need the submitters info */

      final List<BwXproperty> xps = ev.getXproperties(BwXproperty.bedeworkSubmitterEmail);

      if (!Util.isEmpty(xps)) {
        submitterEmail = xps.get(0).getValue();
      }
    }

    boolean deleted = false;

    if (!soft) {
        final Response resp = cl.deleteEvent(ei, !publicEvents);

        if (resp.getStatus() == Response.Status.notFound) {
          request.error(ClientError.unknownEvent);
          return forwardNoAction;
        }

        if (resp.isOk()) {
          deleted = true;
        }

        // else No access to delete - make it a soft delete
    }

    if (!deleted) {
      if (publicEvents) {
        ei.getChangeset(cl.getCurrentPrincipalHref()).
                changed(PropertyInfoIndex.DELETED,
                        ev.getDeleted(),
                        true);
        ev.setDeleted(true);

        final var ueres = cl.updateEvent(ei, true, null, false);
        if (!ueres.isOk()) {
          if (ueres.getStatus() == Response.Status.noAccess) {
            request.error(ClientError.noAccess);
          } else if (ueres.getException() != null) {
            request.error(ClientError.exc, ueres.getException());
          } else {
            request.error(ueres.getMessage());
          }
          return forwardNoAction;
        }
      } else {
        try {
        /* Can't really delete it - try annotating it */
          cl.markDeleted(ev);
        } catch (final BedeworkAccessException ignored) {
          request.error(ClientError.noAccess);
          return forwardNoAction;
        }
      }
    }

    if (publicAdmin &&
        request.getBooleanReqPar("submitNotification", false)) {
      notifySubmitter(request, ei, submitterEmail);
    }

    request.message(ClientMessage.deletedEvents, 1);
    request.refresh();
    cl.clearSearchEntries();

    return forwardSuccess;
  }
}
