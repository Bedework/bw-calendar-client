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

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwString;
import org.bedework.util.misc.Uid;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import javax.servlet.http.HttpServletResponse;

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
public class AddSubLocationAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();
    final HttpServletResponse resp = request.getResponse();

    /* Check access
     */
    if (cl.getPublicAdmin() && !form.getAuthorisedUser()) {
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return forwardNull;
    }

    /* Find the location we base the new one on
     */
    final String uid = request.getReqPar("uid");

    BwLocation location = null;
    if (uid != null) {
      location = cl.getLocation(uid);
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
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return forwardNull;
    }

    final String subaddr = request.getReqPar("sub");

    final BwLocation newloc = new BwLocation();

    newloc.setUid(Uid.getUid());
    newloc.setCreatorHref(location.getCreatorHref());
    newloc.setOwnerHref(location.getOwnerHref());
    newloc.setPublick(location.getPublick());

    if (location.getAddress() != null) {
      newloc.setAddress((BwString)location.getAddress().clone());

    } else {
      newloc.setAddress(new BwString(null, ""));
    }

    final BwString addr = newloc.getAddress();

    // Strip off any old room
    if (addr.getValue().contains(BwLocation.fieldDelimiter)) {
      final int pos = addr.getValue().lastIndexOf(BwLocation.fieldDelimiter);

      addr.setValue(addr.getValue().substring(0, pos));
    }

    // Add new room
    addr.setValue(
            addr.getValue() + BwLocation.fieldDelimiter + subaddr);

    if (location.getSubaddress() != null) {
      newloc.setSubaddress(
              (BwString)location.getSubaddress().clone());
    }

    newloc.setLink(location.getLink());

    try {
      cl.addLocation(newloc);
    } catch (final Throwable t) {
      if (debug) {
        error(t);
      }
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return forwardNull;
    }

    /* Return the new location */
    resp.addHeader("etag", cl.getCurrentChangeToken());
    resp.setContentType("text/json; charset=UTF-8");

    form.setLocation(newloc);
    cl.writeJson(request.getResponse(), newloc);
    resp.getOutputStream().close();

    resp.setStatus(HttpServletResponse.SC_OK);

    return forwardNull;
  }
}
