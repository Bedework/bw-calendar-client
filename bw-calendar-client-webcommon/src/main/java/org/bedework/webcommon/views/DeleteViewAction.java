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
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.BwView;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** Delete a view.
 *
 * <p>Parameters are:<ul>
 *      <li>"name"             Name of view</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such subscription.</li>
 *      <li>"retry"        try again.</li>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class DeleteViewAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();

    /** Check access
     */
    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

    final String name = request.getReqPar("name");

    if (name == null) {
      request.getErr().emit(ValidationError.missingName);
      return forwardRetry;
    }

    final BwView view = cl.getView(name);

    if (view == null) {
      request.getErr().emit(ClientError.unknownView, name);
      return forwardNotFound;
    }

    cl.removeView(view);
    request.getMsg().emit(ClientMessage.deletedView);
    request.getSess().embedViews(request);

    return forwardSuccess;
  }
}
