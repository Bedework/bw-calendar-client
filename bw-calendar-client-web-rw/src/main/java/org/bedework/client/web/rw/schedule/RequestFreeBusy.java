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
package org.bedework.client.web.rw.schedule;

import org.bedework.caldav.util.ParseUtil;
import org.bedework.caldav.util.TimeRange;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventObj;
import org.bedework.calfacade.BwFreeBusyComponent;
import org.bedework.calfacade.ScheduleRecipientResult;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.util.json.JsonUtil;
import org.bedework.webcommon.BwRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Action to fetch free busy information for web use. Modeled on CalDAV iSchedule
 * approach.
 *
 * <p>Request parameters:<ul>
 *      <li>  attendeeUri: whose free busy we want</li>.
 *      <li>  organizerUri: The organizer</li>.
 *      <li>  start:    start of period - default to beginning of this week</li>.
 *      <li>  end:      end of period - default to end of this week</li>.
 * </ul>
 * <p>Forwards to:<ul>
 *      <li>"noAction"     input error or we want to ignore the request.</li>
 *      <li>"noAccess"     No acccess to free busy</li>
 *      <li>"notFound"     event not found.</li>
 *      <li>"error"        input error - correct and retry.</li>
 *      <li>"success"      fetched OK.</li>
 * </ul>
 *
 * <p>If no period is given return this week.
 *
 * @author Mike Douglass douglm   rpi.edu
 */
public class RequestFreeBusy extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl) {
    try {
      final Collection<String> attendees = request.getReqPars("attendeeUri");
      final Set<String> attendeeUris = new TreeSet<>();

      for (final String att: attendees) {
        attendeeUris.add(cl.uriToCaladdr(att));
      }

      String orgUri = request.getReqPar("organizerUri");

      if (orgUri == null) {
        orgUri = cl.getCurrentCalendarAddress();
      }

      final AuthProperties authp = request.getSess().getAuthpars();

      int max = 0;

      if (!cl.isSuperUser()) {
        max = authp.getMaxFBPeriod();
      }

      final TimeRange tr = ParseUtil.getPeriod(request.getReqPar("start"),
                                               request.getReqPar("end"),
                                               Calendar.DATE,
                                               authp.getDefaultFBPeriod(),
                                               Calendar.DATE,
                                               max);


      final HttpServletResponse resp = request.getResponse();

      if (tr == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dates");
        return forwardNull;
      }

      final BwDateTime start = BwDateTime.makeBwDateTime(tr.getStart());

      final BwDateTime end = BwDateTime.makeBwDateTime(tr.getEnd());

      final BwEvent fbreq = BwEventObj.makeFreeBusyRequest(start, end,
                                                           null,// organizer
                                                           orgUri,
                                                           null,// attendees
                                                           attendeeUris);
      if (fbreq == null) {
        return forwardBadRequest;
      }

      final var sr = cl.schedule(new EventInfo(fbreq),
                                 null, null, false);

      request.prepareWrite("freebusy.js", "text/json");

      outputJson(resp, sr);
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
      }
      request.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        t.getMessage());
    }

    return forwardNull;
  }

  @Override
  protected boolean actionIsWebService() {
    return true;
  }

  private final static int indentSize = 2;

  private void outputJson(final HttpServletResponse resp,
                          final ScheduleResult<?> sr) throws Throwable {
    final Writer wtr = resp.getWriter();

    int indent = 0;

    wtr.write("{\n");

    indent += indentSize;

    wtr.write("  \"microformats\": {\n");
    wtr.write("    \"schedule-response\": [\n");

    indent += indentSize;

    final var srrs = sr.recipientResults.values();
    int ct = srrs.size();

    for (final var srr: srrs) {
      outputJson(wtr, indent + indentSize, srr, ct > 1);
      ct--;
    }

    indented(wtr, indent, "]\n");

    indent -= indentSize;
    indented(wtr, indent, "}\n");
    wtr.write("}\n");
  }

  private void outputJson(final Writer wtr,
                          final int indent,
                          final ScheduleRecipientResult srr,
                          final boolean withComma) throws Throwable {
    indented(wtr, indent, "{\n");

    final int indentPlus = indent + indentSize;
    outputJsonValue(wtr, indentPlus, "recipient", srr.recipient);
    outputJsonValue(wtr, indentPlus, "status",
                    String.valueOf(srr.getStatus()),
                    srr.freeBusy != null);

    if (srr.freeBusy != null) {
      outputJson(wtr, indentPlus, srr.freeBusy);
    }

    if (withComma) {
      indented(wtr, indentPlus, "},\n");
    } else {
      indented(wtr, indentPlus, "}\n");
    }
  }

  private void outputJson(final Writer wtr,
                          final int indent,
                          final BwEvent ev) throws Throwable {
    outputJsonStart(wtr, indent, "calendar-data", false);
    wtr.write("{\n");

    int indentPlus = indent + indentSize;

    outputJsonValue(wtr, indentPlus, "dtstart",
                    ev.getDtstart().getDate());

    outputJsonValue(wtr, indentPlus, "dtend",
                    ev.getDtend().getDate());

    outputJsonValue(wtr, indentPlus, "uid", ev.getUid());

    outputJsonValue(wtr, indentPlus, "organizer",
                    ev.getOrganizer().getOrganizerUri());

    final Collection<String> attUris = new ArrayList<>();
    for (final BwAttendee att: ev.getAttendees()) {
      attUris.add(att.getAttendeeUri());
    }

    outputJsonValues(wtr, indentPlus, "attendee", attUris,
                     ev.getFreeBusyPeriods() != null);

    if (ev.getFreeBusyPeriods() != null) {
      outputJsonStart(wtr, indentPlus, "freebusy", true);
      indentPlus += indentSize;
      int ct = ev.getFreeBusyPeriods().size();

      for (final BwFreeBusyComponent fbc: ev.getFreeBusyPeriods()) {
        indented(wtr, indentPlus, "{\n");

        indentPlus += indentSize;
        outputJsonValue(wtr, indentPlus, "fbtype", fbc.getTypeVal());
        outputJsonValues(wtr, indentPlus, "periods", fbc.getPeriods(), false);

        indentPlus -= indentSize;
        if (ct > 1) {
          indented(wtr, indentPlus, "},\n");
        } else {
          indented(wtr, indentPlus, "}\n");
        }
        ct--;
      }

      indented(wtr, indentPlus, "]\n");
      indentPlus -= indentSize;
    }

    // End calendar-data
    indentPlus -= indentSize;
    indented(wtr, indentPlus, "}\n");
  }

  private void outputJsonValue(final Writer wtr,
                               final int indent,
                               final String name,
                               final String val) throws Throwable {
    outputJsonStart(wtr, indent, name, false);
    outputJsonValue(wtr, indent, val);
  }

  private void outputJsonValue(final Writer wtr,
                               final int indent,
                               @SuppressWarnings("SameParameterValue") final String name,
                               final String val,
                               final boolean withComma) throws Throwable {
    outputJsonStart(wtr, indent, name, false);
    outputJsonValue(wtr, indent, val, withComma);
  }

  private void outputJsonValues(final Writer wtr,
                                final int indent,
                                final String name,
                                final Collection<?> vals,
                                final boolean withComma) throws Throwable {
    outputJsonStart(wtr, indent, name, true);
    final int indentPlus = indent + indentSize;
    int ct = vals.size();

    for (final Object val: vals) {
      outputJsonValue(wtr, indentPlus, String.valueOf(val), ct > 1);
      ct--;
    }

    if (withComma) {
      indented(wtr, indentPlus, "],\n");
    } else {
      indented(wtr, indentPlus, "]\n");
    }
  }

  private void outputJsonStart(final Writer wtr,
                               final int indent,
                               final String name,
                               final boolean multi) throws Throwable {
    indented(wtr, indent, "\"");

    wtr.write(name.toLowerCase());

    if (multi) {
      wtr.write("\" : [");
    } else {
      wtr.write("\" : ");
    }
  }

  private void outputJsonValue(final Writer wtr,
                               final int indent,
                               final String val) throws Throwable {
    outputJsonValue(wtr, indent, val, true);
  }

  private void outputJsonValue(final Writer wtr,
                               final int indent,
                               final String val,
                               final boolean withComma) throws Throwable {
    /* "email" : [
    {"type" : ["pref"], "value" : "foo at example.com"},
    {"value" : "bar at example.com"}
  ]
 */

    indented(wtr, indent, "{");

    wtr.write("\"value\" : ");

    if (val == null) {
      wtr.write(JsonUtil.jsonEncode(""));
    } else {
      wtr.write(JsonUtil.jsonEncode(val));
    }
    if (withComma) {
      wtr.write("},\n");
    } else {
      wtr.write("}\n");
    }
  }

  private final static String blanks = "                               ";
  private final static int blanksLen = blanks.length();

  private void indented(final Writer wtr,
                        final int indent,
                        final String val) throws Throwable {
    indent(wtr, indent);
    wtr.write(val);
  }

  private void indent(final Writer wtr,
                      final int indent) throws Throwable {
    if (indent >= blanksLen) {
      wtr.write(blanks);
    } else {
      wtr.write(blanks.substring(0, indent));
    }
  }
}
