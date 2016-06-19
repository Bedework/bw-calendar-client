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

import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.base.BwEventKey;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo.UnknownTimezoneInfo;
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
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    /** Check access
     */
    if (!form.getAuthorisedUser()) {
      return forwardNoAccess;
    }

    Client cl = request.getClient();
    boolean checkOnly = request.present("check") ;

    UpdateFromTimeZonesInfo info = null;

    for (;;) {
      info = cl.updateFromTimeZones(100, checkOnly, info);
      if (info.getTotalEventsChecked() >= info.getTotalEventsToCheck()) {
        break;
      }
    }

    form.setUpdateFromTimeZonesInfo(info);

    if (debug) {
      for (UnknownTimezoneInfo uti: info.getUnknownTzids()) {
        debugMsg(uti.toString());
      }
      for (BwEventKey ekey: info.getUpdatedList()) {
        debugMsg(ekey.toString());
      }
    }

    form.getMsg().emit(ClientMessage.fixedTimezones);

    return forwardSuccess;
  }
}

