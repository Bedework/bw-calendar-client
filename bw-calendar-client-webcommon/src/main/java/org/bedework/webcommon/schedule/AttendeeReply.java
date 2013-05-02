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
package org.bedework.webcommon.schedule;

import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.event.EventActionBase;

/** NOTE: This action is possibly no longer needed now that replies are
 * handled automatically by the system. We probably need to retain inbox entries
 * which contain comments from the attendee or not accetptances.
 *
 * <p>Action to handle replies to scheduling requests - that is the schedule
 * method was REPLY. We, as an organizer (or their delegate) are going to
 * use the reply to update the original invitation and any copies.
 *
 * <p>The incoming event is currently set in the form event property.
 *
 * <p>Request parameters<ul>
 *      <li>update          To trigger update of stored event from the
 *                          editEvent object.</li>
 *      <li>newCalPath      Where to put the event.</li>
 *      <li>calPath         Locations of existing copies of the event.</li>
 * </ul>
 *.
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     for guest mode</li>
 *      <li>"noAction"     for invalid</li>
 *      <li>"success"      changes made.</li>
 * </ul>
 */
public class AttendeeReply extends EventActionBase {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }

    /* =================== Assuming no longer used =======================
    CalSvcI svc = form.fetchSvci();

    BwCalendar inbox = svc.getCalendarsHandler().getSpecial(BwCalendar.calTypeInbox, false);
    if (inbox == null) {
      return forwardSuccess;
    }

    EventInfo ei = refetchEvent(form);
    if (ei == null) {
      // It's gone!!
      return forwardNoAction;
    }

    BwEvent inboxev = ei.getEvent();
    SchedulingI sched = svc.getScheduler();
    ScheduleResult sr;

    EventInfo ourCopy = sched.getStoredMeeting(inboxev);

    if (inboxev.getScheduleMethod() != Icalendar.methodTypeCounter) {
      boolean update = request.getReqPar("update") != null;

      if (!update) {
        return forwardSuccess;
      }

      sr = sched.processResponse(ei);
      emitScheduleStatus(form, sr, false);

      return forwardSuccess;
    }

    /* Do counter * /

    if (ourCopy == null) {
      form.getErr().emit(ClientError.noMeeting);
      return forwardNoAction;
    }

    String meetingColPath = ourCopy.getEvent().getColPath();
    form.setMeetingCal(svc.getCalendarsHandler().get(meetingColPath));

    BwAttendee att = inboxev.getAttendees().iterator().next();
    if (request.present("decline")) {
      // Just straight to schedule action
      sr = sched.declineCounter(ei,
                                request.getReqPar("comment"),
                                att);

      // delete from inbox
      svc.getEventsHandler().delete(ei, false);
      emitScheduleStatus(form, sr, false);

      return forwardDeclined;
    }

    // straight to edit the event
    EventKey ekey = new EventKey(meetingColPath,
                                 inboxev.getUid(),
                                 inboxev.getRecurrenceId(),
                                 false);

    ei = findEvent(ekey, form);

    if (ei == null) {
      return forwardNoAction;
    }

    /* Update the event we just fetched from the inbox event * /
    update(inboxev, ei.getEvent());

    form.setEventInfo(ei);

    /* Set information to allow edit event page to do refresh correctly * /
    ei.setReplyAttendeeURI(att.getAttendeeUri());
    ei.setInboxEventName(inboxev.getName());

    int fwd = refreshEvent(ei, request);
    form.setAttendees(new Attendees());
    form.setFbResponses(null);
    form.setFormattedFreeBusy(null);
    if (fwd == forwardContinue) {
      resetEvent(form);
    }
*/
    return forwardEdit;
  }

  /*
  private void update(final BwEvent from, final BwEvent to) {
    if (changed(from.getSummary(), to.getSummary())) {
      to.setSummary(from.getSummary());
    }

    if (changed(from.getDescription(), to.getDescription())) {
      to.setDescription(from.getDescription());
    }

    if (changed(from.getDtstart(), to.getDtstart())) {
      to.setDtstart(from.getDtstart());
    }

    if (changed(from.getDtend(), to.getDtend())) {
      to.setDtend(from.getDtend());
    }

    if (changed(from.getEndType(), to.getEndType())) {
      to.setEndType(from.getEndType());
    }

    if (changed(from.getDuration(), to.getDuration())) {
      to.setDuration(from.getDuration());
    }

    if (changed(from.getLink(), to.getLink())) {
      to.setLink(from.getLink());
    }

    if (changed(from.getContact(), to.getContact())) {
      to.setContact((BwContact)from.getContact().clone());
    }

    if (changed(from.getLocation(), to.getLocation())) {
      to.setLocation((BwLocation)from.getLocation().clone());
    }
  }

  private boolean changed(final Object newVal, final Object oldVal) {
    if (newVal == null) {
      return false;
    }

    if (oldVal == null) {
      return true;
    }

    return !newVal.equals(oldVal);
  }
*/
}
