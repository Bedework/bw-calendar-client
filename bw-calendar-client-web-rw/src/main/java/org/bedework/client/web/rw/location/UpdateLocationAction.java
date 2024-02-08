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

package org.bedework.client.web.rw.location;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.BwEventProperty;
import org.bedework.calfacade.BwLocation;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.EventProps.ValidateResult;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

import static org.bedework.client.web.rw.EventProps.validateLocation;

/** This action updates a location.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such location.</li>
 *      <li>"success"      update successful.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class UpdateLocationAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    if (request.getReqPar("delete") != null) {
      return forwardDelete;
    }

    /* Updating location previously fetched
     */
    final BwLocation location = form.getLocation();

    final boolean add = form.getAddingLocation();

    if (location == null) {
      form.setLocation(null);
      request.error(ClientError.unknownLocation);
      return forwardNotFound;
    }

    /* If the location exists use it otherwise add one.
     */

    boolean added = false;

    /* We are updating from the current form values or setting keys.
     */

    final String keyName = request.getReqPar("keyName");

    if (keyName != null) {
      final String keyValue = request.getReqPar("keyValue");

      if (keyValue == null) {
        location.delKey(keyName);
      } else {
        location.updKey(keyName, keyValue);
      }
    }

    final ValidateResult ver = validateLocation(request, form);
    if (!ver.ok) {
      return forwardRetry;
    }

    if (cl.isSuperUser()) {
      final String deleted = request.getReqPar("deleted");

      if ("true".equals(deleted)) {
        location.setStatus(BwEventProperty.statusDeleted);
      }
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
        request.message(ClientMessage.addedLocations, 1);
      } else {
        request.error(ClientError.duplicateLocation);
      }
    } else {
      request.message(ClientMessage.updatedLocation);
    }

    return forwardSuccess;
  }

  public BwLocation getLocation() {
    return getRwForm().getLocation();
  }
}
