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

package org.bedework.client.web.rw.views;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.BwView;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/** Add a new view for a user.
 *
 * <p>Parameters are:<ul>
 *      <li>"name"             Name of view</li>
 *      <li>"makedefaultview" Optional y/n to add make this the default view.</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notAdded"     duplicate or bad name.</li>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author Mike Douglass
 */
public class AddViewAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl) {
    /* Check access
     */
    if (request.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

    final var form = getRwForm();
    final String name = request.getReqPar("name");

    if (name == null) {
      request.error(ValidationError.missingName);
      return forwardNotAdded;
    }

    boolean makeDefaultView = false;

    final String str = request.getReqPar("makedefaultview");
    if (str != null) {
      makeDefaultView = str.equals("y");
    }

    final BwView view = new BwView();
    view.setName(name);

    if (!cl.addView(view, makeDefaultView)) {
      request.error(ClientError.viewNotAdded, name);
      return forwardNotAdded;
    }

    form.setView(view);
    form.setViewName(view.getName());
    request.getSess().embedViews(request);
    //form.setSubscriptions(svc.getSubscriptions());

    return forwardSuccess;
  }
}
