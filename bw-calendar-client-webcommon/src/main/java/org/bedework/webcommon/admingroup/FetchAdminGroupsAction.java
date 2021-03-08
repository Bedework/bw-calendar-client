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

package org.bedework.webcommon.admingroup;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.responses.AdminGroupsResponse;
import org.bedework.util.misc.response.Response;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

/** This action fetches admin groups and writes them as a json object.
 * READ-ONLY + ADMIN
 *
 * <p>Forwards to:<ul>
 *      <li>null     always.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm   spherical cow
 */
public class FetchAdminGroupsAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
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

    final Collection<BwGroup> vals = cl.getAdminGroups();

    resp.setContentType("text/json; charset=UTF-8");

    final AdminGroupsResponse adgs = new AdminGroupsResponse();
    adgs.setGroups(vals);
    
    Response.ok(adgs);

    cl.writeJson(resp, adgs);
    resp.getOutputStream().close();

    return forwardNull;
  }
}
