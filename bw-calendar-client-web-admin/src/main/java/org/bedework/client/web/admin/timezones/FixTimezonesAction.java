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
package org.bedework.client.web.admin.timezones;

import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminActionForm;
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
public class FixTimezonesAction extends AdminActionBase {
  public int doAction(final BwRequest request,
                      final AdminClient cl) {
    final boolean checkOnly = request.present("check") ;

    final UpdateFromTimeZonesInfo info = null;

    request.error("unimplemented");
    /* This needs fixing to take a collection href 
    for (;;) {
      info = cl.updateFromTimeZones(100, checkOnly, info);
      if (info.getTotalEventsChecked() >= info.getTotalEventsToCheck()) {
        break;
      }
    }

    form.setUpdateFromTimeZonesInfo(info);

    if (debug()) {
      for (UnknownTimezoneInfo uti: info.getUnknownTzids()) {
        debug(uti.toString());
      }
      for (BwEventKey ekey: info.getUpdatedList()) {
        debug(ekey.toString());
      }
    }

    request.message(ClientMessage.fixedTimezones);
*/
    return forwardSuccess;
  }
}

