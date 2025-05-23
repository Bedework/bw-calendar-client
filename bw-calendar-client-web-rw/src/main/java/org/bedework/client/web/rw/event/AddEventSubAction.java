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
import org.bedework.appcommon.EventKey;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.EventListEntry;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

import java.util.SortedSet;

import static org.bedework.webcommon.DateViewUtil.gotoDateView;
import static org.bedework.webcommon.event.EventUtil.findEvent;

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
public class AddEventSubAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl) {
    final int fwd = addEventSub(request, cl);
    if (fwd != forwardSuccess) {
      return fwd;
    }

    final var start = getRwForm().getEvent().getDtstart().getDate().substring(0, 8);
    gotoDateView(request, start,
                 BedeworkDefs.vtDay);

    request.refresh();

    return forwardSuccess;
  }

  /* Add an event subscription. The collection to add it to is defined by the request
   * parameter newColPath.
   *
   * returns int forward index sucess for OK or an error index.
   */
  private int addEventSub(final BwRequest request,
                          final RWClient cl) {
    final EventKey ekey = getRwForm().getEventKey();

    if (ekey == null) {
      return forwardNoAction;
    }

    final EventInfo ei = findEvent(request, ekey, getLogger());

    if (ei == null) {
      // Do nothing
      return forwardEventNotFound;
    }

    final String subColPath = request.getReqPar("subColPath");
    final BwCollection col;

    if (subColPath != null) {
      /* See if the collection exists and is an event list collection */

      col = cl.getCollection(subColPath);

      if (col == null) {
        // Not correct forward
        request.error(ClientError.unknownCalendar);
        return forwardNoAction;
      }

      if (col.getCalType() != BwCollection.calTypeEventList) {
        request.error(ClientError.badRequest);
        return forwardNoAction;
      }
    } else {
      /* See if we have a default "referenced" collection in the calendar home */
      col = cl.getSpecial(BwCollection.calTypeEventList, true);
    }

    /* add the href to the collection */

    final SortedSet<EventListEntry> refs = col.getEventList();

    String href = request.getReqPar("href");
    if (request.getReqPar("recurrenceId") != null) {
      href += "#" + request.getReqPar("recurrenceId");
    }

    refs.add(new EventListEntry(href));

    col.setEventList(refs);

    cl.updateCollection(col);

    return forwardSuccess;
  }
}
