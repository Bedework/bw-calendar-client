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
import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.BwAlarm;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.EventState;

import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.property.Trigger;

/**
 * Action to set an alarm.
 * <p>No request parameters (other than updates to email and subject)
 * <p>Forwards to:<ul>
 *      <li>"retry"        email options still not valid.</li>
 *      <li>"noEvent"      no event was selected.</li>
 *      <li>"expired"      Event has already taken place.</li>
 *      <li>"success"      mailed (or queued) ok.</li>
 * </ul>
 */
public class SetAlarmAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) throws Throwable {
    final BwEvent ev = form.getEvent();
    final EventState evstate = form.getEventState();

    if (ev == null) {
      return forwardNoAction;
    }

    final BwAlarm alarm = new BwAlarm();

    final Trigger tr;
    //boolean trDuration = false;

    if (evstate.getAlarmTriggerByDate()) {
      /*XXX this needs changing */
      throw new Exception("Unimplemented");
//      tr = new Trigger(form.getTriggerDateTime().getDateTime());
    } else {
      //trDuration = true;

      final Related rel;
      if (evstate.getAlarmRelStart()) {
        rel = Related.START;
      } else {
        rel = Related.END;
      }
      final ParameterList plist = new ParameterList();
      plist.add(rel);
      tr = new Trigger(plist, evstate.getTriggerDuration().toString());
      tr.setValue(evstate.getTriggerDuration().toString());
    }

    String recipient = request.getReqPar("lastEmail");
    if (!Util.present(recipient)) {
      recipient = cl.getPreferences().getEmail();
    }

    if (!Util.present(recipient)) {
      form.getErr().emit(ClientError.mailNoRecipient, 1);
      return forwardRetry;
    }

    String subject = request.getReqPar("subject");
    if (!Util.present(subject)) {
      subject = ev.getSummary();
    }

    /* temporarily just fix the alarm time as 5 mins before the associated event.
     * start. We need to add options for start or end time, period, how often etc.
     */

    alarm.setAlarmType(BwAlarm.alarmTypeEmail);
    alarm.setTrigger(tr.getValue());
    alarm.setTriggerStart(true);
    alarm.setSummary(subject);
    alarm.setDescription(ev.getDescription());

    final BwAttendee att = new BwAttendee();

    att.setAttendeeUri(cl.getCalendarAddress(recipient));

    alarm.addAttendee(att);

    alarm.setOwnerHref(cl.getCurrentPrincipalHref());

    ev.addAlarm(alarm);
    final var ueres =
            cl.updateEvent(new EventInfo(ev), true, null, false);
    if (!ueres.isOk()) {
      form.getErr().emit(ueres.getMessage());
      return forwardError;
    }

    form.getMsg().emit(ClientMessage.setAlarm);

    request.setRequestAttr(BwRequest.eventStateName,
                           evstate);

    return forwardSuccess;
  }
}
