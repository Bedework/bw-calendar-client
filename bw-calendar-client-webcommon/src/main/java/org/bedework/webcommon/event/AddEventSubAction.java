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
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCalendar.EventListEntry;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calsvci.CalSvcI;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.EventKey;

import java.util.SortedSet;

/** Action to add an event to an event list collection.
 * <p>Request parameters<ul>
 *      <li>"subColPath"  optional path to event list collection .</li>
 *      <li>"href"  calendar for event.</li>
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
public class AddEventSubAction extends EventActionBase {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    int fwd = addEventSub(request, form);
    if (fwd != forwardSuccess) {
      return fwd;
    }

    String start = form.getEvent().getDtstart().getDate().substring(0, 8);
    gotoDateView(form, start, BedeworkDefs.dayView);

    form.refreshIsNeeded();

    return forwardSuccess;
  }

  /** Add an event subscription. The collection to add it to is defined by the request
   * parameter newColPath.
   *
   * @param request
   * @param form
   * @return int forward index sucess for OK or an error index.
   * @throws Throwable
   */
  private int addEventSub(final BwRequest request,
                          final BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAction;
    }

    CalSvcI svci = form.fetchSvci();
    EventKey ekey = form.getEventKey();

    if (ekey == null) {
      return forwardNoAction;
    }

    EventInfo ei = findEvent(ekey, form);

    if (ei == null) {
      // Do nothing
      return forwardEventNotFound;
    }

    String subColPath = request.getReqPar("subColPath");
    BwCalendar col = null;

    if (subColPath != null) {
      /* See if the collection exists and is an event list collection */

      col = svci.getCalendarsHandler().get(subColPath);

      if (col == null) {
        // Not correct forward
        form.getErr().emit(ClientError.unknownCalendar);
        return forwardNoAction;
      }

      if (col.getCalType() != BwCalendar.calTypeEventList) {
        form.getErr().emit(ClientError.badRequest);
        return forwardNoAction;
      }
    } else {
      /* See if we have a default "referenced" collection in the calendar home */
      col = svci.getCalendarsHandler().getSpecial(BwCalendar.calTypeEventList, true);
    }

    /* add the href to the collection */

    SortedSet<EventListEntry> refs = col.getEventList();

    String href = request.getReqPar("href");
    if (request.getReqPar("recurrenceId") != null) {
      href += "#" + request.getReqPar("recurrenceId");
    }

    refs.add(new EventListEntry(href));

    col.setEventList(refs);

    svci.getCalendarsHandler().update(col);

    return forwardSuccess;
  }
}
