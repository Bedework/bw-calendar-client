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

package org.bedework.webcommon.resources;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** Delete a resource.
 *
 * <p>Parameters are:<ul>
 *      <li>"name"             Name of resource</li>
 *      <li>"class"            Resource class</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such resource.</li>
 *      <li>"retry"        param missing or invalid.</li>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
public class RemoveResourceAction extends BwAbstractAction {
  /**
   * Constructor.
   */
  public RemoveResourceAction() {
  }

  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }

    String cancel = request.getReqPar("cancel");
    if (cancel != null) {
      return forwardCancelled;
    }

    Client cl = form.fetchClient();

    String name = request.getReqPar("name");
    if (name == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardRetry;
    }
    String rclass = request.getReqPar("class");
    if (rclass == null) {
      form.getErr().emit(ValidationError.missingClass);
      return forwardRetry;
    }

    BwResource r = cl.getCSResource(form.getCurrentCalSuite(), name, rclass);
    if (r == null) {
      form.getErr().emit(ClientError.unknownResource, name);
      return forwardNotFound;
    }

    cl.deleteCSResource(form.getCurrentCalSuite(), name, rclass);
    form.getMsg().emit(ClientMessage.deletedResource);
    return forwardSuccess;
  }
}
