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
package org.bedework.client.web.rw.calendars;

import org.bedework.calfacade.BwCalendar;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/** This action updates calendar properties. Unnamed properties are unchanged,
 * named properties are modified.
 *
 * <p>Parameters are:</p>
 * <ul>
 *      <li>"display"             Set display property</li>
 *      <li>"calPath"             Collection to update</li>
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
public class PatchCalendarAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    if (request.present("access")) {
      // Fail this to stop someone screwing around with the access
      return forwardNoAccess;
    }

    boolean changed = false;

    final BwCalendar cal = request.getCalendar(false);
    if (cal == null) {
      return forwardNotFound;
    }

    if (request.present("display")) {
      cal.setDisplay(request.getBooleanReqPar("display", true));
      changed = true;
    }

    if (request.getErrorsEmitted()) {
      return forwardRetry;
    }

    if (changed) {
      cl.updateCollection(cal);
    }

    cl.flushState();

    return forwardContinue;
  }
}

