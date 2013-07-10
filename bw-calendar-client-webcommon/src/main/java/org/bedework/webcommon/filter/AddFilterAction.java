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
package org.bedework.webcommon.filter;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** Add a new filter.
 *
 * <p>Parameters are:<ul>
 *      <li>"name"             Name of filter</li>
 *      <li>"def"              XML definition.</li>
 *      <li>"desc"             Optional description.</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notAdded"     duplicate or bad name.</li>
 *      <li>"success"      created ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class AddFilterAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }

    Client cl = form.fetchClient();
    BwFilterDef fd = new BwFilterDef();
    fd.setName(request.getReqPar("name"));

    if (fd.getName() == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardNotAdded;
    }

    if (fd.getName().length() > BwFilterDef.maxNameLength) {
      form.getErr().emit(ValidationError.tooLongName);
      return forwardNotAdded;
    }

    fd.setDefinition(request.getReqPar("def"));

    if (fd.getDefinition() == null) {
      form.getErr().emit(ValidationError.missingFilterDef);
      return forwardNotAdded;
    }

    fd.setDescription(request.getReqPar("desc"));

    try {
      cl.validateFilter(fd.getDefinition());
      cl.saveFilter(fd);
    } catch (Throwable t) {
      form.getErr().emit(ClientError.badFilter, t.getMessage());
      return forwardNotAdded;
    }

    form.assignReloadRequired(true);

    return forwardSuccess;
  }
}
