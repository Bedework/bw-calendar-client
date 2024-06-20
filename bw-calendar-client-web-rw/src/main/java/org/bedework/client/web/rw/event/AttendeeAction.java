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
package org.bedework.client.web.rw.event;

import org.bedework.appcommon.ClientError;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwModuleState;
import org.bedework.webcommon.BwRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;

import static org.bedework.client.web.rw.EventCommon.doAttendee;
import static org.bedework.client.web.rw.EventCommon.doFreeBusy;

/**
 * Action to handle recipients and attendees for an event. This action
 * manipulates the current list of attendees or recipients.
 *
 * <p>Request parameters<ul>
 *      <li>role            Set the role for an attendee.</li>
 *      <li>partstat        to change/set participation status.</li>
 *      <li>cn              Common name for an attendee.</li>
 *      <li>lang            language of common name for an attendee.</li>
 *      <li>dir             Directory reference.</li>
 *      <li>cutype          Calendar user type.</li>
 *      <li>uri             account or calendar uri for attendee/recipient.</li>
 *      <li>recipient       present for recipient.</li>
 *      <li>attendee        present for attendee.</li>
 *      <li>delete=anything to remove the entry for the given recipient/attendee.</li>
 *      <li>update=anything to update the entry for the given attendee.</li>
 *      <li>getfb=no        to suppress the free busy.</li>
 *      <li>list=yes        to list attendees only.</li>
 * </ul>
 *.
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     for guest mode</li>
 *      <li>"noAction"     for invalid</li>
 *      <li>"success"      changes made.</li>
 * </ul>
 */
public class AttendeeAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    final BwModuleState mstate = request.getModule().getState();

    final boolean listResponseOnly =
            "yes".equals(request.getReqPar("list"));
    final boolean noFb = "no".equals(request.getReqPar("getfb"));

    if (!listResponseOnly && !noFb) {
      /* Select appropriate view for freebusy display */
      gotoDateView(request, mstate.getDate(), mstate.getViewType());
    }

    /* If we were sent a bunch of json use that */

    int res = forwardSuccess;

    final Collection<String> attjson =
            request.getReqPars("attjson");
    if (attjson != null) {
      final ObjectMapper mapper = new ObjectMapper();

      for (final String s: attjson) {
        if (debug()) {
          debug("json=" + s);
        }

        try {
          final JsonAttendee att =
                  mapper.readValue(s, JsonAttendee.class);

          if (debug()) {
            debug(att.toString());
          }
          res = doAttendee(request,
                           form,
                           request.present("delete"),
                           request.present("update"),
                           false,            // recipient
                           true,             // attendee
                           false,
                           request.getReqPar("partstat"),
                           request.getReqPar("role"),
                           att.getUri(),
                           att.getCn(),
                           null,             // lang
                           att.getCutype(),
                           null);             // dir

          if (res != forwardSuccess) {
            return res;
          }
        } catch (final Throwable t) {
          request.error(ClientError.unknownAttendee, s);
          return forwardNoAction;
        }
      }
    } else {
      /* Try for a uri */
      final String uri = request.getReqPar("uri");

      if (uri != null) {
        res = doAttendee(request,
                         form,
                         request.present("delete"),
                         request.present("update"),
                         request.present("recipient"),
                         request.present("attendee"),
                         false,
                         request.getReqPar("partstat"),
                         request.getReqPar("role"),
                         uri,
                         request.getReqPar("cn"),
                         request.getReqPar("lang"),
                         request.getReqPar("cutype"),
                         request.getReqPar("dir"));
      }
    }

    if ((res != forwardSuccess) || noFb) {
      return res;
    }

    if (listResponseOnly) {
      // List attendees
      return forwardListAttendees;
    }

    final String st = request.getReqPar("start");
    final String et = request.getReqPar("end");
    final String intunitStr = request.getReqPar("intunit");
    final int interval = request.getIntReqPar("interval", 1);

    return doFreeBusy(request,
                      form,
                      form.getAttendees(),
                      st, et, intunitStr, interval);
  }
}
