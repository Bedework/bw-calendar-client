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

import org.bedework.appcommon.TimeView;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventObj;
import org.bedework.calfacade.base.StartEndComponent;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.client.web.rw.Attendees;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;
import org.bedework.webcommon.DurationBean;
import org.bedework.webcommon.EventDates;

import static org.bedework.client.web.rw.EventCommon.initMeeting;

/** Set up for addition of new event.
 *
 * <p>Request parameters are:<ul>
 *      <li>"startdate"              Optional start date for the event
 *                                   as yymmdd or yymmddTHHmmss</li>
 *      <li>"enddate"                Optional end date for the event
 *                                   as yymmdd or yymmddTHHmmss</li>
 *      <li>"minutes"                Optional duration in minutes</li>
 *      <li> subname:                Name of a subscription to an external calendar</li>.
 *      <li> newCalPath:             Path to a (writeable) calendar collection</li>.
 *      <li>entityType               event/task/journal</li>.
 * </ul>
 *
 * Also
 * @see org.bedework.webcommon.BwRequest#getSchedule()
 *
 */
public class InitAddEventAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) throws Throwable {
    request.refresh();

    final BwEvent ev = new BwEventObj();
    form.getEventDates().setNewEvent(ev);

    final TimeView tv = request.getSess().getCurTimeView(request);

    form.getEventStartDate().setDateTime(tv.getCurDayFmt()
                                           .getDateTimeString());
    form.getEventEndDate().setDateTime(tv.getCurDayFmt()
                                         .getDateTimeString());
    final EventInfo ei = new EventInfo(ev);

    form.setEventInfo(ei, true);

    form.assignSavedEvent(new BwEventObj());
    form.resetSelectIds();

    final ChangeTable changes =
            ei.getChangeset(request.getClient()
                                   .getCurrentPrincipalHref());

    form.assignAddingEvent(true);
    form.setAttendees(new Attendees());
    form.setFbResponses(null);
    form.setFormattedFreeBusy(null);

    ev.setEntityType(request.getEntityType()); // Check for error after sched

    final int sched = request.getSchedule();

    if (request.getErrFlag()) {
      return forwardValidationError;
    }

    if (sched == ScheduleMethods.methodTypeRequest) {
      final int res = initMeeting(request, form, true);

      if (res != forwardSuccess) {
        return res;
      }
    }

    String date = request.getReqPar("startdate");

    final EventDates evdates = form.getEventDates();

    if (date != null) {
      evdates.setFromDate(date);
    }

    date = request.getReqPar("enddate");

    if (date != null) {
      evdates.getEndDate().setDateTime(date);
    }

    final int minutes = request.getIntReqPar("minutes", -1);

    if (minutes > 0) {
      // Set the duration
      evdates.setEndType(String.valueOf(StartEndComponent.endTypeDuration));
      final DurationBean dur = evdates.getDuration();

      dur.setType(DurationBean.dayTimeDuration);
      dur.setMinutes(minutes);
    }

    form.setDescription(null);
    form.setSummary(null);
    form.setEventStatus(null);
    form.setRruleComponents(null);
    form.setFormattedRdates(null);
    form.setFormattedExdates(null);

    final BwCalendar cal = request.getNewCal(false);

    if (cal != null) {
      changes.changed(PropertyInfoIndex.COLLECTION,
                      ev.getColPath(),
                      cal.getPath());
      ev.setColPath(cal.getPath());
    }

    final BwSession sess = request.getSess();

    sess.embedAddContentCalendarCollections(request);
    sess.embedUserCollections(request);

    sess.embedContactCollection(request, BwSession.ownersEntity);

    sess.embedCategories(request, false, BwSession.ownersEntity);
    sess.embedLocations(request, BwSession.ownersEntity);

    //if (!request.setEventCalendar(ev)) {
    //  return forwardValidationError;
    //}

    return forwardSuccess;
  }
}
