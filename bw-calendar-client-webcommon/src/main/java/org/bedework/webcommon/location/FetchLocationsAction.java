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
import org.bedework.base.response.GetEntitiesResponse;
import org.bedework.base.response.Response;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.responses.LocationsResponse;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** This action fetches locations and writes them as a json object.
 *
 * <p>Forwards to:<ul>
 *      <li>null     always.</li>
 * </ul>
 *
 * <p>Request param is kind: value may be <ul>
 *      <li>"owners"       all owners locations.</li>
 *      <li>"editable"     the locations we can edit.</li>
 *      <li>"preferred"    our preferred locations.</li>
 * </ul>
 *
 * Default is "owners"</p>
 *
 * @author Mike Douglass   douglm   rpi.edu
 */
public class FetchLocationsAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request) {
    final String fexpr = request.getReqPar("fexpr");

    if (fexpr != null) {
      doSearch(request, fexpr);

      return forwardNull;
    }

    if (!request.contentChanged()) {
      return forwardNull;
    }

    final Client cl = request.getClient();
    final HttpServletResponse resp = request.getResponse();

    request.setNocache(false);

    final BwSession sess = request.getSess();

    final Collection<BwLocation> vals;

    final String kind = request.getReqPar("kind", "owners");

    switch (kind) {
      case "owners":
        vals = sess.getLocations(request, BwSession.ownersEntity,
                                 true);
        break;
      case "editable":
        vals = sess.getLocations(request, BwSession.editableEntity,
                                 false);
//    } else if (kind.equals("preferred")) {
//      attrName = BwRequest.bwPreferredLocationsListName;
//
//      vals = curAuthUserPrefs.getLocationPrefs().getPreferred();
        break;
      default:
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return forwardNull;
    }

    final LocationsResponse locs = new LocationsResponse();
    locs.setLocations(vals);

    if (cl.getPublicAdmin()) {
      // Add the preferred locations
      final Collection<BwLocation> prefs =
              sess.getLocations(request,
                                BwSession.preferredEntity,
                                false);

      final List<String> preferred = new ArrayList<>();

      for (final BwLocation prefLoc: prefs) {
        preferred.add(prefLoc.getHref());
      }

      locs.setPreferred(preferred);
    }

    Response.ok(locs);

    outputJson(resp,
               cl.getCurrentChangeToken(),
               null, locs);

    return forwardNull;
  }

  private void doSearch(final BwRequest request,
                        final String fexpr) {
    final Client cl = request.getClient();
    final HttpServletResponse resp = request.getResponse();

    request.setNocache(true);

    final LocationsResponse locs = new LocationsResponse();

    final GetEntitiesResponse<BwLocation> ges =
            cl.getLocations(fexpr,
                            request.getIntReqPar("from", 0),
                            request.getIntReqPar("size", 10));

    if (ges.isOk()) {
      Response.ok(locs);
      locs.setLocations(ges.getEntities());
    } else {
      locs.setStatus(ges.getStatus());
      locs.setMessage(ges.getMessage());
    }

    outputJson(resp, null, null, locs);
  }
}
