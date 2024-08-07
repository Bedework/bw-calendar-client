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
package org.bedework.client.web.rw.notifications;

import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.BwRWWebGlobals;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/** This action removes a notification identified by the name.
 *
 * <p>Parameters are:</p>
 * <ul>
 *      <li>"name"                Of notification message</li>
 * </ul>
 *
 * <p>Forwards to:</p>
 * <ul>
 *      <li>"noAccess"     user not authorized.</li>
 *      <li>"error"        for problems.</li>
 *      <li>"notFound"     no such notification (may have been handled by
 *                         another client).</li>
 *      <li>"success"      all done.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class RemoveAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl) {
    final var globals = (BwRWWebGlobals)request.getGlobals();
    int forward;

    try {
      cl.removeNotification(request.getReqPar("name"));
      forward = forwardSuccess;
    } catch (final RuntimeException ca) {
      forward = forwardNoAccess;
      request.error(ca.getMessage());
    }

    globals.setNotificationInfo(null); // force a refresh
    cl.flushState();

    return forward;
  }
}

