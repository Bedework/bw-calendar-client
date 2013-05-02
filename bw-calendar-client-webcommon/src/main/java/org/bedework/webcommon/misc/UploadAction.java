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

import org.bedework.appcommon.CheckData;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.BwAlarm;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.EventInfo.UpdateResult;
import org.bedework.calsvci.CalSvcI;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.icalendar.Icalendar;
import org.bedework.webcommon.AddEventResult;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import edu.rpi.cmt.calendar.ScheduleMethods;

import org.apache.struts.upload.FormFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Action to upload an icalendar file.
 * <p>Parameters are:<ul>
 *      <li>"newCalPath"            Path of calendar</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"retry"        email options still not valid.</li>
 *      <li>"noEvent"      no event was selected.</li>
 *      <li>"success"      mailed (or queued) ok.</li>
 * </ul>
 */
public class UploadAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }

    String transparency = request.getReqPar("transparency");
    if (!CheckData.checkTransparency(transparency)) {
      form.getErr().emit(ValidationError.invalidTransparency, transparency);
      return forwardRetry;
    }

    String status = request.getReqPar("status");
    if (!CheckData.checkStatus(status)) {
      form.getErr().emit(ValidationError.invalidStatus, status);
      return forwardRetry;
    }

    boolean stripAlarms = request.getBooleanReqPar("stripAlarms", false);

    CalSvcI svci = form.fetchSvci();
    BwCalendar col = null;

    String newCalPath = request.getReqPar("newCalPath");

    if (newCalPath != null) {
      col = svci.getCalendarsHandler().get(newCalPath);
    }

    if (col == null) {
      if (getPublicAdmin(form)) {
        // Must specify a calendar for public events
        form.getErr().emit(ValidationError.missingCalendar);
        return forwardRetry;
      }

      // Use preferred calendar
      col = svci.getCalendarsHandler().getPreferred();
    }

    FormFile upFile = form.getUploadFile();

    if (upFile == null) {
      // Just forget it
      return forwardSuccess;
    }

    String fileName = upFile.getFileName();

    if ((fileName == null) || (fileName.length() == 0)) {
      form.getErr().emit(ClientError.missingFileName, 1);
      return forwardRetry;
    }

    int numEventsAdded = 0;
    int numEventsUpdated = 0;
    int numFailedOverrides = 0;

    try {
      // To catch some of the parser errors

      InputStream is = upFile.getInputStream();

      IcalTranslator trans = new IcalTranslator(svci.getIcalCallback());

      Icalendar ic = trans.fromIcal(col, new InputStreamReader(is));

      int method = ic.getMethodType();

      if (!getPublicAdmin(form) &&
          (method != ScheduleMethods.methodTypePublish) &&
          (Icalendar.itipReplyMethodType(method) ||
           Icalendar.itipRequestMethodType(method))) {
        return importScheduleMessage(form, ic, col, stripAlarms);
      }

      Collection<AddEventResult> aers = new ArrayList<AddEventResult>();
      form.setAddEventResults(aers);

      Iterator it = ic.iterator();

      while (it.hasNext()) {
        Object o = it.next();

        if (!(o instanceof EventInfo)) {
          continue;
        }

        EventInfo ei = (EventInfo)o;
        BwEvent ev = ei.getEvent();

        /* Make up a unique name for the event. */
        ev.setName(ev.getUid() + ".ics");

        if (transparency != null) {
          ev.setTransparency(transparency);
        }

        if (status != null) {
          ev.setStatus(status);
        }

        if (stripAlarms) {
          Set<BwAlarm> alarms = ev.getAlarms();
          if (alarms != null) {
            alarms.clear();
          }
        }

        ev.setScheduleMethod(ScheduleMethods.methodTypeNone);
        ev.setColPath(col.getPath());

        if (ei.getNewEvent()) {
          try {
            UpdateResult eur = svci.getEventsHandler().add(ei, true,
                                                           false, false);

            AddEventResult aer = new AddEventResult(ev,
                                                    eur.failedOverrides);
            aers.add(aer);

            if (eur.failedOverrides != null) {
              numFailedOverrides += eur.failedOverrides.size();
            }

            numEventsAdded++;
          } catch (CalFacadeException cfe) {
            if (!cfe.getMessage().equals(CalFacadeException.noRecurrenceInstances)) {
              throw cfe;
            }
            form.getErr().emit(cfe.getMessage(), cfe.getExtra());
          }
        } else {
          svci.getEventsHandler().update(ei, false);
          numEventsUpdated++;
        }
      }
    } catch (CalFacadeException cfe) {
      if (debug) {
        cfe.printStackTrace();
      }
      form.getErr().emit(cfe.getMessage(), cfe.getExtra());
      return forwardBadData;
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }

    if (numFailedOverrides > 0) {
      form.getErr().emit(ClientError.failedOverrides, numFailedOverrides);
    }

    form.getMsg().emit(ClientMessage.addedEvents, numEventsAdded);
    form.getMsg().emit(ClientMessage.updatedEvents, numEventsUpdated);

    return forwardSuccess;
  }

  private int importScheduleMessage(final BwActionFormBase form,
                                    final Icalendar ic,
                                    final BwCalendar cal,
                                    final boolean stripAlarms) throws Throwable {
    CalSvcI svci = form.fetchSvci();

    // Scheduling method - should contain a single entity

    if (ic.size() != 1) {
      form.getErr().emit(ValidationError.invalidSchedData);
      return forwardRetry;
    }

    @SuppressWarnings("rawtypes")
    Iterator it = ic.iterator();
    Object o = it.next();

    if (o instanceof EventInfo) {
      EventInfo ei = (EventInfo)o;

      /* Clone it because we may have an updated version of the event in the
       * default calendar.
       */
      // RECUR - don't think cloning works for recurrences
      BwEvent ev = (BwEvent)ei.getEvent().clone();

      BwOrganizer org = ev.getOrganizer();

      if (org == null) {
        form.getErr().emit(ValidationError.missingOrganizer);
        return forwardRetry;
      }

      String userUri = svci.getDirectories().principalToCaladdr(svci.getPrincipal());

      boolean isOrganizer = userUri.equals(org.getOrganizerUri());
      ev.setOrganizerSchedulingObject(isOrganizer);
      ev.setAttendeeSchedulingObject(!isOrganizer);

      //ev.setOriginator(org.getOrganizerUri());

      ev.setScheduleMethod(ic.getMethodType());
      ev.updateStag(svci.getCurrentTimestamp());

      /* Make up a unique name for the event. */
      ev.setName(ev.getUid() + ".ics");

      ev.setColPath(cal.getPath());

      ei = new EventInfo(ev); // RECUR - see above

      if (stripAlarms) {
        Set<BwAlarm> alarms = ev.getAlarms();
        if (alarms != null) {
          alarms.clear();
        }
      }

      svci.getEventsHandler().add(ei, false, false, true);

      form.getMsg().emit(ClientMessage.addedEvents, 1);
    }

    return forwardSuccess;
  }
}
