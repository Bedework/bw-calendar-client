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

/** Update a view for a user - add/remove subscription.
 *
 * <p>Parameters are:<ul>
 *      <li>"name"            Name of view to update</li>
 *      <li>"add"             Name of subscription to add</li>
 *      <li>"remove"          Name of subscription to remove</li>
 *      <li>"makedefaultview" Optional true/false to make this the default view.</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notAdded"     duplicate or bad name.</li>
 *      <li>"retry"        try again.</li>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class UpdateViewAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();

    /** Check access
     */
    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

    String name = request.getReqPar("name");

    if (name == null) {
      request.getErr().emit(ValidationError.missingName);
      return forwardRetry;
    }

    String add = request.getReqPar("add");
    String remove = request.getReqPar("remove");

    if (add != null) {
      if (!cl.collectionExists(add)) {
        request.getErr().emit(ClientError.unknownCalendar, add);
        return forwardNotFound;
      }

      if (!cl.addViewCollection(name, add)) {
        request.getErr().emit(ClientError.unknownView, name);
        return forwardNotFound;
      }
    }

    if (remove != null) {
      if (!cl.removeViewCollection(name, remove)) {
        request.getErr().emit(ClientError.unknownView, name);
        return forwardNotFound;
      }
    }

    /*
    boolean makeDefaultView = false;

    String str = request.getReqPar("makedefaultview");
    if (str != null) {
      makeDefaultView = str.equals("true");
    }

    /* XXX Update prefs here * /

    if (changed) {
      form.getErr().emit("org.bedework.client.notadded");
      return forwardNotAdded;
    }
    */

    BwView view = cl.getView(name);

    if (view == null) {
      request.getErr().emit(ClientError.unknownView, name);
      return forwardNotFound;
    }

    form.setView(view);

    return forwardSuccess;
  }
}
