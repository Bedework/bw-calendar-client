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
package org.bedework.webcommon.timezones;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** This action fixes the system after uploading system timezone definitions.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"error"        some sort of fatal error.</li>
 *      <li>"failed"       for some reason.</li>
 *      <li>"success"      processed.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FixTimezonesAction extends BwAbstractAction {
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    /* Check access
     */
    if (!form.getAuthorisedUser()) {
      return forwardNoAccess;
    }

    Client cl = request.getClient();
    boolean checkOnly = request.present("check") ;

    UpdateFromTimeZonesInfo info = null;

    form.getErr().emit("unimplemented");
    /* This needs fixing to take a collection href 
    for (;;) {
      info = cl.updateFromTimeZones(100, checkOnly, info);
      if (info.getTotalEventsChecked() >= info.getTotalEventsToCheck()) {
        break;
      }
    }

    form.setUpdateFromTimeZonesInfo(info);

    if (debug) {
      for (UnknownTimezoneInfo uti: info.getUnknownTzids()) {
        debug(uti.toString());
      }
      for (BwEventKey ekey: info.getUpdatedList()) {
        debug(ekey.toString());
      }
    }

    form.getMsg().emit(ClientMessage.fixedTimezones);
*/
    return forwardSuccess;
  }
}

