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

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.base.response.GetEntitiesResponse;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.base.BwStringBase;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.Attendees;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.convert.IcalTranslator;
import org.bedework.convert.RecurRuleComponents;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.webcommon.BwRequest;

import java.util.Collection;
import java.util.TreeSet;

import static org.bedework.base.response.Response.Status.notFound;
import static org.bedework.client.web.rw.EventCommon.copyEvent;
import static org.bedework.client.web.rw.EventCommon.resetEvent;
import static org.bedework.webcommon.event.EventUtil.findEvent;

/** This action fetches events for editing
 * ADMIN + RW
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such event.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class FetchEventAction extends RWActionBase {
  @Override
  public String doAction(final BwRequest request,
                         final RWClient cl) {
    if (cl.getPublicAdmin()) {
      // Handled by override
      return forwardNoAccess;
    }

    return doTheAction(request, cl);
  }

  protected String doTheAction(final BwRequest request,
                               final RWClient cl) {
    final var form = getRwForm();
    form.assignAddingEvent(false);

    final Rmode mode;
    if (!request.present("recurrenceId")) {
      mode = Rmode.overrides;
    } else {
      mode = Rmode.expanded;
    }

    final EventInfo einf = findEvent(request, mode, getLogger());

    if (einf == null) {
      request.error(ClientError.unknownEvent);
      return forwardNotFound;
    }

    final var fwd = refreshEvent(request, einf);
    form.setAttendees(new Attendees());
    form.setFbResponses(null);
    form.setFormattedFreeBusy(null);

    request.initialiseSess();

    if (forwardContinue.equals(fwd)) {
      if (request.hasCopy()) {
        copyEvent(request, einf.getEvent());

        return forwardCopy;
      }

      resetEvent(request, false);
    }

    return fwd;
  }

  /** Given the EventInfo object refresh the information in the form.
   *
   * @param request bw request object
   * @param ei      event info
   * @return forward.
   */
  private String refreshEvent(final BwRequest request,
                              final EventInfo ei) {
    final var form = (BwRWActionForm)request.getBwForm();
    final var cl = request.getClient();
    final var ev = ei.getEvent();

    form.setEventInfo(ei, false);
    form.assignAddingEvent(false);

    String str = null;
    BwStringBase<?> bstr = ev.findDescription(null);
    if (bstr != null) {
      str = bstr.getValue();
    }
    form.setDescription(str);

    bstr = ev.findSummary(null);
    if (bstr != null) {
      str = bstr.getValue();
    } else {
      str = null;
    }
    form.setSummary(str);

    form.setEventStatus(ev.getStatus());

    if (!request.setEventCalendar(ei,
                                  ei.getChangeset(cl.getCurrentPrincipalHref()))) {
      return forwardNoAction;
    }

    final BwLocation loc = ev.getLocation();

    if (debug()) {
      if (loc == null) {
        debug("Set event with null location");
      } else {
        debug("Set event with location " + loc);
      }
    }

    form.setLocation(null);

    if (loc != null) {
      form.setLocationUid(loc.getUid());
    } else {
      form.setLocationUid(null);
    }

    final GetEntitiesResponse<RecurRuleComponents> rrcs =
            RecurRuleComponents.fromEventRrules(ev);

    if (rrcs.getStatus() == notFound) {
      form.setRruleComponents(null);
    } else if (!rrcs.isOk()) {
      request.error(rrcs.getMessage());
      return forwardNoAction;
    } else {
      form.setRruleComponents(rrcs.getEntities());
    }

    final EventFormatter ef =
            new EventFormatter(cl,
                               new IcalTranslator(new IcalCallbackcb(cl)),
                               ei);

    form.setCurEventFmt(ef);

    if (ev.getScheduleMethod() != ScheduleMethods.methodTypeNone) {
      // Assume we need a list of event calendars
      form.setMeetingCal(cl.getCollection(ev.getColPath()));
    }

    Collection<BwDateTime> dates = ev.getRdates();
    Collection<DateTimeFormatter> frdates = null;
    if ((dates != null) && (!dates.isEmpty())) {
      frdates = new TreeSet<>();

      for (final BwDateTime date: dates) {
        frdates.add(new DateTimeFormatter(date));
      }
    }

    form.setFormattedRdates(frdates);

    dates = ev.getExdates();
    frdates = null;
    if ((dates != null) && (!dates.isEmpty())) {
      frdates = new TreeSet<>();

      for (final BwDateTime date: dates) {
        frdates.add(new DateTimeFormatter(date));
      }
    }

    form.setFormattedExdates(frdates);

    return forwardContinue;
  }
}
