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
package org.bedework.client.web.rw.contact;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwString;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/** This action fetches a sponsor.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such event.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm   rpi.edu
 */
public class FetchContactAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    /* User requested a contact from the list. Retrieve it, embed it in
     * the form so we can display the page
     */
    final String uid = request.getReqPar("uid");

    BwContact contact = null;
    if (uid != null) {
      final var resp = cl.getContactByUid(uid);
      if (resp.isOk()) {
        contact = resp.getEntity();
      }
    }

    if (debug()) {
      if (contact == null) {
        info("No contact with id " + uid);
      } else {
        info("Retrieved contact " + contact.getUid());
      }
    }

    form.assignAddingContact(false);
    form.setContact(contact);

    if (contact == null) {
      request.error(ClientError.unknownContact, uid);
      return forwardNotFound;
    }

    if (contact.getCn() != null) {
      form.setContactName((BwString)contact.getCn().clone());
      form.setContactStatus(contact.getCn().getLang());
    } else {
      form.setContactName(null);
    }

    return forwardContinue;
  }
}

