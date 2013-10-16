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

import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.EventListPars;

import net.fortuna.ical4j.model.Calendar;

import java.util.Collection;

/**
 * Action to set up an event for display.
 * <p>Request parameters<ul>
 *      <li>"ignoreCreator" show all events regardless of creator - value: "yes"
 *      <li>"calPath"  calendar for event.</li>
 *      <li>"guid"     guid of event.</li>
 *      <li>"recurrenceId"   recurrence-id of event instance - possibly null.</li>
 *      <li>"start"             start date</li>
 *      <li>"end"               end date</li>
 *      <li>"listAllEvents"     (if no start/end) true/false.</li>
 *      <li>"days"              (if no start/end) integer number of days</li>
 *      <li>"filterName"        name of predefined filter</li>
 *      <li>"fexpr"             filter expression</li>
 *      <li>"cat"               (if no filterName) multi-valued category names</li>
 *      <li>"forExport"         true if the intent is to export the list.</li>
 * </ul>
 * <p>Forwards to:<ul>
 *      <li>"noAction"     when request seems wrong.</li>
 *      <li>"showEvent"    event is setup for viewing.</li>
 * </ul>
 */
public class EventListParsAction extends EventActionBase {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    EventListPars elpars = new EventListPars();
    Client cl = request.getClient();

    int forward = setEventListPars(request, elpars);
    if (forward != forwardSuccess) {
      return forward;
    }

    elpars.setForExport(request.getBooleanReqPar("forExport", false));
    elpars.setUseDbSearch(request.getBooleanReqPar("useDbSearch", false));

    cl.setEventListPars(elpars);

    if ((elpars.getFormat() != null) &&
        (elpars.getFormat().equals("text/calendar"))) {
      Collection<EventInfo> eis = getEventsList(request);
      if (eis == null) {
        return forwardNull;
      }

      IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(request.getClient()));

      Calendar ical = trans.toIcal(eis, ScheduleMethods.methodTypePublish);

      request.getResponse().setHeader("Content-Disposition",
                                      "Attachment; Filename=\"" +
                                      form.getContentName() + "\"");
      request.getResponse().setContentType("text/calendar; charset=UTF-8");

      IcalTranslator.writeCalendar(ical, request.getResponse().getWriter());
      request.getResponse().getWriter().close();

      return forwardNull;
    }

    return forwardSuccess;
  }
}
