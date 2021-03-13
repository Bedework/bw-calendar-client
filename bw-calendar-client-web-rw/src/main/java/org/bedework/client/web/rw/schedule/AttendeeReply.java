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
package org.bedework.client.web.rw.schedule;

import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/** NOTE: This action is possibly no longer needed now that replies are
 * handled automatically by the system. We probably need to retain inbox entries
 * which contain comments from the attendee or not accetptances.
 *
 * <p>Action to handle replies to scheduling requests - that is the schedule
 * method was REPLY. We, as an organizer (or their delegate) are going to
 * use the reply to update the original invitation and any copies.
 *
 * <p>The incoming event is currently set in the form event property.
 *
 * <p>Request parameters<ul>
 *      <li>update          To trigger update of stored event from the
 *                          editEvent object.</li>
 *      <li>newCalPath      Where to put the event.</li>
 *      <li>calPath         Locations of existing copies of the event.</li>
 * </ul>
 *.
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     for guest mode</li>
 *      <li>"noAction"     for invalid</li>
 *      <li>"success"      changes made.</li>
 * </ul>
 */
public class AttendeeReply extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) throws Throwable {
    return forwardEdit;
  }

  /*
  private void update(final BwEvent from, final BwEvent to) {
    if (changed(from.getSummary(), to.getSummary())) {
      to.setSummary(from.getSummary());
    }

    if (changed(from.getDescription(), to.getDescription())) {
      to.setDescription(from.getDescription());
    }

    if (changed(from.getDtstart(), to.getDtstart())) {
      to.setDtstart(from.getDtstart());
    }

    if (changed(from.getDtend(), to.getDtend())) {
      to.setDtend(from.getDtend());
    }

    if (changed(from.getEndType(), to.getEndType())) {
      to.setEndType(from.getEndType());
    }

    if (changed(from.getDuration(), to.getDuration())) {
      to.setDuration(from.getDuration());
    }

    if (changed(from.getLink(), to.getLink())) {
      to.setLink(from.getLink());
    }

    if (changed(from.getContact(), to.getContact())) {
      to.setContact((BwContact)from.getContact().clone());
    }

    if (changed(from.getLocation(), to.getLocation())) {
      to.setLocation((BwLocation)from.getLocation().clone());
    }
  }

  private boolean changed(final Object newVal, final Object oldVal) {
    if (newVal == null) {
      return false;
    }

    if (oldVal == null) {
      return true;
    }

    return !newVal.equals(oldVal);
  }
*/
}
