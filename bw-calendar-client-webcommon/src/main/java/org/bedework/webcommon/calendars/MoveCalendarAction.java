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
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwPreferences;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwView;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.util.List;

/** This action moves a calendar or the contents.
 *
 * <p>Parameters are:<ul>
 *      <li>"calPath"       Path of the collection to move</li>
 *      <li>"newCalPath"       Path of the parent to be</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorized.</li>
 *      <li>"inUse" calendar is referenced by an event.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class MoveCalendarAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defense
    }

    Client cl = form.fetchClient();

    boolean contents = request.present("contents");

    BwCalendar cal = request.getCalendar(true);
    BwCalendar newCal = request.getNewCal(true);

    if (cal == null) {
      return forwardNotFound;
    }

    if (!contents && cal.equals(cl.getHome())) {
      form.getErr().emit(ClientError.cannotMoveHome, cal.getPath());
      return forwardInUse;
    }

    if (cal.getUnremoveable() && !form.getCurUserSuperUser()) {
      // Only super user can move the unremovable
      return forwardNoAccess;
    }

    /* redo filters */
    cl.flushState();

    if (!contents) {
      return moveCollection(cal, newCal, form);
    }

    cl.moveContents(cal, newCal);

    return forwardContinue;
  }

  private int moveCollection(final BwCalendar cal,
                             final BwCalendar newCal,
                             final BwActionFormBase form) throws Throwable {
    /* Check for references in views. For user extra simple mode only we will
     * automatically remove the subscription. For others we list the references
     */

    Client cl = form.fetchClient();

    boolean reffed = false;
    boolean autoRemove = !getPublicAdmin(form) &&
      (cl.getPreferences().getUserMode() == BwPreferences.basicMode);

    for (BwView v:  cl.getAllViews()) {
      List<String> paths = v.getCollectionPaths();

      if ((paths != null) && paths.contains(cal.getPath())) {
        if (autoRemove) {
          if (!cl.removeViewCollection(v.getName(), cal.getPath())) {
            form.getErr().emit(ClientError.unknownView, v.getName());
            return forwardError;
          }
        } else {
          form.getErr().emit(ClientError.referencedSubscription, v.getName());
          reffed = true;
        }
      }
    }

    if (reffed) {
      return forwardReffed;
    }

    try {
      cl.moveCollection(cal, newCal);
    } catch (CalFacadeException cfe) {
      if (CalFacadeException.cannotDeleteDefaultCalendar.equals(cfe.getMessage())) {
        form.getErr().emit(ClientError.referencedCalendar, "default calendar");
        return forwardInUse;
      }

      throw cfe;
    }

    form.getMsg().emit(ClientMessage.movedCalendar, cal.getPath());

    return forwardContinue;
  }
}

