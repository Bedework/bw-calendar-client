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
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.CalSuiteResource;
import org.bedework.webcommon.RenderAction;

/**
 * Shows a single resource, for editing purposes.
 *
 * <p>Forwards to:<ul>
 *      <li>"success"      show edit form.</li>
 *      <li>"retry"        back to resource list.</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
public class RenderResourceAction extends RenderAction {
  /**
   * Constructor.
   */
  public RenderResourceAction() {
  }

  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    Client cl = form.fetchClient();

    String name = form.getResourceName();
    if (name == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardRetry;
    }
    String rclass = form.getResourceClass();
    if (rclass == null) {
      rclass = "calsuite";
    }

    if (rclass.equals("global") || rclass.equals("admin")) {
      if (!form.getCurUserSuperUser()) {
        return forwardNoAccess;
      }
    }
    String mod = request.getReqPar("mod");

    BwResource resource = cl.getCSResource(form.getCurrentCalSuite(), name, rclass);
    if (resource == null) {
      form.getErr().emit(ClientError.unknownResource, name);
      return forwardRetry;
    }

    if (mod != null && mod.equals("true")) {
      form.assignAddingResource(false);
    }
    form.setCalSuiteResource(new CalSuiteResource(resource, rclass));

    return forwardSuccess;
  }
}
