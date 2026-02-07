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

package org.bedework.client.web.admin.location;

import org.bedework.calfacade.BwLocation;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.util.misc.Uid;
import org.bedework.webcommon.BwRequest;

import jakarta.servlet.http.HttpServletResponse;

/** This action creates a new location from the one referenced by the uid
 * by appending a new segment to the address.
 *
 * <p>This allows non-admin users to add room number locations</p>
 *
 * <p>Note this is NOT the subaddress field - we are essentially
 * suppressing this</p>
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such event.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class AddSubLocationAction extends AdminActionBase {
  @Override
  public String doAction(final BwRequest request,
                         final AdminClient cl) {
    final var form = getAdminForm();
    /* Find the location we base the new one on
     */
    final String uid = request.getReqPar("uid");

    BwLocation location = null;
    if (uid != null) {
      final var gresp = cl.getLocationByUid(uid);
      if (gresp.isOk()) {
        location = gresp.getEntity();
      }
    }

    if (debug()) {
      if (location == null) {
        info("No location with id " + uid);
      } else {
        info("Retrieved location " + location.getUid());
      }
    }

    form.assignAddingLocation(false);
    form.setLocation(location);

    final HttpServletResponse resp = request.getResponse();

    if (location == null) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return forwardNull;
    }

    final String subaddr = request.getReqPar("sub");

    final var newloc = (BwLocation)location.clone();

    newloc.setUid(Uid.getUid());
    newloc.setRoomField(subaddr);

    try {
      cl.addLocation(newloc);
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
      }
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return forwardNull;
    }

    form.setLocation(newloc);

    /* Return the new location */
    outputJson(resp, cl.getCurrentChangeToken(),
               null, newloc);

    return forwardNull;
  }
}
