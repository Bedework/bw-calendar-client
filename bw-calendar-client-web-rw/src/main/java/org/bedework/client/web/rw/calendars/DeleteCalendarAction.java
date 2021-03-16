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

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.BwView;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

import java.util.List;

/** This action deletes a calendar.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorized.</li>
 *      <li>"inUse" calendar is referenced by an event.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class DeleteCalendarAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) throws Throwable {
    final String calPath = form.getCalendarPath();
    final BwCalendar cal = cl.getCollection(calPath);
    if (cal == null) {
      request.error(ClientError.unknownCalendar, calPath);
      return forwardNotFound;
    }

    final boolean publick = cal.getPublick();

    if (cal.equals(cl.getHome())) {
      request.error(ClientError.cannotDeleteHome, calPath);
      return forwardInUse;
    }

    if (cal.getUnremoveable() && !cl.isSuperUser()) {
      // Only super user can remove the unremovable
      return forwardNoAccess;
    }

    /* Check for references in views. For user extra simple mode only we will
     * automatically remove the subscription. For others we list the references
     */

    boolean reffed = false;
    final boolean autoRemove = !cl.getPublicAdmin() &&
      (cl.getPreferences().getUserMode() == BwPreferences.basicMode);

    for (final BwView v: cl.getAllViews()) {
      final List<String> paths = v.getCollectionPaths();

      if ((paths != null) && paths.contains(cal.getPath())) {
        if (autoRemove) {
          if (!cl.removeViewCollection(v.getName(),
                                       cal.getPath())) {
            request.error(ClientError.unknownView, v.getName());
            return forwardError;
          }
        } else {
          request.error(ClientError.referencedSubscription, v.getName());
          reffed = true;
        }
      }
    }

    if (reffed) {
      return forwardReffed;
    }

    try {
      if (!cl.deleteCollection(cal,
                               request.getBooleanReqPar("deleteContent", false))) {
        request.error(ClientError.unknownCalendar, calPath);
        return forwardNotFound;
      }
    } catch (final CalFacadeException cfe) {
      if (CalFacadeException.collectionNotEmpty.equals(cfe.getMessage())) {
        request.error(ClientError.referencedCalendar, calPath);
        return forwardInUse;
      }

      if (CalFacadeException.cannotDeleteDefaultCalendar.equals(cfe.getMessage())) {
        request.error(ClientError.referencedCalendar, "default calendar");
        return forwardInUse;
      }

      throw cfe;
    }

    request.message(ClientMessage.deletedCalendar, calPath);

    if (publick) {
      request.getSess().flushPublicCache();
    }

    return forwardContinue;
  }
}

