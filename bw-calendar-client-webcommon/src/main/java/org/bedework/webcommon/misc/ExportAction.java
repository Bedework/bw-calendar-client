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
package org.bedework.webcommon.misc;

import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.base.BwTimeRange;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.calsvci.CalSvcI;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.icalendar.Icalendar;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import edu.rpi.sss.util.DateTimeUtil;

import net.fortuna.ical4j.model.Calendar;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Action to export an icalendar file. This might be better done as a custom tag which
 * could write directly to the response. We could be generating something big here.
 *
 * <p>Request parameters<ul>
 *      <li>"guid"           guid of event.</li>
 *      <li>"recurrenceId"   recurrence id of event (optional)... or</li>
 *      <li>"calPath"        Path of calendar to export.</li>
 *      <li>"sresult"        Any value - export last search result.</li>
 *      <li>"expand"         true/false - default is to not expand recurrences.</li>
 * </ul>
 * <p>Forwards to:<ul>
 *      <li>"notFound"     no event was found.</li>
 *      <li>"success"      exported ok.</li>
 * </ul>
 *
 * @author Mike Douglass
 */
public class ExportAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    String calPath = request.getReqPar("calPath");
    CalSvcI svci = form.fetchSvci();

    EventInfo ev = null;
    Collection<EventInfo> evs = null;
    int method = Icalendar.methodTypePublish;

    if ((request.getReqPar("guid") != null) ||
        (request.getReqPar("eventName") != null)) {
      if (debug) {
        debugMsg("Export event by guid or name");
      }

      ev = findEvent(request, Rmode.overrides);

      if (ev == null) {
        return forwardNoAction;
      }
      if (debug) {
        debugMsg("Got event by guid");
      }

      method = ev.getEvent().getScheduleMethod();
      if (!Icalendar.validItipMethodType(method)) {
        method = Icalendar.methodTypePublish;
      }
    } else {
      BwCalendar col = null;
      if (calPath != null) {
        col = svci.getCalendarsHandler().get(calPath);
        if (col == null) {
          return forwardNotFound;
        }
      }

      String expandStr = request.getReqPar("expand");
      RecurringRetrievalMode rrm = null;
      if ((expandStr != null) && "true".equals(expandStr)) {
        rrm = new RecurringRetrievalMode(Rmode.expanded);
      } else {
        rrm = new RecurringRetrievalMode(Rmode.overrides);
      }

      BwTimeRange tr = new BwTimeRange();

      String dl = request.getReqPar("dateLimits");

      if (dl != null) {
        if (dl.equals("active")) {
          tr = new BwTimeRange(BwDateTimeUtil.getDateTime(DateTimeUtil.isoDate(),
                                                          true, false,
                                                          null),   // tzid
                                                          null);
        } else if (dl.equals("limited")) {
          tr = form.getEventDates().getTimeRange();
        }
      }

      FilterBase f = svci.getClientState().getViewFilter(col);

      evs = svci.getEventsHandler().getEvents(null,
                                              f,
                                              tr.getStart(),
                                              tr.getEnd(),
                                              null, // retrieveList
                                              rrm);

      if (evs == null) {
        return forwardNotFound;
      }
    }

    if (ev != null) {
      evs = new TreeSet<EventInfo>();

      evs.add(ev);
    }

    IcalTranslator trans = new IcalTranslator(svci.getIcalCallback());

    Calendar ical = trans.toIcal(evs, method);

    request.getResponse().setHeader("Content-Disposition",
                                    "Attachment; Filename=\"" +
                                    form.getContentName() + "\"");
    request.getResponse().setContentType("text/calendar; charset=UTF-8");

    IcalTranslator.writeCalendar(ical, request.getResponse().getWriter());
    request.getResponse().getWriter().close();

    return forwardNull;
  }
}
