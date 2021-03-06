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
package org.bedework.webcommon.category;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.responses.CategoriesResponse;
import org.bedework.util.misc.response.Response;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

/** This action fetches all categories and embeds them in the session.
 *
 * <p>Forwards to:<ul>
 *      <li>"success"      ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FetchCategoriesAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (!"true".equals(request.getStringActionPar("catlist="))) {
      request.getSess().embedCategories(request, true,
                                        BwSession.editableEntity);

      // jsp transform follows for regular web page
      return forwardSuccess;
    }

    // Return as json list for widgets
    final Collection<BwCategory> vals = request.getSess().getCategoryCollection(
            request, BwSession.ownersEntity, true);

    final HttpServletResponse resp = request.getResponse();

    if (!"true".equals(request.getStringActionPar("catnofile="))) {
      resp.setHeader("Content-Disposition",
                     "Attachment; Filename=\"categoryList.json\"");
    }
    resp.setContentType("application/json; charset=UTF-8");

    final Client cl = request.getClient();
    final CategoriesResponse cats = new CategoriesResponse();
    cats.setCategories(vals);

    Response.ok(cats);

    cl.writeJson(resp, cats);
    resp.getOutputStream().close();

    return forwardNull;
  }
}
