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
package org.bedework.webcommon.calendars;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.wrappers.CalendarWrapper;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.util.Set;

/** This action fetches a calendar.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"       user not authorised.</li>
 *      <li>"notFound"       no such calendar.</li>
 *      <li>"continue"       continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class RenderCalendarAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    /** User requested a calendar from the list. Retrieve it, embed it in
     * the form so we can display the page
     */

    String calPath = form.getCalPath();

    if (calPath == null) {
      // bogus request
      form.getErr().emit(ClientError.missingCalendarPath);
      return forwardNotFound;
    }

    Client cl = request.getClient();
    BwCalendar calendar = cl.getCollection(calPath);

    if (debug) {
      if (calendar == null) {
        logIt("No calendar with path " + calPath);
      } else {
        logIt("Retrieved calendar " + calendar.getId());

        Set<String> cos = form.getCalendarsOpenState();

        if ((cos != null) && (calendar instanceof CalendarWrapper)) {
          CalendarWrapper ccw = (CalendarWrapper)calendar;
          ccw.setOpen(cos.contains(calendar.getPath()));
        }
      }
    }

    form.assignAddingCalendar(false);
    form.setCalendarPath(calPath);
    form.setCalendar(calendar);
    //request.getSess().getChildren(cl, calendar);
    request.getSess().embedCategories(request, false);

    if (calendar == null) {
      form.getErr().emit(ClientError.unknownCalendar, calPath);
      return forwardNotFound;
    } else {
      form.assignBeforeCalendar((BwCalendar)calendar.clone());
    }

    return forwardSuccess;
  }
}
