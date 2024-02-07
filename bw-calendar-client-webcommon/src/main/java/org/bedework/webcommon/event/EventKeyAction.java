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

import org.bedework.appcommon.EventKey;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/**
 * Action to set up an event for a subsequent fetch.
 * <p>Request parameters<ul>
 *      <li>"subid"    subscription id for event.</li>
 *      <li>"calPath"  calendar for event.</li>
 *      <li>"guid"     guid of event.</li>
 *      <li>"eventname"   name of event (instead of guid).</li>
 *      <li>"recurrenceId"   recurrence-id of event instance - possibly null.</li>
 * </ul>
 * <p>Forwards to:<ul>
 *      <li>"noAction"     when request seems wrong.</li>
 *      <li>"showEvent"    event is setup for viewing.</li>
 * </ul>
 */
public class EventKeyAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) {
    final EventKey ekey = request.makeEventKey(false);
    form.setEventKey(ekey);

    if (ekey == null) {
      return forwardNoAction;
    }

    request.setRequestAttr(BwRequest.eventStateName,
                           form.getEventState());

    return forwardSuccess;
  }
}
