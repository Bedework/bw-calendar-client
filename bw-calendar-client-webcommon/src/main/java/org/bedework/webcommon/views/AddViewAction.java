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

package org.bedework.webcommon.views;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.BwView;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
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
public class AddViewAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }

    Client cl = form.fetchClient();
    String name = request.getReqPar("name");

    if (name == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardNotAdded;
    }

    boolean makeDefaultView = false;

    String str = request.getReqPar("makedefaultview");
    if (str != null) {
      makeDefaultView = str.equals("y");
    }

    BwView view = new BwView();
    view.setName(name);

    if (!cl.addView(view, makeDefaultView)) {
      form.getErr().emit(ClientError.viewNotAdded, name);
      return forwardNotAdded;
    }

    form.setView(view);
    form.setViewName(view.getName());
    //form.setSubscriptions(svc.getSubscriptions());

    return forwardSuccess;
  }
}
