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

import org.bedework.appcommon.TimeView;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventObj;
import org.bedework.calfacade.base.StartEndComponent;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.webcommon.Attendees;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;
import org.bedework.webcommon.DurationBean;
import org.bedework.webcommon.EventDates;

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
public class InitAddEventAction extends EventActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    request.refresh();

    BwEvent ev = new BwEventObj();
    form.getEventDates().setNewEvent(ev);

    TimeView tv = request.getSess().getCurTimeView(request);

    form.getEventStartDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
    form.getEventEndDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
    EventInfo ei = new EventInfo(ev);

    form.setEventInfo(ei, true);

    form.assignSavedEvent(new BwEventObj());
    form.resetSelectIds();

    ChangeTable changes = ei.getChangeset(request.getClient().getCurrentPrincipalHref());

    form.assignAddingEvent(true);
    form.setAttendees(new Attendees());
    form.setFbResponses(null);
    form.setFormattedFreeBusy(null);

    ev.setEntityType(request.getEntityType()); // Check for error after sched

    int sched = request.getSchedule();

    if (request.getErrFlag()) {
      return forwardValidationError;
    }

    if (sched == ScheduleMethods.methodTypeRequest) {
      int res = initMeeting(request, form, true);

      if (res != forwardSuccess) {
        return res;
      }
    }

    String date = request.getReqPar("startdate");

    EventDates evdates = form.getEventDates();

    try {
      if (date != null) {
        evdates.setFromDate(date);
      }

      date = request.getReqPar("enddate");

      if (date != null) {
        evdates.getEndDate().setDateTime(date);
      }
    } catch (CalFacadeException cfe) {
      if (CalFacadeException.badDate.equals(cfe.getMessage())) {
        form.getErr().emit(ValidationError.invalidDate, date);
        return forwardBadDate;
      } else {
        throw cfe;
      }
    }

    int minutes = request.getIntReqPar("minutes", -1);

    if (minutes > 0) {
      // Set the duration
      evdates.setEndType(String.valueOf(StartEndComponent.endTypeDuration));
      DurationBean dur = evdates.getDuration();

      dur.setType(DurationBean.dayTimeDuration);
      dur.setMinutes(minutes);
    }

    form.setDescription(null);
    form.setSummary(null);
    form.setEventStatus(null);
    form.setRruleComponents(null);
    form.setFormattedRdates(null);
    form.setFormattedExdates(null);

    BwCalendar cal = request.getNewCal(false);

    if (cal != null) {
      changes.changed(PropertyInfoIndex.COLLECTION.getPname(),
                      ev.getColPath(),
                      cal.getPath());
      ev.setColPath(cal.getPath());
    }

    BwSession sess = request.getSess();

    sess.embedAddContentCalendarCollections(request);
    sess.embedUserCollections(request);

    sess.embedContactCollection(request, BwSession.ownersEntity);

    sess.embedContactCollection(request, BwSession.preferredEntity);

    sess.embedCategories(request, false, BwSession.ownersEntity);
    sess.embedLocations(request, BwSession.ownersEntity);
    sess.embedLocations(request, BwSession.preferredEntity);

    //if (!request.setEventCalendar(ev)) {
    //  return forwardValidationError;
    //}

    return forwardSuccess;
  }
}
