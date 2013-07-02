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

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.SharingI;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** This action published a collection making it world readable and providing an
 * external url for access by outside users.
 *
 * <p>It also becomes subscribable and will be indexed as a public collection.
 *
 * <p>Parameters to publish are:</p>
 * <ul>
 *      <li>"colHref"             Collection to subscribe to or ...</li>
 *      <li>"extUrl"              ... external url to subscribe to</li>
 *      <li>"colName"             name for the alias</li>
 *      <li>"refresh"             external only - refresh rate minutes - optional
 *      <li>"remoteId"            external only - external id - optional
 *      <li>"remotePw"            external only - external pw - optional
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
public class SubscribeAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
//    if (form.getGuest()) {
  //    return forwardNoAccess; // First line of defence
    //}

    String colName = request.getReqPar("colName");
    if (colName == null) {
      form.getErr().emit(ClientError.badRequest, "Missing colName");
      return forwardError;
    }

    CalSvcI svci = form.fetchSvci();
    Client cl = form.fetchClient();

    String href = request.getReqPar("colHref");
    String extUrl = null;
    if (href == null) {
      extUrl = request.getReqPar("extUrl");

      if (extUrl == null) {
        form.getErr().emit(ClientError.badRequest, "Missing colHref or extUrl");
        return forwardError;
      }
    } else if (request.getReqPar("extUrl") != null) {
      form.getErr().emit(ClientError.badRequest, "Must supply only one of colHref or extUrl");
      return forwardError;
    }

    SharingI shares = svci.getSharingHandler();
    SharingI.SubscribeResult sr;

    if (href != null) {
      sr = shares.subscribe(href, colName);
    } else {
      sr = shares.subscribeExternal(extUrl,
                                    colName,
                                    request.getIntReqPar("refresh", -1),
                                    request.getReqPar("remoteId"),
                                    request.getReqPar("remotePw"));
    }

    if (sr.alreadySubscribed) {
      form.getErr().emit(ClientError.alreadySubscribed, sr.path);
    }

    cl.flushState();

    return forwardContinue;
  }
}

