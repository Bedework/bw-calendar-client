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
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwLocation;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwWebUtil;
import org.bedework.webcommon.BwWebUtil.ValidateResult;

/** This action updates a location.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such location.</li>
 *      <li>"success"      update successful.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@bedework.edu
 */
public class UpdateLocationAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();

    /** Check access
     */
    if (cl.isGuest() ||
        (cl.getPublicAdmin() && !form.getAuthorisedUser())) {
      return forwardNoAccess;
    }

    if (request.getReqPar("delete") != null) {
      return forwardDelete;
    }

    /** Updating location previously fetched
     */
    BwLocation location = form.getLocation();

    boolean add = form.getAddingLocation();

    if (location == null) {
      form.setLocation(null);
      form.getErr().emit(ClientError.unknownLocation);
      return forwardNotFound;
    }

    /* If the location exists use it otherwise add one.
     */

    boolean added = false;

    /** We are just updating from the current form values.
     */
    ValidateResult ver = BwWebUtil.validateLocation(form);
    if (!ver.ok) {
      return forwardRetry;
    }

    if (add) {
      added = cl.addLocation(location);
    } else { // CHGTBL if (ver.changed) {
      /* Ensure we're not duplicating the address field. - we need this for caldav
       * /
      BwLocation dbloc = svci.findLocation(location.getAddress());
      if ((dbloc != null) && (!dbloc.getUid().equals(location.getUid()))) {
        form.getErr().emit(ClientError.duplicatelocation,
                           location.getAddress().getValue());
        return forwardRetry;
      }*/
      cl.updateLocation(location);
    }

    form.assignAddingLocation(false);

    if (add) {
      if (added) {
        form.getMsg().emit(ClientMessage.addedLocations, 1);
      } else {
        form.getErr().emit(ClientError.duplicateLocation);
      }
    } else {
      form.getMsg().emit(ClientMessage.updatedLocation);
    }

    return forwardSuccess;
  }
}
