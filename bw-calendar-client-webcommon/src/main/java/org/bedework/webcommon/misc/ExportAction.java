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

import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwDuration;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.base.BwTimeRange;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.convert.IcalTranslator;
import org.bedework.convert.Icalendar;
import org.bedework.convert.jscal.JSCalTranslator;
import org.bedework.jsforj.model.JSGroup;
import org.bedework.util.calendar.IcalendarUtil;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwRequest;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletResponse;

import static org.bedework.webcommon.event.EventUtil.findEvent;

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
  @Override
  public int doAction(final BwRequest request) {
    final String calPath = request.getReqPar("calPath");
    final Client cl = request.getClient();

    EventInfo ev = null;
    Collection<EventInfo> evs = null;
    int method = Icalendar.methodTypePublish;

    if ((request.getReqPar("guid") != null) ||
        (request.getReqPar("href") != null) ||
        (request.getReqPar("eventName") != null)) {
      if (debug()) {
        debug("Export event by guid, href or name");
      }

      ev = findEvent(request, Rmode.overrides, getLogger());

      if (ev == null) {
        return forwardNoAction;
      }

      method = ev.getEvent().getScheduleMethod();
      if (!Icalendar.validItipMethodType(method)) {
        method = Icalendar.methodTypePublish;
      }
    } else {
      if (calPath == null) {
        warn("No collection path supplied");
        return forwardNotFound;
      }

      final BwCalendar col = cl.getCollection(calPath);
      if (col == null) {
        return forwardNotFound;
      }

      final String dl = request.getReqPar("dateLimits");
      final BwDateTime start;
      final BwDateTime end;

      BwTimeRange tr = null;
      final AuthProperties authp = cl.getAuthProperties();


      if (dl != null) {
        if (dl.equals("active")) {
          tr = new BwTimeRange(
                  BwDateTimeUtil.getDateTime(DateTimeUtil.isoDate(),
                                             true, false,
                                             null),   // tzid
                  null);
        } else if (dl.equals("limited")) {
          tr = getBwForm().getEventDates().getTimeRange();
        }
      }
      
      if (tr != null) {
        start = tr.getStart();
        end = tr.getEnd();
      } else {
        start = BwDateTime.makeBwDateTime(new DateTime());

        int days = request.getIntReqPar("days", -32767);
        
        final int max = authp.getMaxWebCalPeriod();
        if (days < 0) {
          days = max;
        } else if ((days > max) && !cl.isSuperUser()) {
          days = max;
        }
        
        final BwDuration dur = new BwDuration();
        dur.setDays(days);

        end = start.addDuration(dur);
      }

      evs = cl.getEvents("(colPath='" + col.getPath() + "')",
                         start,
                         end,
                         request.present("expand"));

      if (evs == null) {
        return forwardNotFound;
      }
    }

    if (ev != null) {
      evs = new TreeSet<>();

      evs.add(ev);
    }

    final HttpServletResponse resp = request.getResponse();

    resp.setHeader("Content-Disposition",
                   "Attachment; Filename=\"" +
                           request.getContentName() + "\"");

    try (final Writer wtr = resp.getWriter()) {
      final String ct = request.getReqPar("content-type");

      if ("application/jscalendar+json".equals(ct)) {
        final JSCalTranslator trans = new JSCalTranslator(
                new IcalCallbackcb(cl));

        final JSGroup grp = trans.toJScal(evs, method);

        resp.setContentType(ct + "; charset=UTF-8");

        JSCalTranslator.writeJSCalendar(grp, wtr);
      } else {
        final IcalTranslator trans = new IcalTranslator(
                new IcalCallbackcb(cl));

        final Calendar ical = trans.toIcal(evs, method);

        resp.setContentType("text/calendar; charset=UTF-8");

        IcalendarUtil.writeCalendar(ical, wtr);
      }
    } catch (final IOException e) {
      throw new BedeworkException(e);
    }

    return forwardNull;
  }
}
