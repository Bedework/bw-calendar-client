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
import org.bedework.base.exc.BedeworkAccessException;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.convert.IcalTranslator;
import org.bedework.convert.Icalendar;
import org.bedework.util.calendar.IcalendarUtil;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwModuleState;
import org.bedework.webcommon.BwRequest;

import org.apache.commons.text.StringEscapeUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import static org.bedework.webcommon.DateViewUtil.gotoDateView;

/**
 * Action to publish a calendar as rfc formatted information
 * <p>Request parameters - all optional:<ul>
 *      <li>  start:    start of period - default to beginning of this week</li>.
 *      <li>  end:      end of period - default to end of this week</li>.
 *      <li>  fexpr:    required filter expression</li>.
 *      <li>  expanded: expand recurrences</li>.
 *      <li>  name:     optional name for result</li>.
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"noAction"     input error or we want to ignore the request.</li>
 *      <li>"noAccess"     No acccess to free busy</li>
 *      <li>"notFound"     event not found.</li>
 *      <li>"error"        input error - correct and retry.</li>
 *      <li>"success"      fetched OK.</li>
 * </ul>
 *
 * <p>If no period is given return this week. If no interval and intunit is
 * supplied default to 1 hour intervals during the workday.
 *
 * @author Mike Douglass douglm @ rpi.edu
 */
public class WebCalendarAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request) {
    final Client cl = request.getClient();
    final BwModuleState mstate = request.getModule().getState();

    gotoDateView(request, mstate.getDate(), mstate.getViewType());

    final Locale loc = BwLocale.getLocale();
    final Calendar start = Calendar.getInstance(loc);
    final Calendar end = Calendar.getInstance(loc);
    String name = request.getReqPar("name");

    if (name == null) {
      name = start.toString();
    }

    name = StringEscapeUtils.escapeJava(name);

    final String st = request.getReqPar("start");

    if (st == null) {
      start.add(Calendar.WEEK_OF_YEAR, -1);
    } else {
      final int days = request.getIntReqPar("start", -32767);
      if (days != -32767) {
        start.add(Calendar.DATE, days);
      } else {
        final Date jdt = DateTimeUtil.fromISODate(st);
        start.setTime(jdt);
      }
    }

    final String et = request.getReqPar("end");

    if (et == null) {
      // 1 week
      end.add(Calendar.MONTH, 1);
    } else {
      final int days = request.getIntReqPar("end", -32767);
      if (days != -32767) {
        end.add(Calendar.DATE, days);
      } else {
        final Date jdt = DateTimeUtil.fromISODate(et);
        end.setTime(jdt);
      }
    }

    // Don't allow more than about 3 months
    final Calendar check = Calendar.getInstance(loc);
    check.setTime(start.getTime());
    check.add(Calendar.DATE, 31 * 3);

    if (check.before(end)) {
      request.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "dates");
      return forwardNull;
    }

    final String fexpr = request.getReqPar("fexpr");
    if (fexpr == null) {
      request.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "no fexpr specified");
      return forwardNull;
    }

    try {
      final BwDateTime sdt = BwDateTimeUtil.getDateTime(
              DateTimeUtil.isoDate(start.getTime()),
              true, false, null);
      final BwDateTime edt = BwDateTimeUtil.getDateTime(
              DateTimeUtil.isoDate(end.getTime()),
              true, false, null);

      if (debug()) {
        debug("getEvents for start = " + sdt +
                 " end = " + edt);
      }

      final Collection<EventInfo> evs =
              cl.getEvents(fexpr,
                           sdt,
                           edt,
                           request.present("expanded"));

      final IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(cl));

      final net.fortuna.ical4j.model.Calendar c = trans.toIcal(
              evs,
              Icalendar.methodTypePublish);

      if (!name.endsWith(".ics")) {
        name += ".ics";
      }

      request.prepareWrite(name, "text/calendar");

      IcalendarUtil.writeCalendar(c, request.getWriter());
    } catch (final BedeworkAccessException ignored) {
      request.sendError(HttpServletResponse.SC_FORBIDDEN, null);
    }

    return forwardNull;
  }
}
