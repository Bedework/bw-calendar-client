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

import org.bedework.calfacade.BwCalendar;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

import java.util.HashSet;
import java.util.Set;

/** This action sets the open close state for a calendar.
 *
 * <p>Request parameters:<ul>
 *      <li>"open"     true/false.</li>
 *      <li>"calPath"  path to change.</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"notFound"    bad path.</li>
 *      <li>"success"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm - rpi.edu
 */
public class OpenCloseAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) {
    final var cl = request.getClient();
    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defense
    }

    final BwCalendar cal = request.getCalendar(true);
    if (cal == null) {
      return forwardNotFound;
    }

    final String path = cal.getPath();

    if ((cal.getCalType() != BwCalendar.calTypeAlias) &&
        !cal.getCollectionInfo().childrenAllowed) {
      // Ignore - we have no open close state for these
      return forwardSuccess;
    }

    final boolean open = request.getBooleanReqPar("open", true);

    Set<String> cos = form.getCalendarsOpenState();
    if ((cos == null) && !open) {
      return forwardSuccess;
    }

    if (cos == null) {
      cos = new HashSet<>();
      form.setCalendarsOpenState(cos);
    }

    if (open) {
      cos.add(path);
    } else {
      cos.remove(path);
    }

    final var sess = request.getSess();
    if (cl.getPublicAdmin()) {
      sess.embedCollections(request);
    }
    sess.embedUserCollections(request);
    sess.embedCategories(request, false,
                         BwSession.ownersEntity);

    return forwardSuccess;
  }
}

