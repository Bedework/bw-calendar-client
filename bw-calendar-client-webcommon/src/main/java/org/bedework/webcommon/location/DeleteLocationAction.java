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
import org.bedework.calfacade.BwLocation;
import org.bedework.client.rw.RWClient;
import org.bedework.client.rw.RWClient.DeleteReffedEntityResult;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/**
 * Action to delete a location.
 * <p>Request parameters:<ul>
 *      <li>  locationId</li>.
 * </ul>
 * <p>Forwards to:<ul>
 *      <li>"noAction"     for guest mode.</li>
 *      <li>"referenced"   location has references.</li>
 *      <li>"success"      deleted ok.</li>
 * </ul>
 */
public class DeleteLocationAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final RWClient cl = (RWClient)request.getClient();

    /* Check access
     */
    if (cl.isGuest() ||
        (cl.getPublicAdmin() && !form.getAuthorisedUser())) {
      return forwardNoAccess;
    }

    final String uid = form.getLocationUid();

    if (uid == null) {
      // Do nothing
      return forwardNotFound;
    }

    form.setPropRefs(null);

    final var resp = cl.getLocationByUid(uid);
    if (resp.isNotFound()) {
      return forwardNotFound;
    }
    final BwLocation loc = resp.getEntity();
    final DeleteReffedEntityResult delResult = cl.deleteLocation(loc);

    if (delResult == null) {
      form.getErr().emit(ClientError.unknownLocation, uid);
      return forwardNotFound;
    }

    if (!delResult.getDeleted()) {
      form.setPropRefs(delResult.getReferences());

      form.getErr().emit(ClientError.referencedLocation);
      return forwardReferenced;
    }

    form.getMsg().emit(ClientMessage.deletedLocations, 1);

    return forwardSuccess;
  }

}
