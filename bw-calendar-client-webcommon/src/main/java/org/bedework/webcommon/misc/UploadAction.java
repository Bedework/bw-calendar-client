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

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.calfacade.BwAlarm;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.EventInfo.UpdateResult;
import org.bedework.client.rw.RWClient;
import org.bedework.convert.IcalTranslator;
import org.bedework.convert.Icalendar;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.webcommon.AddEventResult;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwWebUtil;

import org.apache.struts.upload.FormFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
  @SuppressWarnings("rawtypes")
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }

    final String transparency = request.getReqPar("transparency");
    if (!BwWebUtil.checkTransparency(transparency)) {
      form.getErr().emit(ValidationError.invalidTransparency, transparency);
      return forwardRetry;
    }

    final String status = request.getReqPar("status");
    if (!BwWebUtil.checkStatus(status)) {
      form.getErr().emit(ValidationError.invalidStatus, status);
      return forwardRetry;
    }

    final boolean stripAlarms = request.getBooleanReqPar("stripAlarms", false);

    final RWClient cl = (RWClient)request.getClient();

    final Map<String, String> paths = new HashMap<>();

    final String newCalPath = request.getReqPar("newCalPath");

    final FormFile upFile = form.getUploadFile();

    if (upFile == null) {
      // Just forget it
      return forwardSuccess;
    }

    final String fileName = upFile.getFileName();

    if ((fileName == null) || (fileName.length() == 0)) {
      form.getErr().emit(ClientError.missingFileName, 1);
      return forwardRetry;
    }

    int numEventsAdded = 0;
    int numEventsUpdated = 0;
    int numFailedOverrides = 0;

    BwCalendar col = null;

    try {
      // To catch some of the parser errors

      final InputStream is = upFile.getInputStream();

      final IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(cl));

      final Icalendar ic = trans.fromIcal(null, new InputStreamReader(is));

      final int method = ic.getMethodType();

      if (!cl.getPublicAdmin() &&
          (method != ScheduleMethods.methodTypePublish) &&
          (Icalendar.itipReplyMethodType(method) ||
           Icalendar.itipRequestMethodType(method))) {
        return importScheduleMessage(request, ic, null, stripAlarms);
      }

      final Collection<AddEventResult> aers = new ArrayList<>();
      form.setAddEventResults(aers);

      final Iterator it = ic.iterator();

      while (it.hasNext()) {
        final Object o = it.next();

        if (!(o instanceof EventInfo)) {
          continue;
        }

        final EventInfo ei = (EventInfo)o;
        final BwEvent ev = ei.getEvent();

        /* Make up a unique name for the event. */
        ev.setName(ev.getUid() + ".ics");

        if (transparency != null) {
          ev.setTransparency(transparency);
        }

        if (status != null) {
          ev.setStatus(status);
        }

        if (stripAlarms) {
          final Set<BwAlarm> alarms = ev.getAlarms();
          if (alarms != null) {
            alarms.clear();
          }
        }

        if (newCalPath != null) {
          if (col == null) {
            col = cl.getCollection(newCalPath);

            if (col == null) {
              form.getErr().emit(ValidationError.missingCalendar);
              return forwardRetry;
            }
          }

          ev.setColPath(col.getPath());
        } else {
          final String icalName =
                  IcalDefs.entityTypeIcalNames[ev.getEntityType()];

          String path = paths.get(icalName);

          if (path == null) {
            path = cl.getPreferredCollectionPath(icalName);

            if (path == null) {
              form.getErr().emit(ValidationError.missingCalendar);
            }
          }

          paths.put(icalName, path);

          ev.setColPath(path);
        }

        col = cl.getCollection(newCalPath);

        if (col == null) {
          form.getErr().emit(ValidationError.missingCalendar);
        }

        ev.setScheduleMethod(ScheduleMethods.methodTypeNone);

        if (ei.getNewEvent()) {
          final UpdateResult eur = cl.addEvent(ei, true,
                                               false);

          final AddEventResult aer =
                  new AddEventResult(ev,
                                     eur.failedOverrides);
          aers.add(aer);

          if (eur.failedOverrides != null) {
            numFailedOverrides += eur.failedOverrides.size();
          }

          numEventsAdded++;

          /*TODO Used to catch exception here and watch for this:
          } catch (CalFacadeException cfe) {
            if (!cfe.getMessage().equals(CalFacadeException.noRecurrenceInstances)) {
              throw cfe;
            }
            form.getErr().emit(cfe.getMessage(), cfe.getExtra());
          }*/
        } else {
          final var ueres =
                  cl.updateEvent(ei, false, null, false);
          if (!ueres.isOk()) {
            form.getErr().emit(ueres.getMessage());
            return forwardError;
          }
          numEventsUpdated++;
        }
      }
    } catch (final CalFacadeException cfe) {
      if (debug()) {
        cfe.printStackTrace();
      }
      form.getErr().emit(cfe.getMessage(), cfe.getExtra());
      return forwardBadData;
    } catch (final Throwable t) {
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

  private int importScheduleMessage(final BwRequest request,
                                    final Icalendar ic,
                                    final BwCalendar cal,
                                    final boolean stripAlarms) {
    final RWClient cl = (RWClient)request.getClient();

    // Scheduling method - should contain a single entity

    if (ic.size() != 1) {
      request.getErr().emit(ValidationError.invalidSchedData);
      return forwardRetry;
    }

    @SuppressWarnings("rawtypes")
    final Iterator it = ic.iterator();
    final Object o = it.next();

    if (o instanceof EventInfo) {
      EventInfo ei = (EventInfo)o;

      /* Clone it because we may have an updated version of the event in the
       * default calendar.
       */
      // RECUR - don't think cloning works for recurrences
      final BwEvent ev = (BwEvent)ei.getEvent().clone();

      final BwOrganizer org = ev.getOrganizer();

      if (org == null) {
        request.getErr().emit(ValidationError.missingOrganizer);
        return forwardRetry;
      }

      final String userUri = cl.getCurrentCalendarAddress();

      final boolean isOrganizer = userUri.equals(org.getOrganizerUri());
      ev.setOrganizerSchedulingObject(isOrganizer);
      ev.setAttendeeSchedulingObject(!isOrganizer);

      //ev.setOriginator(org.getOrganizerUri());

      ev.setScheduleMethod(ic.getMethodType());

      /* Make up a unique name for the event. */
      ev.setName(ev.getUid() + ".ics");

      ev.setColPath(cal.getPath());

      ei = new EventInfo(ev); // RECUR - see above

      if (stripAlarms) {
        final Set<BwAlarm> alarms = ev.getAlarms();
        if (alarms != null) {
          alarms.clear();
        }
      }

      cl.addEvent(ei, false, true);

      request.getMsg().emit(ClientMessage.addedEvents, 1);
    }

    return forwardSuccess;
  }
}
