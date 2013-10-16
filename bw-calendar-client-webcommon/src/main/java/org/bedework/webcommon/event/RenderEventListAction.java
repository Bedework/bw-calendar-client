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
package org.bedework.webcommon.event;

import org.bedework.appcommon.FormattedEvents;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.util.Collection;

/** This action sets up a list of events for potential modification.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAction"     input error or we want to ignore the request.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"continue"     continue to list page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class RenderEventListAction extends EventActionBase {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    Collection<EventInfo> eis = getEventsList(request);
    if (eis == null) {
      return forwardNoAction;
    }

    Client cl = request.getClient();

    request.setRequestAttr(BwRequest.bwEventListParsName,
                           cl.getEventListPars());

    request.setRequestAttr(BwRequest.bwEventListName,
                           new FormattedEvents(cl, eis));

    //form.setFormattedEvents(new FormattedEvents(cl, eis));

    return forwardContinue;
  }
}
