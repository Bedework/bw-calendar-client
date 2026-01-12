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
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

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
  @Override
  public String doAction(final BwRequest request) {
    /* User requested a calendar from the list. Retrieve it, embed it in
     * the form so we can display the page
     */

    final var form = request.getBwForm();
    final var calPath = form.getCalPath();

    if (calPath == null) {
      // bogus request
      request.error(ClientError.missingCalendarPath);
      return forwardNotFound;
    }

    final var cl = request.getClient();
    final var calendar = cl.getCollection(calPath);

    if (debug()) {
      debug("Retrieved calendar " + calPath);
    }

    form.assignAddingCalendar(false);
    form.setCalendarPath(calPath);
    form.setCalendar(calendar);
    //request.getSess().getChildren(cl, calendar);
    request.getSess().embedCategories(request, false,
                                      BwSession.ownersEntity);

    if (calendar == null) {
      request.error(ClientError.unknownCalendar, calPath);
      return forwardNotFound;
    }

    final var cos = form.getCalendarsOpenState();

    if (cos != null) {
      calendar.setOpen(cos.contains(calPath));
    }

    final var ssr = request.getClient().getSynchStatus(calendar);
    request.setSessionAttr(BwRequest.bwSubscriptionStatus, ssr);

    return forwardSuccess;
  }
}
