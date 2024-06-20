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

import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.EventKey;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventProxy;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.webcommon.BwRequest;

/** Action to add an event alias to a personal calendar.
 * <p>Request parameters<ul>
 *      <li>"subid"    subscription id for event.</li>
 *      <li>"calPath"  calendar for event.</li>
 *      <li>"guid"     guid of event.</li>
 *      <li>"recurrenceId"   recurrence-id of event instance - possibly null.</li>
 * </ul>
 * <p>Forwards to:<ul>
 *      <li>"noAction"    when request seems wrong.</li>
 *      <li>"eventNotFound"  no such event.</li>
 *      <li>"calendarNotFound"  no such target calendar.</li>
 *      <li>"duplicate"    duplicate guid.</li>
 *      <li>"success"      added ok.</li>
 * </ul>
 */
public class AddEventRefAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    final int fwd = addEventRef(request, cl, form);
    if (fwd != forwardSuccess) {
      return fwd;
    }

    final String start = form.getEvent().getDtstart().getDate().substring(0, 8);
    gotoDateView(request, start,
                 BedeworkDefs.vtDay);

    request.refresh();

    return forwardSuccess;
  }

  /** Add an event ref. The calendar to add it to is defined by the request
   * parameter newCalPath.
   *
   * @param request bedework request object
   * @param form action form
   * @return int forward index sucess for OK or an error index.
   */
  private int addEventRef(final BwRequest request,
                          final RWClient cl,
                          final BwRWActionForm form) {
//    EventInfo ei = findEvent(request, Rmode.masterOnly);

    final EventKey ekey = form.getEventKey();

    if (ekey == null) {
      return forwardNoAction;
    }

    final EventInfo ei = findEvent(request, ekey);

    if (ei == null) {
      // Do nothing
      return forwardEventNotFound;
    }

    /* Create an event to act as a reference to the targeted event and copy
     * the appropriate fields from the target
     */
    final BwEventProxy proxy =
            BwEventProxy.makeAnnotation(ei.getEvent(),
                                        ei.getEvent().getOwnerHref(),
                                        false);

    final EventInfo eref = new EventInfo(proxy);
    form.setEventInfo(eref, false); // Make it available

    String calPath = request.getReqPar("newCalPath");

    if (calPath == null) {
      final String icalName = IcalDefs.entityTypeIcalNames[proxy.getEntityType()];

      calPath = cl.getPreferredCollectionPath(icalName);
    }
    proxy.setOwnerHref(cl.getCurrentPrincipalHref());

    final String transparency = request.getReqPar("transparency");
    if (transparency != null) {
      if (!BwEvent.validTransparency(transparency)) {
        request.error(ValidationError.invalidTransparency, transparency);
        return forwardBadValue;
      }

      proxy.setTransparency(transparency);
    }

    eref.getEvent().setColPath(calPath);

    try {
      cl.addEvent(eref, true, false);
      request.message(ClientMessage.addedEventrefs, 1);
    } catch (final RuntimeException rte) {
      if (CalFacadeException.duplicateGuid.equals(rte.getMessage())) {
        request.error(ClientError.duplicateUid);
        return forwardDuplicate;
      }

      if (CalFacadeException.collectionNotFound.equals(rte.getMessage())) {
        request.error(ValidationError.missingCalendar);
        return forwardDuplicate;
      }

      throw rte;
    }

    return forwardSuccess;
  }
}
