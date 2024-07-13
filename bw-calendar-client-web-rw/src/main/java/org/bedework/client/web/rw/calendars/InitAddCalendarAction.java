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

package org.bedework.client.web.rw.calendars;

import org.bedework.calfacade.BwCalendar;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

/** This action sets the state ready for adding a calendar.
 *
 * <p>Parameters are:<ul>
 *      <li>"calPath"       Path of the parent to be</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"      user not authorised.</li>
 *      <li>"notAllowed"    cannot add a calendar to that calendar.</li>
 *      <li>"continue"      continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class InitAddCalendarAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    final BwSession sess = request.getSess();
    final BwCalendar cal = request.getCalendar(true);

    if ((cal == null) || !cal.getCollectionInfo().childrenAllowed) {
      return forwardNotAllowed;
    }

    form.setParentCalendarPath(cal.getPath());

    /* Set the objects to null so we get new ones.
     */
    form.setCalendar(null);
    form.assignAddingCalendar(true);

    sess.embedCategories(request, false, BwSession.ownersEntity);

    return forwardContinue;
  }
}

