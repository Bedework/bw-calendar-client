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
package org.bedework.webcommon.contact;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** This action deletes a category.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"inUse"        category is referenced by an event.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class DeleteContactAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();

    /** Check access
     */
    if (cl.isGuest() ||
            (cl.getPublicAdmin() && !form.getAuthorisedUser())) {
      return forwardNoAccess;
    }

    form.setPropRefs(null);

    String uid = form.getContact().getUid();

    Client.DeleteReffedEntityResult drer = cl.deleteContact(form.getContact());

    if (drer == null) {
      form.getErr().emit(ClientError.unknownContact, uid);
      return forwardNotFound;
    }

    if (!drer.getDeleted()) {
      form.setPropRefs(drer.getReferences());

      form.getErr().emit(ClientError.referencedContact, uid);
      return forwardInUse;
    }

    form.getMsg().emit(ClientMessage.deletedContact);

    return forwardContinue;
  }
}

