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

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.BwAlarm;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calsvci.CalSvcI;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import edu.rpi.sss.util.Util;

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
public class SetAlarmAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    BwEvent ev = form.getEvent();

    if (ev == null) {
      return forwardNoAction;
    }

    CalSvcI svci = form.fetchSvci();

    BwAlarm alarm = new BwAlarm();

    Trigger tr;
    //boolean trDuration = false;

    if (form.getAlarmTriggerByDate()) {
      /*XXX this needs changing */
      throw new Exception("Unimplemented");
//      tr = new Trigger(form.getTriggerDateTime().getDateTime());
    } else {
      //trDuration = true;

      Related rel;
      if (form.getAlarmRelStart()) {
        rel = Related.START;
      } else {
        rel = Related.END;
      }
      ParameterList plist = new ParameterList();
      plist.add(rel);
      tr = new Trigger(plist, form.getTriggerDuration().toString());
      tr.setValue(form.getTriggerDuration().toString());
    }

    String recipient = form.getLastEmail();
    if (!Util.present(recipient)) {
      form.getErr().emit(ClientError.mailNoRecipient, 1);
      return forwardRetry;
    }

    String subject = form.getLastSubject();
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

    BwAttendee att = new BwAttendee();

    att.setAttendeeUri(svci.getDirectories().userToCaladdr(recipient));

    alarm.addAttendee(att);

    alarm.setEvent(ev);
    alarm.setOwnerHref(svci.getPrincipal().getPrincipalRef());

    ev.addAlarm(alarm);
    svci.getEventsHandler().update(new EventInfo(ev), true, null);

    form.getMsg().emit(ClientMessage.setAlarm);

    return forwardSuccess;
  }
}
