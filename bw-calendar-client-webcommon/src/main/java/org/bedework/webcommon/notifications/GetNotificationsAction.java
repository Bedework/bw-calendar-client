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

import org.bedework.appcommon.NotificationInfo;
import org.bedework.appcommon.client.Client;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/**
 * Ajax action to get notifications
 * <p>Request parameters:<ul>
 *      <li>  email   - address for emails - may be repeated</li>.
 *      <li>  add | remove  - only one must be present </li>.
 * </ul>
 */
public class GetNotificationsAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();
    //final HttpServletResponse response = request.getResponse();

    /** Check access
     */
    if (cl.isGuest()) {
      return forwardNoAccess;
    }

    NotificationInfo ni = form.getNotificationInfo();
    if (ni == null) {
      ni = new NotificationInfo();
      form.setNotificationInfo(ni);
    }

    ni.refresh(cl, false);

    return forwardSuccess;
  }
}
