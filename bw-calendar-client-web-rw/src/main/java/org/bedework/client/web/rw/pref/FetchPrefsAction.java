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

package org.bedework.client.web.rw.pref;

import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

/** Fetch preferences.
 *
 * <p>Parameters are:<ul>
 *      <li>"user"            User whos prefs we're changing - superuser only</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such user.</li>
 *      <li>"success"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FetchPrefsAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    final BwSession sess = request.getSess();

    sess.embedCategories(request, false,
                         BwSession.ownersEntity);
    sess.embedCategories(request, true,
                         BwSession.defaultEntity);
    sess.embedAddContentCalendarCollections(request);
    sess.embedLocations(request, BwSession.editableEntity);

    final String str = request.getReqPar("user");
    if (str != null) {
      /* Fetch a given users preferences */
      if (!form.getCurUserSuperUser()) {
        return forwardNoAccess; // First line of defence
      }

      final BwPreferences prefs = cl.getPreferences(str);
      if (prefs == null) {
        return forwardNoAccess;
      }

      form.setUserPreferences(prefs);
    } else {
      /* Just set this users prefs */
      form.setUserPreferences(cl.getPreferences());
    }

    return forwardSuccess;
  }
}
