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
package org.bedework.client.web.rw.sharing;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.svc.SubscribeResult;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.RWActionBase;
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
public class SubscribeAction extends RWActionBase {
  @Override
  public String doAction(final BwRequest request,
                         final RWClient cl) {
    final String colName = request.getReqPar("colName");
    if (colName == null) {
      request.error(ClientError.badRequest, "Missing colName");
      return forwardError;
    }

    final String href = request.getReqPar("colHref");
    String extUrl = null;
    if (href == null) {
      extUrl = request.getReqPar("extUrl");

      if (extUrl == null) {
        request.error(ClientError.badRequest, "Missing colHref or extUrl");
        return forwardError;
      }
    } else if (request.getReqPar("extUrl") != null) {
      request.error(ClientError.badRequest, "Must supply only one of colHref or extUrl");
      return forwardError;
    }

    final SubscribeResult sr;

    if (href != null) {
      sr = cl.subscribe(href, colName);
    } else {
      sr = cl.subscribeExternal(extUrl,
                                colName,
                                request.getIntReqPar("refresh", -1),
                                request.getReqPar("remoteId"),
                                request.getReqPar("remotePw"));
    }

    if (sr.isAlreadySubscribed()) {
      request.error(ClientError.alreadySubscribed, sr.getPath());
    }

    cl.flushState();

    return forwardContinue;
  }
}

