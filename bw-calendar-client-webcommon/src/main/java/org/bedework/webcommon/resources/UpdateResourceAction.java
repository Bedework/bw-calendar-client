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

import org.bedework.appcommon.CalSuiteResource;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import org.apache.struts.upload.FormFile;

/** Update a view for a user - add/remove subscription.
 *
 * <p>Parameters are:<ul>
 *      <li>"name"            Name of view to update</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"retry"        try again.</li>
 *      <li>"success"      content updated - back to resource list.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class UpdateResourceAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();

    /** Check access
     */
    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

    final String cancel = request.getReqPar("cancel");
    if (cancel != null) {
      return forwardCancelled;
    }

    final String name = request.getReqPar("name");
    final String type = request.getReqPar("class");
    if (name == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardRetry;
    }

    if (type == null) {
      form.getErr().emit(ValidationError.missingClass);
      return forwardRetry;
    }

    final String update = request.getReqPar("update");
    final String remove = request.getReqPar("remove");

    final BwResource r = cl.getCSResource(form.getCurrentCalSuite(), name, type);
    if (r == null) {
      form.getErr().emit(ClientError.unknownResource, name);
      return forwardNotFound;
    }

    // Remove the resource
    if (remove != null) {
      return forwardDelete;
    }

    if (update == null) {
      return forwardSuccess;
    }

    // Update the resource content
    final String content = request.getReqPar("content");
    final FormFile formFile = form.getUploadFile();

    if (content != null) {
      cl.setResourceValue(r, content);
    } else if (formFile != null) {
      cl.setResourceValue(r, formFile.getFileData());
    } else {
      form.getErr().emit(ClientError.unknownResource, name);
      return forwardNotFound;
    }
      
    cl.updateResource(r, true);
    final CalSuiteResource fres = form.getCalSuiteResource();
    form.setCalSuiteResource(new CalSuiteResource(r,
                                                  fres.getRclass()));

    return forwardSuccess;
  }
}
