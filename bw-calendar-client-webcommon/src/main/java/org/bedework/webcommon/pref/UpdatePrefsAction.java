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
package org.bedework.webcommon.pref;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwPreferences;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.util.timezones.Timezones;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/** This action updates preferences.
 *
 * <p>Parameters are:<ul>
 *      <li>"user"             User whos prefs we're changing - superuser only</li>
 *      <li>"preferredView"    Name of preferred view</li>
 *      <li>"viewPeriod"       day/week/month/year</li>
 *      <li>"skin"             Name of default skin</li>
 *      <li>"skinStyle"        Name of default skin style</li>

 *      <li>"email"            Email address of user</li>
 *      <li>"calPath"          Path to default calendar</li>
 *      <li>"userMode"         User interface mode</li>
 *      <li>"workDays"         7-character string representing workdays,
 *                             "W" representing each workday, space otherwise;
 *                             e.g. " WWWWW " is a typical Mon-Fri workweek</li>
 *      <li>"workdayStart"     In minutes, e.g. e.g. 14:30 is 870 and 17:30 is 1050</li>
 *      <li>"workdayEnd"       In minutes</li>
 *      <li>"preferredEndType" For adding events: "duration" or "date"</li>
 *      <li>"scheduleAutoRespond"   "true"</li>
 *      <li>"scheduleAutoCancelAction"   index 0 to 2</li>
 *      <li>"scheduleDoubleBook"    "true"/"false"</li>
 *      <li>"scheduleAutoProcessResponses"     Index 0 to 2</li>
 *      <li>"defaultImageDirectory"     Path</li>
 *      <li>"maxEntitySize"     bytes</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"error"        for problems.</li>
 *      <li>"notFound"     no such calendar.</li>
 *      <li>"continue"     continue on to update page.</li>
 *      <li>"delete"       for confirmation.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm   rpi.edu
 */
public class UpdatePrefsAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();
    BwPreferences prefs;
    boolean tzChanged = false;

    /* Refetch the prefs */
    if (cl.getPublicAdmin() && (request.getReqPar("user") != null)) {
      /* Fetch a given users preferences */
      if (!form.getCurUserSuperUser()) {
        return forwardNoAccess; // First line of defence
      }

      prefs = cl.getPreferences(request.getReqPar("user"));
    } else {
      prefs = cl.getPreferences();
    }

    String str = request.getReqPar("defaultImageDirectory");
    if (str != null) {
      BwCalendar cal = cl.getCollection(str);
      if (cal == null) {
        form.getErr().emit(ClientError.unknownCalendar, str);
        return forwardNotFound;
      }
      prefs.setDefaultImageDirectory(str);
      form.assignImageUploadDirectory(str);
    }

    if (form.getCurUserSuperUser()) {
      int maxEntitySize = request.getIntReqPar("maxEntitySize", -1);

      if (maxEntitySize > 0) {
        prefs.setMaxEntitySize(maxEntitySize);
      }
    }

    str = request.getReqPar("preferredView");
    if (str != null) {
      if (cl.getView(str) == null) {
        form.getErr().emit(ClientError.unknownView, str);
        return forwardNotFound;
      }

      prefs.setPreferredView(str);
    }

    str = request.getReqPar("viewPeriod");
    if (str != null) {
      prefs.setPreferredViewPeriod(validViewPeriod(str));
    }

    str = validViewMode(request.getReqPar("defaultViewMode"));
    if (str != null) {
      prefs.setDefaultViewMode(str);
    }

    str = request.getReqPar("defaultTzid");
    if (str != null) {
      if (Timezones.getTz(str) != null) {
        prefs.setDefaultTzid(str);
        tzChanged = true;
      }
    }

    if (request.present("defaultCategory")) {
      str = request.getReqPar("defaultCategory");

      if (str == null) {
        // Remove all
        prefs.removeProperties(BwPreferences.propertyDefaultCategory);
      } else {
        Collection<String> inUids = request.getReqPars("defaultCategory");
        Set<String> uids = new TreeSet<String>();

        for (String uid: inUids) {
          if (uid == null) {
            continue;
          }

          if (cl.getCategory(uid) != null) {
            uids.add(uid);
          }
        }

        prefs.setDefaultCategoryUids(uids);
      }
    }

    if (request.present("hour24")) {
      prefs.setHour24(request.getBooleanReqPar("hour24"));
      form.setHour24(prefs.getHour24());
    }

    str = request.getReqPar("skin");
    if (str != null) {
      prefs.setSkinName(str);
    }

    str = request.getReqPar("skinStyle");
    if (str != null) {
      prefs.setSkinStyle(str);
    }

    str = request.getReqPar("email");
    if (str != null) {
      prefs.setEmail(str);
    }

    str = request.getReqPar("newCalPath");
    if (str != null) {
      BwCalendar cal = cl.getCollection(str);
      if (cal == null) {
        form.getErr().emit(ClientError.unknownCalendar, str);
        return forwardNotFound;
      }
      prefs.setDefaultCalendarPath(cal.getPath());
    }

    int mode = request.getIntReqPar("userMode", -1);

    if (mode != -1) {
      if ((mode < 0) || (mode > BwPreferences.maxMode)) {
        form.getErr().emit(ValidationError.invalidPrefUserMode);
        return forwardBadPref;
      }

      prefs.setUserMode(mode);
    }

    int pageSize = request.getIntReqPar("pageSize", -1);

    if (pageSize != -1) {
      if (pageSize < 0) {
        form.getErr().emit(ValidationError.invalidPageSize);
        return forwardBadPref;
      } else {
        prefs.setPageSize(pageSize);
      }
    }

    str = request.getReqPar("workDays");
    if (str != null) {
      // XXX validate
      prefs.setWorkDays(str);
    }

    int startMinutes = request.getIntReqPar("workdayStart", -1);

    if (startMinutes != -1) {
      if ((startMinutes < 0) || (startMinutes > ((24 * 60) - 1))) {
        form.getErr().emit(ValidationError.invalidPrefWorkDayStart);
        return forwardBadPref;
      }
    } else {
      startMinutes = prefs.getWorkdayStart();
    }

    int endMinutes = request.getIntReqPar("workdayEnd", -1);

    if (endMinutes != -1) {
      if ((endMinutes < 0) || (endMinutes > ((24 * 60) - 1))) {
        form.getErr().emit(ValidationError.invalidPrefWorkDayEnd);
        return forwardBadPref;
      }
    } else {
      endMinutes = prefs.getWorkdayEnd();
    }

    if (startMinutes > endMinutes) {
      form.getErr().emit(ValidationError.invalidPrefWorkDays);
      return forwardBadPref;
    }

    prefs.setWorkdayStart(startMinutes);
    prefs.setWorkdayEnd(endMinutes);

    str = request.getReqPar("preferredEndType");
    if (str != null) {
      if (BwPreferences.preferredEndTypeDuration.equals(str) ||
          BwPreferences.preferredEndTypeDate.equals(str)) {
        prefs.setPreferredEndType(str);
        form.setEndDateType(prefs.getPreferredEndType());
      } else {
        form.getErr().emit(ValidationError.invalidPrefEndType);
        return forwardBadPref;
      }
    }

    Boolean bool = request.getBooleanReqPar("scheduleAutoRespond");
    if (bool != null) {
      prefs.setScheduleAutoRespond(bool);
    }

    int ival = request.getIntReqPar("scheduleAutoCancelAction", -1);

    if (ival != -1) {
      if ((ival < 0) || (ival > BwPreferences.scheduleMaxAutoCancel)) {
        form.getErr().emit(ValidationError.invalidPrefAutoCancel);
        return forwardBadPref;
      }

      prefs.setScheduleAutoCancelAction(ival);
    }

    bool = request.getBooleanReqPar("scheduleDoubleBook");
    if (bool != null) {
      prefs.setScheduleDoubleBook(bool);
    }

    ival = request.getIntReqPar("scheduleAutoProcessResponses", -1);

    if (ival != -1) {
      if ((ival < 0) || (ival > BwPreferences.scheduleMaxAutoProcessResponses)) {
        form.getErr().emit(ValidationError.invalidPrefAutoProcess);
        return forwardBadPref;
      }

      prefs.setScheduleAutoProcessResponses(ival);
    }

    cl.updatePreferences(prefs);
    if (tzChanged) {
      Timezones.setThreadDefaultTzid(prefs.getDefaultTzid());
    }
    form.setUserPreferences(prefs);
    form.getMsg().emit(ClientMessage.updatedPrefs);
    return forwardSuccess;
  }

  private String validViewMode(final String s) {
    if (s == null) {
      return null;
    }

    String ls = s.toLowerCase();

    if ("list".equals(ls)) {
      return ls;
    }

    if ("daily".equals(ls)) {
      return ls;
    }

    if ("grid".equals(ls)) {
      return ls;
    }

    return null;
  }
}
