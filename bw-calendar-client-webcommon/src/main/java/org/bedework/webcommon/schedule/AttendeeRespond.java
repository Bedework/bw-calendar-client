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

import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventProxy;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.ifs.Directories;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.EventInfo.UpdateResult;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.SchedulingI;
import org.bedework.icalendar.Icalendar;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwWebUtil;
import org.bedework.webcommon.EventKey;
import org.bedework.webcommon.event.EventActionBase;

import edu.rpi.cmt.calendar.IcalDefs;
import edu.rpi.cmt.calendar.ScheduleMethods;

import java.util.Collection;

/**
 * Action to handle scheduling requests - that is the schedule method was
 * REQUEST or CANCEL.
 *
 * <p>For REQUEST we, as an attendee are going to respond to that request.
 *
 * <p>For CANCEL we, as an attendee will update or delete a copy of the event.
 *
 * <p>The incoming event is currently set in the form event property.
 *
 * The allowable request parameters and actions taken depend upon the
 * scheduling METHOD value sent in the request "method" parameter or on the
 * method set in the event.
 *
 * The values or the "method" request parameter are:<br/>
 *  <ul>
 *    <li><em>REFRESH</em>          User want organizer to refresh the event.</li>
 * </ul>
 *
 * The mthods set in the event are
 *  <ul>
 *    <li><em>REPLY</em>            User want to reply to the event.</li>
 *    <li><em>COUNTER</em>          User want to change event properties.</li>
 *    <li><em>DELEGATE</em>         User want to delegate another user as
 *                                  an attendee.</li>
 * </ul>
 *
 * <p>Request parameters<ul>
 *      <li>initUpdate      as an attendee we want to update our status.
 *                          Set up a copy of the event ready for that action.</li>
 *      <li>comment         To set the comment in the response.</li>
 *      <li>newCalPath      Where to put the event.</li>
 *      <li>subname         Specify where to put the event using subscription.</li>
 *      <li>comment         To set the comment in the response.</li>
 *      <li>partstat        to change/set participation status.</li>
 *      <li>delegate        uri or account for delegate.</li>
 *      <li>rsvp            true or false.</li>
 * </ul>
 *.
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     for guest mode</li>
 *      <li>"noAction"     for invalid</li>
 *      <li>"success"      changes made.</li>
 * </ul>
 */
public class AttendeeRespond extends EventActionBase {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }

    CalSvcI svc = form.fetchSvci();

    SchedulingI sched = svc.getScheduler();

    if (request.present("initUpdate")) {
//      ei = sched.initAttendeeUpdate(ei);
      EventInfo ei = form.getEventInfo();
      if (ei == null) {
        // It's gone!!
        return forwardNoAction;
      }

      form.setEventKey(new EventKey(ei.getEvent(), true));

      return forwardSuccess;
    }

    EventInfo ei = refetchEvent(form);
    if (ei == null) {
      // It's gone!!
      return forwardNoAction;
    }

    BwEvent ev = ei.getEvent();

    String methStr = request.getReqPar("method");

    if ("REFRESH".equals(methStr)) {
      ScheduleResult sr = sched.requestRefresh(ei,
                                               request.getReqPar("comment"));
      emitScheduleStatus(form, sr, false);

      return forwardSuccess;
    }

    int method = ev.getScheduleMethod();

    if (method == ScheduleMethods.methodTypeCancel) {
      //ScheduleResult sr = sched.processCancel(ei);
      //emitScheduleStatus(form, sr, false);

      return forwardSuccess;
    }

    if ("COUNTER".equals(methStr)) {
      /* Update the event from the incoming request parameters */
      boolean publicAdmin = getPublicAdmin(form);

      /* ------------------------ Text fields ------------------------------ */
      setEventText(request, ev, true, null);

      /* -------------------------- Dates ------------------------------ */
      int res = form.getEventDates().updateEvent(ei);
      if (res == forwardValidationError) {
        return res;
      }

      //if (res == forwardUpdated) {
      //  incSequence = true;
     // }

      /* -------------------------- Location ------------------------------ */
      if (publicAdmin) {
        if (!adminEventLocation(form, ei)) {
          return forwardValidationError;
        }
      } else {
        if (setEventLocation(ei, form, false)) {
          // RFC says maybe for this.
          //incSequence = true;
        }
      }

      /* -------------------------- Contact ------------------------------ */
      if (publicAdmin) {
        if (!setEventContact(form, false)) {
          return forwardValidationError;
        }
      }

      /* -------------------------- Categories ------------------------------ */
      SetEntityCategoriesResult secr = setEntityCategories(request, null,
                                                           ev, null);
      if (secr.rcode != forwardSuccess) {
        return secr.rcode;
      }
    }

    /* ------------------ final validation -------------------------- */

    Collection<ValidationError>  ves = BwWebUtil.validateEvent(svc,
                                                               false,
                                                               false,
                                                               ev);

    if (ves != null) {
      for (org.bedework.calfacade.exc.ValidationError ve: ves) {
        form.getErr().emit(ve.getErrorCode(), ve.getExtra());
      }
      return forwardValidationError;
    }

//    EventInfo ourCopy = sched.getStoredMeeting(ev);

    /* The event should have a calendar set to the inbox it came from.
     * That inbox may be owned by somebody other than the current user if a
     * calendar user has delegated control of their inbox to some other user
     * e.g. secretary.
     */

    String partStat = request.getReqPar("partstat");

    setupAttendeeRespond(svc,
                         ei,
                         request.getReqPar("delegate"),        // delegate
                         methStr,      // method
                         partStat,
                         request.getReqPar("comment"),         // comment
                         request.getBooleanReqPar("rsvp", false));

    UpdateResult ur = svc.getEventsHandler().update(ei,
                                                    false,
                                                    null);

    emitScheduleStatus(form, ur.schedulingResult, false);

    return forwardSuccess;
  }

  private String setupAttendeeRespond(final CalSvcI svc,
                                      final EventInfo ei,
                                      final String delegate,
                                      final String meth,
                                      final String partStat,
                                      final String orgComment,
                                      final boolean rsvp) throws CalFacadeException {
    BwEvent ev = ei.getEvent();
    BwEventProxy proxy = null;
    if (ev instanceof BwEventProxy) {
      proxy = (BwEventProxy)ev;
    }

    if (!ev.getAttendeeSchedulingObject()) {
      return CalFacadeException.schedulingBadMethod;
    }

    /* Check that the current user is actually an attendee
     */
    Directories dir = svc.getDirectories();
    String uri = dir.principalToCaladdr(svc.getPrincipal());
    BwAttendee att = ev.findAttendee(uri);

    if (att == null) {
      return CalFacadeException.schedulingNotAttendee;
    }

    if (ev.getOriginator() == null) {
      return CalFacadeException.schedulingNoOriginator;
    }

    int method = ScheduleMethods.methodTypeNone;

    if (delegate == null) {
      method = Icalendar.findMethodType(meth);

      if (!Icalendar.itipReplyMethodType(method)) {
        return CalFacadeException.schedulingBadResponseMethod;
      }
    }

    ev.setScheduleMethod(method);

    if (delegate != null) {
      /* RFC 2446 4.2.5 - Delegating an event
       *
       * When delegating an event request to another "Calendar User", the
       * "Delegator" must both update the "Organizer" with a "REPLY" and send
       * a request to the "Delegate". There is currently no protocol
       * limitation to delegation depth. It is possible for the original
       * delegate to delegate the meeting to someone else, and so on. When a
       * request is delegated from one CUA to another there are a number of
       * responsibilities required of the "Delegator". The "Delegator" MUST:
       *
       *   .  Send a "REPLY" to the "Organizer" with the following updates:
       *   .  The "Delegator's" "ATTENDEE" property "partstat" parameter set
       *      to "delegated" and the "delegated-to" parameter is set to the
       *      address of the "Delegate"
       *   .  Add an additional "ATTENDEE" property for the "Delegate" with
       *      the "delegated-from" property parameter set to the "Delegator"
       *   .  Indicate whether they want to continue to receive updates when
       *      the "Organizer" sends out updated versions of the event.
       *      Setting the "rsvp" property parameter to "TRUE" will cause the
       *      updates to be sent, setting it to "FALSE" causes no further
       *      updates to be sent. Note that in either case, if the "Delegate"
       *      declines the invitation the "Delegator" will be notified.
       *   .  The "Delegator" MUST also send a copy of the original "REQUEST"
       *      method to the "Delegate".
       */
      String calAddr = svc.getDirectories().uriToCaladdr(delegate);
      if (calAddr == null) {
        return ValidationError.invalidUser;
      }

      att.setPartstat(IcalDefs.partstatValDelegated);
      att.setDelegatedTo(calAddr);

      /* The attendee may indicate they wish to continue to receive
       * notification by setting rsvp = true
       */
      att.setRsvp(rsvp);
    } else if (method == ScheduleMethods.methodTypeReply) {
      // Expect a valid partstat for the attendee corresponding to the inbox
      // the event came from.
      int pStat = IcalDefs.checkPartstat(partStat);

      if ((pStat != IcalDefs.partstatAccepted) &&
          (pStat != IcalDefs.partstatDeclined) &&
          (pStat != IcalDefs.partstatTentative)) {
        return CalFacadeException.schedulingInvalidPartStatus;
      }

      // Update the status - will affect incoming event object.
      if (proxy != null) {
        att = (BwAttendee)att.clone();
        ev.removeAttendee(att);
        ev.addAttendee(att);
      }

      att.setPartstat(partStat);
    } else if (method == ScheduleMethods.methodTypeCounter) {
    } else {
      throw new RuntimeException("Never get here");
    }

    return null;
  }
}
