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
package org.bedework.webcommon.event;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.exc.CalFacadeAccessException;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import edu.rpi.sss.util.Util;

import java.util.List;

/**
 * Action to delete an event
 * <p>Request parameters:<ul>
 *      <li>  guid  - uid for event or...</li>.
 *      <li>  eventName  - name for event</li>.
 *      <li>  recurrenceId  - for event instance</li>.
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
public class DeleteEventAction extends EventActionBase {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAction;
    }

    boolean publicAdmin = getPublicAdmin(form);
    boolean submitApp = form.getSubmitApp();
    boolean publicEvents = publicAdmin || submitApp;

    String submitterEmail = null;
    Client cl = form.fetchClient();

    EventInfo ei = findEvent(request, Rmode.entityOnly);

    if (ei == null) {
      // Do nothing
      form.getErr().emit(ClientError.unknownEvent);
      return forwardNoAction;
    }

    BwEvent ev = ei.getEvent();

    if (publicAdmin) {
      // We might need the submitters info */

      List<BwXproperty> xps = ev.getXproperties(BwXproperty.bedeworkSubmitterEmail);

      if (!Util.isEmpty(xps)) {
        submitterEmail = xps.iterator().next().getValue();
      }
    }

    try {
      if (!cl.deleteEvent(ei, !publicEvents)) {
        form.getErr().emit(ClientError.unknownEvent);
        return forwardNoAction;
      }
    } catch (CalFacadeAccessException cfe) {
      if (publicEvents) {
        ev.setDeleted(true);
        try {
          cl.updateEvent(ei, true, null);
        } catch (CalFacadeAccessException cfe1) {
          form.getErr().emit(ClientError.noAccess);
          return forwardNoAction;
        }
      } else {
        try {
          /* Can't really delete it - try annotating it */
          cl.markDeleted(ev);
        } catch (CalFacadeAccessException cfe1) {
          form.getErr().emit(ClientError.noAccess);
          return forwardNoAction;
        }
      }
    }

    if (publicAdmin &&
        request.getBooleanReqPar("submitNotification", false)) {
      notifySubmitter(ei, submitterEmail, form);
    }

    form.getMsg().emit(ClientMessage.deletedEvents, 1);
    form.refreshIsNeeded();

    return forwardSuccess;
  }
}
