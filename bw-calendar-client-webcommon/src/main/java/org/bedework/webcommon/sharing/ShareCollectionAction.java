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
package org.bedework.webcommon.sharing;

import org.bedework.appcommon.client.Client;
import org.bedework.caldav.util.sharing.AccessType;
import org.bedework.caldav.util.sharing.RemoveType;
import org.bedework.caldav.util.sharing.SetType;
import org.bedework.caldav.util.sharing.ShareType;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** This action sends a sharing invite for the collection identified by the href.
 * If the current user is unauthenticated and the identified collection is a
 * public calendar collection we will proceed with this as a super-user action.
 *
 * <p>There is a limit on the number of outstanding invites to any given user.
 * We also need a limit on the total number of outstanding invites by the
 * super-user. Perhaps rate-limiting and warnings in the log will prevent
 * malicious activity.
 *
 * <p>We need to log in as a super user as they have write access to the collection
 * to be shared.
 *
 * <p>Parameters to share are:</p>
 * <ul>
 *      <li>"cua"                 calendar user address</li>
 *      <li>"colHref"             Collection to share</li>
 *      <li>"colName"             suggested name of collection [optional]</li>
 *      <li>"rw"                  optional to specify read/write [optional]</li>
 * </ul>
 *
 * <p>Parameters to remove a share are:</p>
 * <ul>
 *      <li>"cua"                 calendar user address</li>
 *      <li>"colHref"             Collection to share</li>
 *      <li>"remove"              optional to specify read/write</li>
 * </ul>
 *
 * <p>Forwards to:</p>
 * <ul>
 *      <li>"noAccess"     user not authorized.</li>
 *      <li>"error"        for problems.</li>
 *      <li>"notFound"     no such calendar.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class ShareCollectionAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
//    if (form.getGuest()) {
  //    return forwardNoAccess; // First line of defence
    //}

    Client cl = request.getClient();

    BwCalendar col = request.getCollection(false);
    if (col == null) {
      return forwardNotFound;
    }

    String cua = request.getCua(false);
    if (cua == null) {
      form.getErr().emit(ValidationError.missingRecipients);
      return forwardRetry;
    }

    String calAddr = cl.uriToCaladdr(cua);
    if (calAddr == null) {
      form.getErr().emit(ValidationError.invalidUser, cua);
      return forwardRetry;
    }

    ShareType share = new ShareType();

    if (request.present("remove")) {
      RemoveType rem = new RemoveType();

      rem.setHref(calAddr);

      share.getRemove().add(rem);
    } else {
      SetType set = new SetType();

      set.setHref(calAddr);

      String colName = request.getReqPar("colName");
      if (colName == null) {
        set.setSummary(col.getSummary());
      } else {
        set.setSummary(colName);
      }

      AccessType at = new AccessType();

      if (form.getGuest()) {
        // No more than read
        at.setRead(true);
      } else {
        boolean readWrite = request.present("rw");

        if (readWrite &&
                !col.getOwnerHref().equals(cl.getCurrentPrincipalHref())) {
          readWrite = false;
        }

        if (readWrite) {
          at.setReadWrite(true);
        } else {
          at.setRead(true);
        }
      }

      set.setAccess(at);
      share.getSet().add(set);
    }

    cl.share(col.getOwnerHref(), col, share);

    if (request.getErrorsEmitted()) {
      return forwardRetry;
    }

    cl.flushState();

    return forwardContinue;
  }
}

