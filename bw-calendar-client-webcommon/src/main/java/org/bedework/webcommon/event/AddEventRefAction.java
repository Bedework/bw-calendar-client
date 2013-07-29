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

import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventProxy;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.EventKey;

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
public class AddEventRefAction extends EventActionBase {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    int fwd = addEventRef(request, form);
    if (fwd != forwardSuccess) {
      return fwd;
    }

    String start = form.getEvent().getDtstart().getDate().substring(0, 8);
    gotoDateView(form, start, BedeworkDefs.dayView);

    form.refreshIsNeeded();

    return forwardSuccess;
  }

  /** Add an event ref. The calendar to add it to is defined by the request
   * parameter newCalPath.
   *
   * @param request
   * @param form
   * @return int forward index sucess for OK or an error index.
   * @throws Throwable
   */
  private int addEventRef(final BwRequest request,
                          final BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();

    /** Check access
     */
    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

//    EventInfo ei = findEvent(request, Rmode.masterOnly);

    EventKey ekey = form.getEventKey();

    if (ekey == null) {
      return forwardNoAction;
    }

    EventInfo ei = findEvent(ekey, form);

    if (ei == null) {
      // Do nothing
      return forwardEventNotFound;
    }

    /* Create an event to act as a reference to the targeted event and copy
     * the appropriate fields from the target
     */
    BwEventProxy proxy = BwEventProxy.makeAnnotation(ei.getEvent(),
                                                     ei.getEvent().getOwnerHref(),
                                                     false);

    EventInfo eref = new EventInfo(proxy);
    form.setEventInfo(eref); // Make it available

    String calPath = getReqPar(request.getRequest(), "newCalPath");

    if (calPath == null) {
      calPath = cl.getPreferredCollectionPath();
    }
    proxy.setOwnerHref(cl.getCurrentPrincipalHref());

    String transparency = request.getReqPar("transparency");
    if (transparency != null) {
      if (!BwEvent.validTransparency(transparency)) {
        form.getErr().emit(ValidationError.invalidTransparency, transparency);
        return forwardBadValue;
      }

      proxy.setTransparency(transparency);
    }

    eref.getEvent().setColPath(calPath);

    try {
      cl.addEvent(eref, true, false, false);
      form.getMsg().emit(ClientMessage.addedEventrefs, 1);
    } catch (CalFacadeException cfe) {
      if (CalFacadeException.duplicateGuid.equals(cfe.getMessage())) {
        form.getErr().emit(ClientError.duplicateUid);
        return forwardDuplicate;
      }

      if (CalFacadeException.collectionNotFound.equals(cfe.getMessage())) {
        form.getErr().emit(ValidationError.missingCalendar);
        return forwardDuplicate;
      }

      throw cfe;
    }

    return forwardSuccess;
  }
}
