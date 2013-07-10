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
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.RenderAction;

/** Implant the subscriptions in the form.
 *
 * <p>Forwards to:<ul>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class RenderViewAction extends RenderAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    Client cl = form.fetchClient();

    String name = form.getViewName();

    if (name == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardRetry;
    }

    BwView view = cl.findView(name);

    if (view == null) {
      form.getErr().emit(ClientError.unknownView, name);
      return forwardNotFound;
    }

    form.setView(view);

    String reqpar = request.getReqPar("delete");

    if (reqpar != null) {
      return forwardDelete;
    }

    return forwardSuccess;
  }
}
