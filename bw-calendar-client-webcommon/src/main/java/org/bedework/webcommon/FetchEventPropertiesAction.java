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

package org.bedework.webcommon;

import org.bedework.base.response.GetEntitiesResponse;
import org.bedework.base.response.Response;
import org.bedework.calfacade.BwEventProperty;
import org.bedework.calfacade.responses.EventPropertiesResponse;

import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** This action fetches event properties and writes them as a json object.
 *
 * <p>Forwards to:<ul>
 *      <li>null     always.</li>
 * </ul>
 *
 * <p>Request param is kind: value may be <ul>
 *      <li>"owners"       all owners.</li>
 *      <li>"editable"     the properties we can edit.</li>
 *      <li>"preferred"    our preferred properties.</li>
 * </ul>
 *
 * Default is "owners"</p>
 *
 * @author Mike Douglass   douglm   rpi.edu
 */
public abstract class FetchEventPropertiesAction<T extends BwEventProperty<?>>
        extends BwAbstractAction {
  protected abstract Collection<T> getEProps(BwRequest request,
                                             int kind);

  /** Return all current user entities that match the filter.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @param fexpr filter expression
   * @return Collection     of objects
   */
  protected abstract GetEntitiesResponse<T> search(
          BwRequest request,
          String fexpr);

  protected abstract EventPropertiesResponse makeResponse(Collection<T> eprops);

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

    final HttpServletResponse resp = request.getResponse();

    request.setNocache(false);

    final Collection<T> vals;

    final String kind = request.getReqPar("kind", "owners");

    switch (kind) {
      case "owners":
        vals = getEProps(request, BwSession.ownersEntity);
        break;
      case "editable":
        vals = getEProps(request, BwSession.editableEntity);
//    } else if (kind.equals("preferred")) {
//      attrName = BwRequest.bwPreferredLocationsListName;
//
//      vals = curAuthUserPrefs.getLocationPrefs().getPreferred();
        break;
      default:
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return forwardNull;
    }

    final EventPropertiesResponse epresp = makeResponse(vals);
    final var cl = request.getClient();

    if (cl.getPublicAdmin()) {
      // Add the preferred locations
      final Collection<T> prefs =
              getEProps(request, BwSession.preferredEntity);

      final List<String> preferred = new ArrayList<>();

      for (final T pref: prefs) {
        preferred.add(pref.getHref());
      }

      epresp.setPreferred(preferred);
    }

    Response.ok(epresp);

    outputJson(resp,
               cl.getCurrentChangeToken(),
               null, epresp);

    return forwardNull;
  }

  private boolean doSearch(final BwRequest request,
                           final String fexpr) {
    final HttpServletResponse resp = request.getResponse();

    request.setNocache(true);

    resp.setContentType("text/json; charset=UTF-8");

    final GetEntitiesResponse<T> ges = search(request,
                                              fexpr);

    final EventPropertiesResponse epresp = makeResponse(ges.getEntities());

    if (ges.isOk()) {
      Response.ok(epresp);
    } else {
      epresp.setStatus(ges.getStatus());
      epresp.setMessage(ges.getMessage());
    }

    return outputJson(resp, null, null, epresp);
  }
}
