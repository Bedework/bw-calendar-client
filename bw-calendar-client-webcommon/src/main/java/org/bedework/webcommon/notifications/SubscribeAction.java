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
package org.bedework.webcommon.notifications;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.client.rw.RWClient;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * Ajax action to subscribe for email notifications
 * <p>Request parameters:<ul>
 *      <li>  email   - address for emails - may be repeated</li>.
 *      <li>  add | remove  - only one must be present </li>.
 * </ul>
 */
public class SubscribeAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final RWClient cl = (RWClient)request.getClient();
    final HttpServletResponse response = request.getResponse();

    /* Check access
     */
    if (cl.isGuest()) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return forwardNull;
    }

    final boolean add = request.present("add");
    final boolean remove = request.present("remove");

    if (add && remove) {
      form.getErr().emit(ClientError.badRequest);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return forwardNull;
    }

    // TODO This should only be the default - we need to allow it to be supplied
    final String href = cl.getCurrentPrincipalHref();

    final List<String> emails = request.getReqPars("email");

    try {
      if (remove) {
        cl.unsubscribe(href, emails);
      } else {
        if (Util.isEmpty(emails)) {
          form.getErr().emit(ClientError.badRequest);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return forwardNull;
        }

        cl.subscribe(href, emails);
      }
    } catch (final CalFacadeException cfe) {
      cl.rollback();
      if (debug()) {
        error(cfe);
      }
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return forwardNull;
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return forwardNull;
  }
}
