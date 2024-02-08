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

import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.convert.IcalTranslator;
import org.bedework.convert.RecurRuleComponents;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.misc.response.GetEntitiesResponse;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

import java.util.Date;

import static org.bedework.client.web.rw.EventCommon.copyEvent;
import static org.bedework.util.misc.response.Response.Status.notFound;

/**
 * Fetches the inbox copy:
 * <ol>
 * <li> Try to fetch the referenced copy,</li>
 * <li> If not present:<ul>
 * <li>  if preserveInbox is not present clone+delete the inbox copy</li>
 * <li> Display the inbox copy</li>
 * <li> For CANCEL it's OK. </li>
 * <li> For anything else warn user event disappeared</li>
 * </li></ul>
 * <li> If calendar copy is present<ul>
 * <li> if preserveInbox is not present delete the inbox copy</li>
 * <li> Embed calendar event.</li>
 * <li> Forward to display</li>
 * </li></ul>
 *
 * <p>Request parameters<ul>
 *      <li>"subid"    subscription id for event.</li>
 *      <li>"calPath"  calendar for event.</li>
 *      <li>"guid"     guid of event.</li>
 *      <li>"recurrenceId"   recurrence-id of event instance - possibly null.</li>
 * </ul>
 * <p>Forwards to:<ul>
 *      <li>"noAction"     when request seems wrong.</li>
 *      <li>"noMeeting"    when no copy of the event is found.</li>
 *      <li>"continue"    event is setup for viewing.</li>
 * </ul>
 */
public class ProcessInboxEvent extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    if (!request.present("calPath")) {
      // Just continue

      request.setSessionAttr(BwRequest.bwDefaultEventCalendar,
                             cl.getPreferences().getDefaultCalendarPath());
      return forwardContinue;
    }

    final boolean preserveInbox = request.present("preserveInbox");
    form.assignAddingEvent(false);

    final Rmode mode;
    if (!request.present("recurrenceId")) {
      mode = Rmode.overrides;
    } else {
      mode = Rmode.expanded;
    }
    final EventInfo einf = findEvent(request, mode);
    if (einf == null) {
      // Disappeared while we sat there I guess.
      return forwardNoAction;
    }

    request.removeSessionAttr(BwRequest.bwReqUidName);

    if (einf.getEvent().getEntityType() == IcalDefs.entityTypeVpoll) {
      form.setRequestedUid(einf.getEvent().getUid());
      final String tab = request.getReqPar("tab");

      if (tab == null) {
        request.removeSessionAttr(BwRequest.bwReqVpollTabName);
      } else {
        request.setSessionAttr(BwRequest.bwReqVpollTabName, tab);
      }

      if (!preserveInbox) {
        // Delete the inbox copy
        cl.deleteEvent(einf, false);
      }

      return forwardContinue;
    }

    // Try to fetch the real copy.

    final EventInfo colEi = cl.getStoredMeeting(einf.getEvent());
    if (colEi == null) {
      // Copy the inbox copy - will embed it in form
      copyEvent(request, einf.getEvent());

      if (!preserveInbox) {
        // Delete the inbox copy
        cl.deleteEvent(einf, false);
      }

      form.getMsg().emit(ClientMessage.scheduleColCopyDeleted);
      return forwardNoMeeting;
    }

    if (!preserveInbox) {
      // Delete the inbox copy
      cl.deleteEvent(einf, false);
    }

    form.setEventInfo(colEi, false);
    final BwEvent ev = colEi.getEvent();

    // Not export - just set up for display

    final GetEntitiesResponse<RecurRuleComponents> rrcs =
            RecurRuleComponents.fromEventRrules(ev);

    if (rrcs.getStatus() == notFound) {
      form.setRruleComponents(null);
    } else if (!rrcs.isOk()) {
      request.getErr().emit(rrcs.getMessage());
      return forwardNoAction;
    } else {
      form.setRruleComponents(rrcs.getEntities());
    }

    final EventFormatter ef = new EventFormatter(cl,
                                                 new IcalTranslator(new IcalCallbackcb(cl)),
                                                 colEi);

    form.setCurEventFmt(ef);

    final Date evdt = DateTimeUtil.fromISODateTimeUTC(ev.getDtstart().getDate());

    /* Set the date using the current user timezone */
    setViewDate(request, DateTimeUtil.isoDate(evdt).substring(0, 8));

    // Assume we need the collection containing the meeting
    form.setMeetingCal(cl.getCollection(ev.getColPath()));

    request.getSess().embedLocations(request,
                                     BwSession.ownersEntity);

    return forwardContinue;
  }
}
