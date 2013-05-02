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

package org.bedework.webcommon.location;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.BwLocation;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** This action fetches a location.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such event.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FetchLocationAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    /** Check access
     */
    if (getPublicAdmin(form) && !form.getAuthorisedUser()) {
      return forwardNoAccess;
    }

    /** User requested a location from the list. Retrieve it, embed it in
     * the form so we can display the page
     */
    String uid = request.getReqPar("uid");

    BwLocation location = null;
    if (uid != null) {
      location = form.fetchSvci().getLocationsHandler().get(uid);
    }

    if (debug) {
      if (location == null) {
        logIt("No location with id " + uid);
      } else {
        logIt("Retrieved location " + location.getUid());
      }
    }

    form.assignAddingLocation(false);
    form.setLocation(location);

    if (location == null) {
      form.getErr().emit(ClientError.unknownLocation, uid);
      return forwardNotFound;
    }

    return forwardContinue;
  }
}
