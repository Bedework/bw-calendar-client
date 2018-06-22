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

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwEventProperty;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.configs.BasicSystemProperties;
import org.bedework.calfacade.responses.EventPropertiesResponse;
import org.bedework.calfacade.responses.GetEntitiesResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

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
public abstract class FetchEventPropertiesAction<T extends BwEventProperty>
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
   * @throws Throwable on fatal error
   */
  protected abstract GetEntitiesResponse<T> search(BwRequest request,
                                                   String fexpr)
          throws Throwable;

  protected abstract EventPropertiesResponse makeResponse(Collection<T> eprops);

  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final String fexpr = request.getReqPar("fexpr");

    if (fexpr != null) {
      doSearch(request, form, fexpr);

      return forwardNull;
    }

    final Client cl = request.getClient();
    final HttpServletResponse resp = request.getResponse();

    form.setNocache(false);
    final String changeToken = cl.getCurrentChangeToken();

    final String ifNoneMatch = request.getRequest().getHeader("if-none-match");

    if ((changeToken != null) && changeToken.equals(ifNoneMatch)) {
      resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return forwardNull;
    }

    /* Add an etag */
    resp.addHeader("etag", changeToken);

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

    resp.setContentType("text/json; charset=UTF-8");

    final EventPropertiesResponse epresp = makeResponse(vals);

    final BasicSystemProperties props = cl.getBasicSystemProperties();
    final BwPrincipal pr = cl.getCurrentPrincipal();

    for (final T ent: vals) {
      ent.fixNames(props, pr);
    }

    if (cl.getPublicAdmin()) {
      // Add the preferred locations
      final Collection<T> prefs =
              getEProps(request, BwSession.preferredEntity);

      final List<String> preferred = new ArrayList<>();

      for (final T pref: prefs) {
        pref.fixNames(props, pr);
        preferred.add(pref.getHref());
      }

      epresp.setPreferred(preferred);
    }
    
    okReturn(epresp);

    cl.writeJson(resp, epresp);
    resp.getOutputStream().close();

    return forwardNull;
  }

  private void doSearch(final BwRequest request,
                        final BwActionFormBase form,
                        final String fexpr) throws Throwable {
    final Client cl = request.getClient();
    final HttpServletResponse resp = request.getResponse();

    form.setNocache(true);

    resp.setContentType("text/json; charset=UTF-8");

    final GetEntitiesResponse<T> ges = search(request,
                                              fexpr);

    final EventPropertiesResponse epresp = makeResponse(ges.getEntities());

    if (ges.isOk()) {
      okReturn(epresp);
    } else {
      epresp.setStatus(ges.getStatus());
      epresp.setMessage(ges.getMessage());
    }

    cl.writeJson(resp, epresp);
    resp.getOutputStream().close();
  }
}
