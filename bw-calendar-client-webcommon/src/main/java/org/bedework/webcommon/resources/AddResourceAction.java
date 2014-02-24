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

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.BwResourceContent;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.appcommon.CalSuiteResource;

/** Add a new resource for a calendar suite.
 *
 * <p>Parameters are:<ul>
 *      <li>"class"             Class of resource (e.g. calsuite)</li>
 *      <li>"name"              Name of the new resource</li>
 *      <li>"ct"                Content type of the new resource</li>
 *      <li>"type"              Sub-type of the resource</li>
 *      <li>"content"           (Optional) initial content of the resource</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notAdded"     duplicate or bad name.</li>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
public class AddResourceAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();

    /** Check access
     */
    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

    String rclass = request.getReqPar("class");
    final String name = request.getReqPar("name");
    String contentType = request.getReqPar("ct");
    final String type = request.getReqPar("type");

    if (rclass == null) {
      rclass = CalSuiteResource.resourceClassCalSuite;
    }

    if (name == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardNotAdded;
    }

    if (contentType == null) {
      form.getErr().emit(ValidationError.missingContentType);
      return forwardNotAdded;
    }

    if (type == null) {
      form.getErr().emit(ValidationError.missingType);
      return forwardNotAdded;
    }

    if (rclass.equals(CalSuiteResource.resourceClassGlobal) ||
            rclass.equals(CalSuiteResource.resourceClassAdmin)) {
      if (!form.getCurUserSuperUser()) {
        return forwardNoAccess;
      }
    }

    BwResource r = cl.getCSResource(form.getCurrentCalSuite(),
                                    name, rclass);
    if (r != null) {
      return forwardDuplicate;
    }

    r = new BwResource();
    r.setName(name);

    final BwResourceContent rc = new BwResourceContent();
    r.setContent(rc);

    contentType += "\ttype=" + type;
    r.setContentType(contentType);

    cl.addCSResource(form.getCurrentCalSuite(), r, rclass);

    form.setResourceName(name);
    form.setResourceClass(rclass);
    form.assignAddingResource(true);
    return forwardSuccess;
  }
}
